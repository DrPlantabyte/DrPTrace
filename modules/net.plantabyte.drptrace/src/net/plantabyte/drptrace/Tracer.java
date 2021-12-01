package net.plantabyte.drptrace;

import net.plantabyte.drptrace.geometry.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
	
	public Collection<List<BezierCurve>> traceAllShapes(final Bitmap bitmap) {
		// first, find a patch of 1's in the bitmap
		final int w = bitmap.getWidth(), h = bitmap.getHeight();
		var searchedMap = new ByteArrayBitmap(w, h); // TODO: optimize with zorder bitmap
		for(int y = 0; y < h; y++){
			for(int x = 0; x < w; x++){
				if(searchedMap.get(x, y) == 0 && bitmap.get(x, y) != 0){
					// found a shape!
					// recursively trace it and any nested voids/shapes
					
				}
				searchedMap.set(x, y, (byte)1);
			}
		}
		throw new UnsupportedOperationException("WIP");
	}
	
	
}