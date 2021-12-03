package net.plantabyte.drptrace;

import net.plantabyte.drptrace.geometry.*;

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
	public List<BezierCurve> traceClosedPath(Vec2[] pathPoints, int smoothness)
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
		var beziers = new ArrayList<BezierCurve>(numBeziers);
		
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
	public List<BezierCurve> traceOpenPath(Vec2[] pathPoints, int smoothness)
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
		var beziers = new ArrayList<BezierCurve>(numBeziers);
		
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
	
	public Collection<List<BezierCurve>> traceAllShapes(final IntMap bitmap, final int smoothness) {
		// first, find a patch of 1's in the bitmap
		final int w = bitmap.getWidth(), h = bitmap.getHeight();
		var searchedMap = new ZOrderBinaryMap(w, h);
		for(int y = 0; y < h; y++){
			for(int x = 0; x < w; x++){
				if(searchedMap.get(x, y) == 0){ // pixel not yet searched
					// algorithm: flood fill (both patches of 0's and 1's), and for
					// each flood fill, trace the outer edge
				}
				searchedMap.set(x, y, (byte)1);
			}
		}
		throw new UnsupportedOperationException("WIP");
	}
	
	public List<Vec2> followEdge(final IntMap source, final int x, final int y){
		// trace counter-clockwise around the edge
		final int color = source.get(x,y);
		final var pointPath = new LinkedList<Vec2>();
		// first move up to top edge
		int x2 = x, y2 = y;
		do{
			y2++;
		} while(source.isInRange(x2, y2) && source.get(x2,y2) == color);
		var pos = new Vec2i(x2, y2-1);
		var startPos = pos;
		var lastPos = pos.down();
		do {
			System.out.println("current: "+pos+"\tlast: "+lastPos); // TODO: remove
			// get neighbors in counterclockwise order
			var neighbors = neighborsCounterClockwise(pos, lastPos);
			/// index 3 is lastPos
			int nextSame = -1;
			boolean offColorDetected = false;
			for(int i = 0; i < 4; i++){
				int c = source.isInRange(neighbors[i].x,neighbors[i].y) ? source.get(neighbors[i].x,neighbors[i].y) : ~color;
				if(c != color){
					var mp = midPoint(pos, neighbors[i]);
					pointPath.add(mp);
					System.out.println("\t"+mp); // TODO: remove
					offColorDetected = true;
				} else if(offColorDetected & nextSame < 0) {
					nextSame = i;
				}
			}
			// move to next pos
			if(nextSame < 0){
				// no neighbors!
				break;
			}
			lastPos = pos;
			pos = neighbors[nextSame];
		}while(!pos.equals(startPos)); // repeat until we loop back to start
		// TODO: test this function
		return new ArrayList<>(pointPath); // convert to array list for better performance downstream
	}
	private static void floodFill(final IntMap source, final ZOrderBinaryMap searchedMap, final int x, final int y){
		final int color = source.get(x,y);
		throw new UnsupportedOperationException("WIP");
	}
	
	/**
	 * Gets up, left, down, right neighbors in counter-clockwise order, ending at
	 * <code>end</code>
	 * @param center The point to rotate around
	 * @param end Where the counter-clockwise sequence should end
	 * @return An array of up, left, down, right neighbors, where the last index
	 * (3) is equal to end
	 */
	private static Vec2i[] neighborsCounterClockwise(Vec2i center, Vec2i end){
		Vec2i[] cc = new Vec2i[4];
		Vec2i[] out = new Vec2i[4];
		cc[0] = center.up();
		cc[1] = center.left();
		cc[2] = center.down();
		cc[3] = center.right();
		int i = 0;
		for(i = 0; i < 3; i++){
			if(cc[i].equals(end)) break;
		}
		for(int n = 0; n < 4; n++){
			out[n] = cc[(n+i+1)%4];
		}
		return out;
	}
	
	private static Vec2 midPoint(Vec2i a, Vec2i b){
		return new Vec2(0.5*(a.x+b.x), 0.5*(a.y+b.y));
	}
}