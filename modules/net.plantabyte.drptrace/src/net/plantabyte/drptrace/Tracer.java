package net.plantabyte.drptrace;

import net.plantabyte.drptrace.geometry.*;
import net.plantabyte.drptrace.trace.*;

import java.util.*;

/**
 * The Tracer class provides methods for turning a series of points into a
 * sequence of bezier curves tracing that path. If tracing a shape, use
 * <code>traceClosedPath(Vec2[], int)</code>; if tracing a line, use
 * <code>traceOpenPath(Vec2[], int)</code>.
 */
public class Tracer {
	/**
	 * Traces a series of points as a sequence of bezier curves, looping back to
	 * the beginning to form a closed loop. The density of bezier curves is
	 * controlled by the <code>smoothness</code> score. Specifically, the
	 * smoothness number is the ratio of provided path points to the number of
	 * beziers. For example, a smoothness of 5 means that there will be 1 bezier
	 * for every 5 path points.
	 * @param pathPoints A series of points to trace with bezier curves. MUST
	 *                   contain at least 3 points
	 * @param smoothness Controls the density of beziers (higher number means
	 *                   fewer bezier curves). MUST be at least 1.
	 * @return Returns a list of <code>BezierCurve</code>s tracing the path of
	 * the points
	 * @throws IllegalArgumentException Thrown if any of the input arguments are
	 * invalid
	 */
	public BezierSeries traceClosedPath(Vec2[] pathPoints, int smoothness)
			throws IllegalArgumentException {
		if(pathPoints.length < 3){
			throw new IllegalArgumentException("Must have at least 3 points to trace closed path");
		}
		if(smoothness < 1){
			throw new IllegalArgumentException("Smoothness must be a positive number");
		}
		final int numPoints = pathPoints.length;
		final int numBeziers = Math.max(numPoints / smoothness, 2);
		final int intervalSize = numPoints/numBeziers + 1; // last interval may be a different size
		var beziers = new BezierSeries(numBeziers);
		
		int start = 0;
		for(int c = 0; c < numBeziers && start < numPoints; c++){
			int end = Math.min(start + intervalSize, numPoints); // start and end are both inclusive
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
		return beziers;
	}
	
	/**
	 * Traces a series of points as a sequence of bezier curves. The density of
	 * bezier curves is controlled by the <code>smoothness</code> score.
	 * Specifically, the smoothness number is the ratio of provided path points
	 * beziers. For example, a smoothness of 5 means that there will be 1 bezier
	 * for every 5 path points.
	 * @param pathPoints A series of points to trace with bezier curves. MUST
	 *                   contain at least 2 points
	 * @param smoothness Controls the density of beziers (higher number means
	 *                   fewer bezier curves). MUST be at least 1.
	 * @return Returns a list of <code>BezierCurve</code>s tracing the path of
	 * the points
	 * @throws IllegalArgumentException Thrown if any of the input arguments are
	 * invalid
	 */
	public BezierSeries traceOpenPath(Vec2[] pathPoints, int smoothness)
			throws IllegalArgumentException {
		if(pathPoints.length < 2){
			throw new IllegalArgumentException("Must have at least 2 points to trace open path");
		}
		if(smoothness < 1){
			throw new IllegalArgumentException("Smoothness must be a positive number");
		}
		final int numPoints = pathPoints.length;
		final int numBeziers = Math.max(numPoints / smoothness, 1);
		final int intervalSize = numPoints/numBeziers + 1; // last interval may be a different size
		var beziers = new BezierSeries(numBeziers);
		
		int start = 0;
		for(int c = 0; c < numBeziers && start < numPoints; c++){
			int end = Math.min(start + intervalSize, numPoints-1); // start and end are both inclusive
			// note: exclude end points from fitting
			final Vec2[] buffer;
			if(end - start < 3){
				beziers.add(new BezierCurve(pathPoints[start], pathPoints[end]));
			} else {
				buffer = new Vec2[end - start - 2];
				System.arraycopy(pathPoints, start + 1, buffer, 0, buffer.length);
				var b = new BezierCurve(pathPoints[start], buffer[0],
						buffer[buffer.length - 1], pathPoints[end]
				);
				b.fitToPoints(buffer);
				beziers.add(b);
			}
			start = end;
		}
		return beziers;
	}
	
	public Map<Integer, List<BezierSeries>> traceAllShapes(final IntMap bitmap, final int smoothness) {
		final int w = bitmap.getWidth(), h = bitmap.getHeight();
		var searchedMap = new ZOrderBinaryMap(w, h);
		Map<Integer, List<BezierSeries>> output = new HashMap<>();
		// algorithm: flood fill each patch, and for
		// each flood fill, trace the outer edge
		for(int y = 0; y < h; y++){
			for(int x = 0; x < w; x++){
				if(searchedMap.get(x, y) == 0){ // pixel not yet searched
					// first, find a patch of color in the bitmap
					int color = bitmap.get(x, y);
					// next, trace the outer perimeter of the color patch
					var circumference = followEdge(bitmap, x, y);
					var vectorized = traceClosedPath(circumference, smoothness);
					output.computeIfAbsent(color, (Integer k)-> new LinkedList<>());
					output.get(color).add(vectorized);
					// finally, flood-fill the patch in the searched map
					floodFill(bitmap, searchedMap, x, y);
					searchedMap.set(x, y, (byte)1);
				}
			}
		}
		return output;
	}
	
	private Vec2[] followEdge(final IntMap source, final int x, final int y){
		// trace counter-clockwise around the edge
		final int color = source.get(x,y);
		final var pointPath = new LinkedList<Vec2>();
		TraceMachine m = new TraceMachine(source, new Vec2i(x, y), color, pointPath);
		do{
			m.step();
		}while(!m.done());
		
		return pointPath.toArray(new Vec2[pointPath.size()]); // convert to array for better performance downstream
	}
	private static void floodFill(final IntMap source, final ZOrderBinaryMap searchedMap, final int x, final int y){
		final int color = source.get(x,y);
		final var Q = new LinkedList<Vec2i>();
		Q.push(new Vec2i(x, y));
		while(Q.size() > 0){
			var pop = Q.pop();
			searchedMap.set(pop.x, pop.y, (byte)1);
			Vec2i[] neighbors = {pop.up(), pop.left(), pop.down(), pop.right()};
			for(var n : neighbors){
				if(source.isInRange(n.x, n.y) && source.get(n.x, n.y) == color && searchedMap.get(n.x, n.y) == 0){
					// n is same color and not yet searched
					Q.push(n);
				}
			}
		}
	}
	
}