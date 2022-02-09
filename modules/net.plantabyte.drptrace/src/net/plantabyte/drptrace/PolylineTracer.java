package net.plantabyte.drptrace;

import net.plantabyte.drptrace.geometry.BezierCurve;
import net.plantabyte.drptrace.geometry.BezierShape;
import net.plantabyte.drptrace.geometry.Vec2;
import net.plantabyte.drptrace.geometry.Vec2i;
import net.plantabyte.drptrace.intmaps.ZOrderBinaryMap;
import net.plantabyte.drptrace.math.Util;
import net.plantabyte.drptrace.trace.TraceMachine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import static net.plantabyte.drptrace.intmaps.IntMapUtil.floodFill;

/**
 * The PolylineTracer class provides methods for turning a series of points into a
 * sequence of bezier curves tracing that path, using an algorithm of successive
 * subdivisions of straight paths, followed by smoothing to fit the curves of the
 * path. If tracing a shape, use
 * <code>traceClosedPath(Vec2[], int)</code>; if tracing a line, use
 * <code>traceOpenPath(Vec2[], int)</code>. For tracing a whole raster image,
 * use <code>traceAllShapes(IntMap)</code>.
 */
public class PolylineTracer {
	/**
	 * Traces a series of points as a sequence of bezier curves, looping back to
	 * the beginning to form a closed loop. The density of bezier curves is
	 * controlled by the <code>interval</code> score. Specifically, the
	 * interval number is the ratio of provided path points to the number of
	 * beziers. For example, a interval of 10 means that there will be 1 bezier
	 * for every 10 path points.
	 * @param pathPoints A series of points to trace with bezier curves. MUST
	 *                   contain at least 3 points
	 * @return Returns a list of <code>BezierCurve</code>s tracing the path of
	 * the points
	 * @throws IllegalArgumentException Thrown if any of the input arguments are
	 * invalid
	 */
	public BezierShape traceClosedPath(Vec2[] pathPoints)
			throws IllegalArgumentException {
		return tracePath(pathPoints, true);
	}

	/**
	 * Traces a series of points as a sequence of bezier curves. The density of
	 * bezier curves is controlled by the <code>interval</code> score.
	 * Specifically, the interval number is the ratio of provided path points
	 * beziers. For example, a interval of 10 means that there will be 1 bezier
	 * for every 10 path points.
	 * @param pathPoints A series of points to trace with bezier curves. MUST
	 *                   contain at least 2 points
	 * @return Returns a list of <code>BezierCurve</code>s tracing the path of
	 * the points
	 * @throws IllegalArgumentException Thrown if any of the input arguments are
	 * invalid
	 */
	public BezierShape traceOpenPath(Vec2[] pathPoints)
			throws IllegalArgumentException {
		return tracePath(pathPoints, false);
	}

	private BezierShape tracePath(Vec2[] pathPoints, boolean closedLoop)
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
		final var segments = new BezierShape(nodeIndices.size()+2);
		segments.setClosed(closedLoop);
		for(int i = 1; i < nodeIndices.size(); i++){
			// skip duplicates
			if(nodeIndices.get(i-1) == nodeIndices.get(i)){
				continue;
			}
			segments.add(new BezierCurve(pathPoints[nodeIndices.get(i-1)], pathPoints[nodeIndices.get(i)]));
		}
		segments.add(new BezierCurve(pathPoints[nodeIndices.get(nodeIndices.size()-1)], pathPoints[(pathPoints.length+e_offset)%pathPoints.length]));
		// TODO: not done yet
		return segments;
	}


	private static double crossProductMagnitude(Vec2 a, Vec2 b){
//		var c = new double[]{
//				a[1]*b[2] - a[2]*b[1], // x
//				a[2]*b[0] - a[0]*b[2], // y
//				a[0]*b[1] - a[1]*b[0]  // z
//		};
		return a.x*b.y - a.y*b.x;
	}
	/**
	 * Traces every shape (including the background) of the provided raster bitmap.
	 * The density of bezier curves is controlled by the <code>interval</code>
	 * score. Specifically, the interval number is the ratio of provided path
	 * points beziers. For example, a interval of 10 means that there will be 1
	 * bezier for every 10 path points.
	 * @param bitmap A 2D array of integer values, such that each contiguous area
	 *               of a number is considered to be a single shape.
	 * @return Returns a list of <code>BezierShape</code> objects, each representing
	 * one shape from the raster. The order is important: the shapes should be drawn
	 * in the order such that the first index is in the back and the last index is
	 * in the front.
	 * @throws IllegalArgumentException Thrown if any of the input arguments are
	 * invalid
	 */
	public List<BezierShape> traceAllShapes(final IntMap bitmap) throws IllegalArgumentException {
		final int w = bitmap.getWidth(), h = bitmap.getHeight();
		final int halfW = w/2;
		var searchedMap = new ZOrderBinaryMap(w, h);
		var output = new LinkedList<BezierShape>();
		// algorithm: flood fill each patch, and for
		// each flood fill, trace the outer edge
		for(int y = 0; y < h; y++){
			for(int tx = 0; tx < w; tx++){
				// offset x to start search in middle of top instead of top-left
				final int x = (tx + halfW) % w;
				if(searchedMap.get(x, y) == 0){ // pixel not yet searched
					// first, find a patch of color in the bitmap
					int color = bitmap.get(x, y);
					// next, trace the outer perimeter of the color patch
					var circumference = TraceMachine.followEdge(bitmap, x, y);
					var vectorized = traceClosedPath(circumference);
					vectorized.setColor(color);
					vectorized.setClosed(true);
					output.add(vectorized);
					// finally, flood-fill the patch in the searched map
					floodFill(bitmap, searchedMap, x, y);
					searchedMap.set(x, y, (byte)1);
				}
			}
		}
		return output;
	}

}
