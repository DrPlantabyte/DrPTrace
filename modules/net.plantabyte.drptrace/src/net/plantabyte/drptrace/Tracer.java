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
	
	private static class Corner{
		public final Vec2i topLeft;
		public final Vec2i topRight;
		public final Vec2i bottomLeft;
		public final Vec2i bottomRight;
		public Corner(Vec2i topLeft){
			this.topLeft = topLeft;
			this.topRight = topLeft.right();
			this.bottomLeft = topLeft.down();
			this.bottomRight = bottomLeft.right();
		}
		private Corner(Vec2i topLeft, Vec2i topRight, Vec2i bottomLeft, Vec2i bottomRight){
			this.topLeft = topLeft;
			this.topRight = topRight;
			this.bottomLeft = bottomLeft;
			this.bottomRight = bottomRight;
		}
		public Corner left(){
			return new Corner(this.topLeft.left(), this.topLeft, this.bottomLeft.left(), this.bottomLeft);
		}
		
		public Corner right(){
			return new Corner(this.topRight, this.topRight.right(), this.bottomRight, this.bottomRight.right());
		}
		
		public Corner up(){
			return new Corner(this.topLeft.up(), this.topRight.up(), this.topLeft, this.topRight);
		}
		
		public Corner down(){
			return new Corner(this.bottomLeft, this.bottomRight, this.bottomLeft.down(), this.bottomRight.down());
		}
		
		@Override
		public boolean equals(final Object o) {
			if(this == o) {
				return true;
			}
			if(o == null || getClass() != o.getClass()) {
				return false;
			}
			final Corner corner = (Corner) o;
			return topLeft.equals(corner.topLeft) && topRight.equals(
					corner.topRight) && bottomLeft.equals(corner.bottomLeft)
					&& bottomRight.equals(corner.bottomRight);
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(topLeft, topRight, bottomLeft, bottomRight);
		}
	}
	private static class TraceMachine{
		private final IntMap src;
		private Corner pos;
		private final Corner initialPos;
		private final int color;
		private final List<Vec2> midpoints;
		public TraceMachine(IntMap source, Vec2i topLeft, int color, List<Vec2> pointTacker){
			this.src = source;
			this.pos = new Corner(topLeft);
			this.initialPos = pos;
			this.color = color;
			this.midpoints = pointTacker;
		}
		private boolean isColor(Vec2i p){
			return src.isInRange(p.x, p.y) && src.get(p.x, p.y) == color;
		}
		public boolean done(){
			return this.pos.equals(initialPos);
		}
		public void step(){
			var oldPos = pos;
			if(isColor(pos.bottomLeft)){
				// ??
				// #?
				if(!isColor(pos.topLeft)){
					// .?
					// #?
					midpoints.add(midPoint(pos.topLeft, pos.bottomLeft));
					pos = pos.left();
				} else {
					// #?
					// #?
					if(!isColor(pos.topRight)){
						// #.
						// #?
						midpoints.add(midPoint(pos.topLeft, pos.topRight));
						pos = pos.up();
					} else {
						// ##
						// #?
						if(!isColor(pos.bottomRight)){
							// ##
							// #.
							midpoints.add(midPoint(pos.topRight, pos.bottomRight));
							pos = pos.right();
						} else {
							// ##
							// ##
							throw new IllegalStateException("Not on corner, all four are color");
						}
					}
				}
			} else {
				// ??
				// .?
				if(isColor(pos.bottomRight)){
					// ??
					// .#
					midpoints.add(midPoint(pos.bottomLeft, pos.bottomRight));
					pos = pos.down();
				} else {
					// ??
					// ..
					if(isColor(pos.topRight)){
						// ?#
						// ..
						midpoints.add(midPoint(pos.topRight, pos.bottomRight));
						pos = pos.right();
					} else {
						// ?.
						// ..
						if(isColor(pos.topLeft)){
							// #.
							// ..
							midpoints.add(midPoint(pos.topLeft, pos.topRight));
							pos = pos.up();
						} else {
							// ..
							// ..
							throw new IllegalStateException("Not on corner, all four are off-color");
						}
					}
				}
			}
			
		}
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
		// now follow edges around in counter-clockwise direction
		TraceMachine m = new TraceMachine(source, new Vec2i(x2, y2), color, pointPath);
		do{
			m.step();
		}while(!m.done());
		
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
	
	private static Vec2i nextNeighborCounterClockwise(Vec2i center, Vec2i from){
		Vec2i[] cc = new Vec2i[4];
		cc[0] = center.up();
		cc[1] = center.left();
		cc[2] = center.down();
		cc[3] = center.right();
		int i = 0;
		for(i = 0; i < 3; i++){
			if(cc[i].equals(from)) break;
		}
		return cc[(i+3)%4];
	}
	
	private static Vec2i nextNeighborClockwise(Vec2i center, Vec2i from){
		Vec2i[] cc = new Vec2i[4];
		cc[0] = center.up();
		cc[1] = center.right();
		cc[2] = center.down();
		cc[3] = center.left();
		int i = 0;
		for(i = 0; i < 3; i++){
			if(cc[i].equals(from)) break;
		}
		return cc[(i+3)%4];
	}
	
	private static Vec2 midPoint(Vec2i a, Vec2i b){
		return new Vec2(0.5*(a.x+b.x), 0.5*(a.y+b.y));
	}
}