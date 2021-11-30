package net.plantabyte.drptrace;

import net.plantabyte.drptrace.geometry.BezierCurve;
import net.plantabyte.drptrace.geometry.Vec2;

import java.util.*;

public class Tracer {
	
	public List<BezierCurve> tracePointPath(Vec2[] pathPoints, int smoothness) {
		if(pathPoints.length < 3){
			throw new IllegalArgumentException("Must have at least 3 points to trace path");
		}
		final int numPoints = pathPoints.length;
		final int numBeziers = Math.max(numPoints / smoothness, 2);
		final int intervalSize = numPoints/numBeziers + 1; // last interval may be a different size
		var beziers = new ArrayList<BezierCurve>(numBeziers);
		
		var firstPoint = pathPoints[0];
		int start = 0;
		for(int c = 0; c < numBeziers; c++){
			int end = start + intervalSize + 1;
			final Vec2[] buffer;
			if(end >= numPoints && pathPoints[0] != pathPoints[numPoints-1]){
				// need to close the loop
				buffer = new Vec2[numPoints-start+1];
				System.arraycopy(pathPoints, start, buffer, 0, buffer.length-1);
				buffer[buffer.length-1] = firstPoint;
			} else {
				end = Math.min(end, numPoints);
				buffer = new Vec2[end-start];
				System.arraycopy(pathPoints, start, buffer, 0, buffer.length);
			}
			var b = new BezierCurve(buffer[0], buffer[1%buffer.length], buffer[(buffer.length*2-2)%buffer.length], buffer[buffer.length-1]);
			b.fitToPoints(buffer);
			beziers.add(b);
			start = end-1;
		}
		return beziers;
	}
}