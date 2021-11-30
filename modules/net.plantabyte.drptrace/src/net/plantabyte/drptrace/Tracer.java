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
			int end = Math.min(start + intervalSize, numPoints); // start and end are both inclusive
			// note: exclude end points from fitting
			final Vec2[] buffer;
			if(end - start < 3){
				beziers.add(new BezierCurve(pathPoints[start], pathPoints[end % numPoints]));
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
}