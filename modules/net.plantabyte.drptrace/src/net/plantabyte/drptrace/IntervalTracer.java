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
public class IntervalTracer extends Tracer {

	private int interval;

	/**
	 * Constructs a new <code>IntervalTracer</code> with the given precision interval. The
	 * 	interval number is the ratio of provided path points to the number of
	 * 	beziers. For example, a interval of 10 means that there will be 1 bezier
	 * 	for every 10 path points.
	 * @param interval Controls the density of beziers (higher number means
	 * 	                  fewer bezier curves). MUST be at least 1 (10 recommended for
	 * 	                  relatively small or detailed rasters, 50+ for large rasters).
	 * @throws IllegalArgumentException Thrown if any of the input arguments are
	 * invalid
	 */
	public IntervalTracer(int interval){
		if(interval <= 1) throw new IllegalArgumentException(String.format("Invalid interval score: %s (must be greator than 1)", interval));
		this.interval = interval;
	}
	/**
	 * Traces a series of points as a sequence of bezier curves, looping back to
	 * the beginning to form a closed loop if <code>closedLoop</code> is true.
	 * The density of bezier curves is controlled by the <code>interval</code>
	 * score that was set in the constructor. Specifically, the
	 * interval number is the ratio of provided path points to the number of
	 * beziers. For example, a interval of 10 means that there will be 1 bezier
	 * for every 10 path points.
	 * @param pathPoints A series of points to trace with bezier curves. MUST
	 *                   contain at least 3 points for a closed loop or 2 points
	 *                   for an open trace
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


}
