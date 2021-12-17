/*
MIT License

Copyright (c) 2021 Dr. Christopher C. Hall, aka DrPlantabyte

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package net.plantabyte.drptrace;

import net.plantabyte.drptrace.geometry.*;
import net.plantabyte.drptrace.intmaps.ZOrderBinaryMap;
import net.plantabyte.drptrace.trace.*;

import java.util.*;

/**
 * The Tracer class provides methods for turning a series of points into a
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
 * Note that the "smoothness" parameter is used to adjust the density of bezier
 * curve nodes, with a higher number resulting in fewer nodes. 10 is usually a
 * good value to use.
 */
public class Tracer {
	/**
	 * Traces a series of points as a sequence of bezier curves, looping back to
	 * the beginning to form a closed loop. The density of bezier curves is
	 * controlled by the <code>smoothness</code> score. Specifically, the
	 * smoothness number is the ratio of provided path points to the number of
	 * beziers. For example, a smoothness of 10 means that there will be 1 bezier
	 * for every 10 path points.
	 * @param pathPoints A series of points to trace with bezier curves. MUST
	 *                   contain at least 3 points
	 * @param smoothness Controls the density of beziers (higher number means
	 *                   fewer bezier curves). MUST be at least 1 (10 recommended).
	 * @return Returns a list of <code>BezierCurve</code>s tracing the path of
	 * the points
	 * @throws IllegalArgumentException Thrown if any of the input arguments are
	 * invalid
	 */
	public BezierShape traceClosedPath(Vec2[] pathPoints, int smoothness)
			throws IllegalArgumentException {
		return tracePath(pathPoints, smoothness, true);
	}
	
	/**
	 * Traces a series of points as a sequence of bezier curves. The density of
	 * bezier curves is controlled by the <code>smoothness</code> score.
	 * Specifically, the smoothness number is the ratio of provided path points
	 * beziers. For example, a smoothness of 10 means that there will be 1 bezier
	 * for every 10 path points.
	 * @param pathPoints A series of points to trace with bezier curves. MUST
	 *                   contain at least 2 points
	 * @param smoothness Controls the density of beziers (higher number means
	 *                   fewer bezier curves). MUST be at least 1 (10 recommended).
	 * @return Returns a list of <code>BezierCurve</code>s tracing the path of
	 * the points
	 * @throws IllegalArgumentException Thrown if any of the input arguments are
	 * invalid
	 */
	public BezierShape traceOpenPath(Vec2[] pathPoints, int smoothness)
			throws IllegalArgumentException {
		return tracePath(pathPoints, smoothness, false);
	}

	private BezierShape tracePath(Vec2[] pathPoints, int smoothness, boolean closedLoop)
			throws IllegalArgumentException{
		//
		final int min_beziers = closedLoop ? 2 : 1;
		final int min_pts = closedLoop ? 3 : 2;
		final int e_offset = closedLoop ? 0 : -1;
		if(pathPoints.length < 3){
			throw new IllegalArgumentException(String.format("Must have at least %s points to trace %s path",
					min_pts, closedLoop ? "closed" : "open"));
		}
		if(smoothness < 1){
			throw new IllegalArgumentException("Smoothness must be a positive number");
		}
		final int numPoints = pathPoints.length;
		final int numBeziers = Math.max(numPoints / smoothness, min_beziers);
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
	 * The density of bezier curves is controlled by the <code>smoothness</code>
	 * score. Specifically, the smoothness number is the ratio of provided path
	 * points beziers. For example, a smoothness of 10 means that there will be 1
	 * bezier for every 10 path points.
	 * @param bitmap A 2D array of integer values, such that each contiguous area
	 *               of a number is considered to be a single shape.
	 * @param smoothness Controls the density of beziers (higher number means
	 *                   fewer bezier curves). MUST be at least 1 (10 recommended).
	 * @return Returns a list of <code>BezierShape</code> objects, each representing
	 * one shape from the raster. The order is important: the shapes should be drawn
	 * in the order such that the first index is in the back and the last index is
	 * in the front.
	 * @throws IllegalArgumentException Thrown if any of the input arguments are
	 * invalid
	 */
	public List<BezierShape> traceAllShapes(final IntMap bitmap, final int smoothness) throws IllegalArgumentException {
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
					var vectorized = traceClosedPath(circumference, smoothness);
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