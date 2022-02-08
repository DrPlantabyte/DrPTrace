package net.plantabyte.drptrace;

import net.plantabyte.drptrace.geometry.BezierCurve;
import net.plantabyte.drptrace.geometry.BezierShape;
import net.plantabyte.drptrace.geometry.Vec2;
import net.plantabyte.drptrace.geometry.Vec2i;
import net.plantabyte.drptrace.intmaps.ZOrderBinaryMap;
import net.plantabyte.drptrace.trace.TraceMachine;

import java.util.LinkedList;
import java.util.List;

import static net.plantabyte.drptrace.intmaps.IntMapUtil.floodFill;
import static net.plantabyte.drptrace.trace.TraceMachine.followEdge;

/**
 * The IntervalTracer class provides methods for turning a series of points into a
 * sequence of bezier curves tracing that path. If tracing a shape, use
 * <code>traceClosedPath(Vec2[], int)</code>; if tracing a line, use
 * <code>traceOpenPath(Vec2[], int)</code>. For tracing a whole raster image,
 * use <code>traceAllShapes(IntMap, int)</code>.
 * <p>
 * Your tracing process should look like this:<br>
 * 1. transfer/store your raster data in an <code>IntMap</code><br>
 * 2. Instantiate a <code>new Tracer()</code><br>
 * 3. Call <code>Tracer.traceAllShapes(IntMap, int)</code> to trace the raster
 * to a list of <code>BezierShape</code>s<br>
 * 4. Read the bezier curves from the <code>BezierShape</code> list<br>
 * <p>
 * Note that the "interval" parameter is used to adjust the density of bezier
 * curve nodes, with a higher number resulting in fewer nodes. 10 is usually a
 * good value to use.
 */
public class IntervalTracer {
	/**
	 * Traces a series of points as a sequence of bezier curves, looping back to
	 * the beginning to form a closed loop. The density of bezier curves is
	 * controlled by the <code>interval</code> score. Specifically, the
	 * interval number is the ratio of provided path points to the number of
	 * beziers. For example, a interval of 10 means that there will be 1 bezier
	 * for every 10 path points.
	 * @param pathPoints A series of points to trace with bezier curves. MUST
	 *                   contain at least 3 points
	 * @param interval Controls the density of beziers (higher number means
	 *                   fewer bezier curves). MUST be at least 1 (10 recommended).
	 * @return Returns a list of <code>BezierCurve</code>s tracing the path of
	 * the points
	 * @throws IllegalArgumentException Thrown if any of the input arguments are
	 * invalid
	 */
	public BezierShape traceClosedPath(Vec2[] pathPoints, int interval)
			throws IllegalArgumentException {
		return tracePath(pathPoints, interval, true);
	}

	/**
	 * Traces a series of points as a sequence of bezier curves. The density of
	 * bezier curves is controlled by the <code>interval</code> score.
	 * Specifically, the interval number is the ratio of provided path points
	 * beziers. For example, a interval of 10 means that there will be 1 bezier
	 * for every 10 path points.
	 * @param pathPoints A series of points to trace with bezier curves. MUST
	 *                   contain at least 2 points
	 * @param interval Controls the density of beziers (higher number means
	 *                   fewer bezier curves). MUST be at least 1 (10 recommended).
	 * @return Returns a list of <code>BezierCurve</code>s tracing the path of
	 * the points
	 * @throws IllegalArgumentException Thrown if any of the input arguments are
	 * invalid
	 */
	public BezierShape traceOpenPath(Vec2[] pathPoints, int interval)
			throws IllegalArgumentException {
		return tracePath(pathPoints, interval, false);
	}

	private BezierShape tracePath(Vec2[] pathPoints, int interval, boolean closedLoop)
			throws IllegalArgumentException{
		//
		final int min_beziers = closedLoop ? 2 : 1;
		final int min_pts = closedLoop ? 3 : 2;
		final int e_offset = closedLoop ? 0 : -1;
		if(pathPoints.length < min_pts){
			throw new IllegalArgumentException(String.format("Must have at least %s points to trace %s path",
					min_pts, closedLoop ? "closed" : "open"));
		}
		if(interval < 1){
			throw new IllegalArgumentException("interval must be a positive number");
		}
		final int numPoints = pathPoints.length;
		final int numBeziers = Math.max(numPoints / interval, min_beziers);
		final int intervalSize = numPoints/numBeziers + 1; // last interval may be a different size
		var beziers = new BezierShape(numBeziers);

		int start = 0;
		for(int c = 0; c < numBeziers && start < numPoints; c++){
			int end = Math.min(start + intervalSize, numPoints + e_offset); // start and end are both inclusive
			// note: exclude end points from fitting
			final Vec2[] buffer;
			if(end - start < 3){
				beziers.add(new BezierCurve(pathPoints[start], pathPoints[end % numPoints]));
			} else {
				buffer = new Vec2[end - start - 2];
				System.arraycopy(pathPoints, start + 1, buffer, 0, buffer.length);
				var b = new BezierCurve(pathPoints[start], buffer[0],
						buffer[buffer.length - 1], pathPoints[end % numPoints]
				);
				b.fitToPoints(buffer);
				beziers.add(b);
			}
			start = end;
		}
		beziers.setClosed(closedLoop);
		return beziers;
	}

	/**
	 * Traces every shape (including the background) of the provided raster bitmap.
	 * The density of bezier curves is controlled by the <code>interval</code>
	 * score. Specifically, the interval number is the ratio of provided path
	 * points beziers. For example, a interval of 10 means that there will be 1
	 * bezier for every 10 path points.
	 * @param bitmap A 2D array of integer values, such that each contiguous area
	 *               of a number is considered to be a single shape.
	 * @param interval Controls the density of beziers (higher number means
	 *                   fewer bezier curves). MUST be at least 1 (10 recommended).
	 * @return Returns a list of <code>BezierShape</code> objects, each representing
	 * one shape from the raster. The order is important: the shapes should be drawn
	 * in the order such that the first index is in the back and the last index is
	 * in the front.
	 * @throws IllegalArgumentException Thrown if any of the input arguments are
	 * invalid
	 */
	public List<BezierShape> traceAllShapes(final IntMap bitmap, final int interval) throws IllegalArgumentException {
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
					var circumference = followEdge(bitmap, x, y);
					var vectorized = traceClosedPath(circumference, interval);
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
