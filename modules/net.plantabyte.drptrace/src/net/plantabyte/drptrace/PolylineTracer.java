package net.plantabyte.drptrace;

import net.plantabyte.drptrace.geometry.BezierCurve;
import net.plantabyte.drptrace.geometry.BezierShape;
import net.plantabyte.drptrace.geometry.Vec2;
import net.plantabyte.drptrace.geometry.Vec2i;
import net.plantabyte.drptrace.intmaps.ZOrderBinaryMap;
import net.plantabyte.drptrace.math.HillClimbSolver;
import net.plantabyte.drptrace.math.Solver;
import net.plantabyte.drptrace.math.Util;
import net.plantabyte.drptrace.trace.TraceMachine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static net.plantabyte.drptrace.intmaps.IntMapUtil.floodFill;
import static net.plantabyte.drptrace.math.Util.RMSE;

/**
 * The PolylineTracer class provides methods for turning a series of points into a
 * sequence of bezier curves tracing that path, using an algorithm of successive
 * subdivisions of straight paths, followed by smoothing to fit the curves of the
 * path. If tracing a shape, use
 * <code>traceClosedPath(Vec2[])</code>; if tracing a line, use
 * <code>traceOpenPath(Vec2[])</code>. For tracing a whole raster image,
 * use <code>traceAllShapes(IntMap)</code>.
 * <p>
 * The <code>PolylineTracer</code> traces paths by detecting corners and
 * inflection points, making it potentially a better choice than <code>IntervalTracer</code>
 * when tracing large shapes that possess occasional fin details,
 * producing fewer nodes on straight edges and more nodes around small
 * features in the path.
 * </p>
 */
public class PolylineTracer extends Tracer{
	/**
	 * The <code>PolylineTracer</code> traces paths by detecting corners and
	 * inflection points, making it potentially a better choice than <code>IntervalTracer</code>
	 * when tracing large shapes that possess occasional fin details,
	 * producing fewer nodes on straight edges and more nodes around small
	 * features in the path.
	 */
	public PolylineTracer(){
		//
	}
	/**
	 * Traces a series of points as a sequence of bezier curves, looping back to
	 * the beginning to form a closed loop if so specified by the <code>closedLoop</code>
	 * parameter.
	 * @param pathPoints A series of points to trace with bezier curves. MUST
	 *                   have at least 3 points for a closed loop and at least
	 *                   2 points for an open path!
	 * @param closedLoop If true, traceback to the starting point (index 0);
	 *                   if false, trace to the final point
	 * @return Returns a list of <code>BezierCurve</code>s tracing the path of
	 * the points
	 * @throws IllegalArgumentException Thrown if any of the input arguments are
	 * invalid (eg too few points)
	 */
	@Override
	public BezierShape tracePath(Vec2[] pathPoints, boolean closedLoop)
			throws IllegalArgumentException{
		//
		final int min_pts = closedLoop ? 3 : 2;
		final int e_offset = closedLoop ? 0 : -1;
		if(pathPoints.length < min_pts){
			throw new IllegalArgumentException(String.format("Must have at least %s points to trace %s path",
					min_pts, closedLoop ? "closed" : "open"));
		}
		final int fittingWindowSize = 5;
		final var nodeIndices = new ArrayList<Integer>(pathPoints.length/2);
		nodeIndices.add(0);
		if(pathPoints.length <= 16) {
			// too small for fancy stuff
			nodeIndices.add(pathPoints.length/4);
			nodeIndices.add(pathPoints.length/2);
			nodeIndices.add((3*pathPoints.length)/4);
		} else {
			final double cornerAngleThreshold = 0.75 * Math.PI;
			final int limit = pathPoints.length - 2;
			final int quarterCount = Math.max(1, pathPoints.length / 4);
			double beforeLastAngle = Math.PI; // remember, sharp turn equals small angle
			double lastAngle = Math.PI;
			var curvitureBuffer = new double[pathPoints.length];
			//Arrays.fill(curvitureBuffer, 0);
			for (int i = 1; i < limit; i++) {
				final var p = pathPoints[i];
				final int start = Math.max(0, i - fittingWindowSize);
				final int end = Math.min(pathPoints.length, i + fittingWindowSize);
				var preWindowAve = Vec2.average(pathPoints, start, i - start);
				var postWindowAve = Vec2.average(pathPoints, i + 1, end - (i + 1));
				var thisWindowAve = Vec2.average(pathPoints, start, end - start);
				var angle = p.angleBetween(preWindowAve, postWindowAve);
				var curviture = Vec2.curvitureOf(preWindowAve, thisWindowAve, postWindowAve);
				curvitureBuffer[i] = curviture;
				// first, make sure interval is never more than 25% of total path
				final int lastIndex = nodeIndices.isEmpty() ? 0 : nodeIndices.get(nodeIndices.size() - 1);
				if ((i - lastIndex) >= quarterCount) {
					nodeIndices.add(i);
				} else
					// second, detect corners
					if (angle < cornerAngleThreshold && angle > lastAngle && lastAngle < beforeLastAngle) {
						// local turn maximum just passed (local angle minumum)
						nodeIndices.add(i - 1);
					} else

						beforeLastAngle = lastAngle;
				lastAngle = angle;

			}
			// third, add inflection points (and then resort to put things back in order)
			final double[] curvitures = Util.rollingAverage(curvitureBuffer, 7);
			for (int i = 2; i < curvitures.length - 2; ++i) {
				if (curvitures[i] < curvitures[i - 1] && curvitures[i - 1] < curvitures[i - 2]
						&& curvitures[i] < curvitures[i + 1] && curvitures[i + 1] < curvitures[i + 2]) {
					// i is local minimum, thus is an inflection point
					nodeIndices.add(i);
				}
			}
			nodeIndices.sort(Integer::compareTo);
		}
		nodeIndices.add((pathPoints.length+e_offset)%pathPoints.length);
		// deduplicate (should be a rare occurence)
		for(int i = nodeIndices.size()-1; i > 0; --i){
			if(nodeIndices.get(i).equals(nodeIndices.get(i-1))){
				nodeIndices.remove(i);
			}
		}
		// now fit to point data
		final var segments = new BezierShape(nodeIndices.size()+2);
		final var startIndices = new ArrayList<Integer>();
		final var endIndices = new ArrayList<Integer>();
		segments.setClosed(closedLoop);
		for(int i = 1; i < nodeIndices.size(); ++i){
			final int start = nodeIndices.get(i-1);
			startIndices.add(start);
			final int end = nodeIndices.get(i);
			endIndices.add(end);
			final int endi = end == 0 ? pathPoints.length : end;
			final var p1 = pathPoints[start];
			final var p2 = pathPoints[(start+1)% pathPoints.length];
			final var p3 = pathPoints[(end+pathPoints.length-1)% pathPoints.length];
			final var p4 = pathPoints[end];
			var bc = new BezierCurve(p1, p2, p3, p4);
			bc.fitToPoints(pathPoints, start, endi-start);
			segments.add(bc);
		}
		// smooth out almost smooth nodes
		final double smoothAngleThreshold = 0.75*Math.PI;
		for(int n = 0; n < segments.size()-1-e_offset; ++n){
			final int start = startIndices.get(n);
			final int middle = endIndices.get(n);
			final int end = endIndices.get((n+1)%segments.size());
			var curr = segments.get(n%segments.size());
			var next = segments.get((n+1)%segments.size());
			var angle = curr.getP4().angleBetween(curr.getP3(), next.getP2());
			if(angle > smoothAngleThreshold) {
				var r = smoothOut(curr, next, pathPoints, start, middle, end == 0 ? pathPoints.length : end);
				segments.set(n%segments.size(), r[0]);
				segments.set((n+1)%segments.size(), r[1]);
			}
		}



		return segments;
	}

	/**
	 * makes the point between two beziers smooth and re-fits to the corresponding data segments from the list of points
	 * @param b1 bezier 1
	 * @param b2 bezier 2
	 * @param pathPoints all points
	 * @param start index at start of b1
	 * @param middle index at end of b1/start of b2
	 * @param end index at end of b2
	 * @return array of two bezier curves
	 */
	private static BezierCurve[] smoothOut(final BezierCurve b1, final BezierCurve b2, final Vec2[] pathPoints, final int start, final int middle, final int end) {
		final var deltaVec = b2.getP2().sub(b1.getP3());
		final double L1 = -1*b1.getP3().dist(b1.getP4());
		final double L2 = b2.getP2().dist(b2.getP1());
		final double angle = Math.atan2(deltaVec.y, deltaVec.x);
		final double[] params = new double[]{angle, L1, L2};
		Function<double[], Double> optiFunc = (double[] paramArray) -> {
			final double _angle = paramArray[0];
			final double _L1 = paramArray[1];
			final double _L2 = paramArray[2];
			final var b1p3 = new Vec2(_L1*Math.cos(_angle), _L1*Math.sin(_angle)).add(b1.getP4());
			final var b2p2 = new Vec2(_L2*Math.cos(_angle), _L2*Math.sin(_angle)).add(b2.getP1());
			return RMSE(
					new BezierCurve(b1.getP1(), b1.getP2(), b1p3, b1.getP4()), pathPoints, start, middle-start
			) + RMSE(
					new BezierCurve(b2.getP1(), b2p2, b2.getP3(), b2.getP4()), pathPoints, middle, end-middle
			);
		};
		Solver solver = new HillClimbSolver(0.1, 10000);
		double[] optimizedArray = solver.minimize(optiFunc, params);
		final var new_b1p3 = new Vec2(optimizedArray[1]*Math.cos(optimizedArray[0]), optimizedArray[1]*Math.sin(optimizedArray[0])).add(b1.getP4());
		final var new_b2p2 = new Vec2(optimizedArray[2]*Math.cos(optimizedArray[0]), optimizedArray[2]*Math.sin(optimizedArray[0])).add(b2.getP1());
		return new BezierCurve[]{
				new BezierCurve(b1.getP1(), b1.getP2(), new_b1p3, b1.getP4()),
				new BezierCurve(b2.getP1(), new_b2p2, b2.getP3(), b2.getP4())
		};
	}


	private static double crossProductMagnitude(Vec2 a, Vec2 b){
//		var c = new double[]{
//				a[1]*b[2] - a[2]*b[1], // x
//				a[2]*b[0] - a[0]*b[2], // y
//				a[0]*b[1] - a[1]*b[0]  // z
//		};
		return a.x*b.y - a.y*b.x;
	}

}
