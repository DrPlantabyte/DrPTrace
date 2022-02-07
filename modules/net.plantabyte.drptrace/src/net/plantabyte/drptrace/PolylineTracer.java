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
		final int min_pts = 3;
		if(pathPoints.length < min_pts){
			throw new IllegalArgumentException(String.format("Must have at least %s points to trace %s path",
					min_pts, closedLoop ? "closed" : "open"));
		}

		throw new UnsupportedOperationException("Not implemented yet!");
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
		throw new UnsupportedOperationException("Not implemented yet!");
	}

}
