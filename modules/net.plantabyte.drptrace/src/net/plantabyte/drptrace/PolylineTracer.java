package net.plantabyte.drptrace;

import net.plantabyte.drptrace.geometry.BezierCurve;
import net.plantabyte.drptrace.geometry.BezierShape;
import net.plantabyte.drptrace.geometry.Vec2;
import net.plantabyte.drptrace.geometry.Vec2i;
import net.plantabyte.drptrace.intmaps.ZOrderBinaryMap;
import net.plantabyte.drptrace.math.Util;
import net.plantabyte.drptrace.trace.TraceMachine;

import java.util.ArrayList;
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
		final double cornerAngleThreshold = 0.75/Math.PI;
		final int limit = pathPoints.length-1;
		double beforeLastAngle = 0;
		double lastAngle = 0;
		for(int i = 1; i < limit; i++){
			final int start = Math.max(0,i-fittingWindowSize);
			final int end = Math.min(pathPoints.length,i+fittingWindowSize);
			var preSlope = Util.linearRegressionAngle(pathPoints, start, i-start);
			var postSlope = Util.linearRegressionAngle(pathPoints, i, end-i);
			var angle = Vec2.angle(preSlope, postSlope);
			if(angle > cornerAngleThreshold && angle < lastAngle && lastAngle > beforeLastAngle){
				// local maximum just passed
				nodeIndices.add(i-1);
			}
			beforeLastAngle = lastAngle;
			lastAngle = angle;
		}
		final var segments = new BezierShape(nodeIndices.size()+2);
		segments.setClosed(closedLoop);
		int lastIndex = 0;
		for(int i : nodeIndices){
			segments.add(new BezierCurve(pathPoints[lastIndex], pathPoints[i]));
			lastIndex = i;
		}
		segments.add(new BezierCurve(pathPoints[lastIndex], pathPoints[(pathPoints.length+e_offset)%pathPoints.length]));
		// TODO: not done yet
		return segments;
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
		var searchedMap = new ZOrderBinaryMap(w, h);
		var output = new LinkedList<BezierShape>();
		// algorithm: flood fill each patch, and for
		// each flood fill, trace the outer edge
		for(int y = 0; y < h; y++){
			for(int x = 0; x < w; x++){
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