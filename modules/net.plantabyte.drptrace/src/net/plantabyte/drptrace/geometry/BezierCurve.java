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
package net.plantabyte.drptrace.geometry;

import net.plantabyte.drptrace.math.*;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * This class represents a single bezier curve
 */
public final class BezierCurve {
	private Vec2[] p = new Vec2[4];
	
	/**
	 * Standard constructor for cubic bezier curve
	 * @param origin point 1
	 * @param ctrl1 point 2
	 * @param ctrl2 point 3
	 * @param dest point 4
	 */
	public BezierCurve(Vec2 origin, Vec2 ctrl1, Vec2 ctrl2, Vec2 dest){
		p[0] = origin;
		p[1] = ctrl1;
		p[2] = ctrl2;
		p[3] = dest;
	}
	
	/**
	 * Constructor for a line segment
	 * @param origin point 1
	 * @param dest point 2
	 */
	public BezierCurve(Vec2 origin, Vec2 dest){
		p[0] = origin;
		p[1] = origin;
		p[2] = dest;
		p[3] = dest;
	}
	
	/**
	 * Creates a copy
	 * @return a deep-copy duplicate of this object
	 */
	public BezierCurve clone(){
		return new BezierCurve(p[0], p[1], p[2], p[3]);
	}
	
	/**
	 * Gets the first end point
	 * @return A 2D point
	 */
	public Vec2 getP1(){
		return p[0];
	}
	
	/**
	 * Gets the first control point
	 * @return A 2D point
	 */
	public Vec2 getP2(){
		return p[1];
	}
	
	/**
	 * Gets the second control point
	 * @return A 2D point
	 */
	public Vec2 getP3(){
		return p[2];
	}
	
	/**
	 * Gets the second end point
	 * @return A 2D point
	 */
	public Vec2 getP4(){
		return p[3];
	}
	
	/**
	 * Computes bezier curve coordinate as a function of t, where t ranged from 0 to 1
	 * @param t double from 0 to 1
	 * @return The point at f(t) along this bezier curve
	 */
	public Vec2 f(double t){
		double x = cube(1-t)*p[0].x + 3*square(1-t)*t*p[1].x + 3*(1-t)*square(t)*p[2].x + cube(t)*p[3].x;
		double y = cube(1-t)*p[0].y + 3*square(1-t)*t*p[1].y + 3*(1-t)*square(t)*p[2].y + cube(t)*p[3].y;
		return new Vec2(x, y);
	}
	
	/**
	 * Generates a series of points along the bezier curve
	 * @param numPoints Number of points to create (min 2)
	 * @return Array of points
	 */
	public Vec2[] makePoints(final int numPoints){
		Vec2[] output = new Vec2[numPoints];
		final double tick = 1.0 / (double)numPoints;
		output[0] = f(0);
		for(int i = 1; i < numPoints-1; i++){
			output[i] = f(i*tick);
		}
		output[numPoints-1] = f(1.0);
		return output;
	}
	private static double cube(double x){
		return x*x*x;
	}
	private static double square(double x){
		return x*x;
	}
	
	/**
	 * Adjusts the control points of this instance to fit to the provided point
	 * path.
	 * @param pathPoints Series of points outlining the desired path from P1 to P4
	 */
	public void fitToPoints(final List<Vec2> pathPoints) {
		this.fitToPoints(pathPoints.toArray(new Vec2[pathPoints.size()]));
	}
	/**
	 * Adjusts the control points of this instance to fit to the provided point
	 * path.
	 * @param pathPoints Series of points outlining the desired path from P1 to P4
	 */
	public void fitToPoints(final Vec2[] pathPoints) {
		if(pathPoints.length == 0){
			// nothing at all
			return;
		}else if(pathPoints.length <= 2){
			// no fitting, line segment
			p[1] = pathPoints[0];
			p[2] = pathPoints[pathPoints.length-1];
			return;
		}
		// check for straight lines
		var origin = pathPoints[0];
		var endPoint = pathPoints[pathPoints.length-1];
		var line = endPoint.sub(origin);
		var theta = Math.atan2(line.y, line.x);
		final double tolerance = 0.03125;
		boolean isLine = true;
		for(int i = 1; i < pathPoints.length; i++){
			var L = pathPoints[i].sub(origin);
			if(Math.abs(Math.atan2(L.y, L.x) - theta) > tolerance){
				isLine = false;
				break;
			}
		}
		if(isLine){
			p[1] = pathPoints[0];
			p[2] = pathPoints[pathPoints.length-1];
			return;
		}
		// setup for using a function solver
		double[] paramArray = {p[1].x, p[1].y, p[2].x, p[2].y};
		Function<double[], Double> optiFunc = (double[] params) -> RMSE(
				new BezierCurve(this.getP1(), new Vec2(params[0], params[1]), new Vec2(params[2], params[3]), this.getP4()),
				pathPoints
		);
				//+ (this.getP1().distSquared(this.getP2()) + this.getP4().distSquared(this.getP3())) / (this.getP1().distSquared(this.getP4())); // add bias against long control handles
		Solver solver = new HillClimbSolver(0.1, 10000);
		double[] optimizedArray = solver.minimize(optiFunc, paramArray);
		this.p[1] = new Vec2(optimizedArray[0], optimizedArray[1]);
		this.p[2] = new Vec2(optimizedArray[2], optimizedArray[3]);
	}
	private static double RMSE(final BezierCurve b, final Vec2[] pathPoints){
		final int k = 16; // tune for balancing performance and accuracy
		double totalRSE = 0;
		final Vec2[] bPoints = b.makePoints(k);
		// RMSE points to bezier
		for(int i = 0; i < pathPoints.length; i++){
			var p = pathPoints[i];
			double RSE = Double.MAX_VALUE;
			// approximating bezier as line segments to get mean squared error
			// (lowest squared error of all line segments for each point)
			for(int s = 1; s < k; s++){
				var L1 = bPoints[s-1];
				var L2 = bPoints[s];
				double dist = Util.distFromPointToLineSegment(L1, L2, p);
				if(dist < RSE) {RSE = dist;}
			}
			totalRSE += RSE;
		}
		// RMSE bezier to points
		for(int i = 0; i < bPoints.length; i++){
			var p = bPoints[i];
			double RSE = Double.MAX_VALUE;
			for(int s = 1; s < pathPoints.length; s++){
				var L1 = pathPoints[s-1];
				var L2 = pathPoints[s];
				double dist = Util.distFromPointToLineSegment(L1, L2, p);
				if(dist < RSE) {RSE = dist;}
			}
			totalRSE += RSE;
		}
		return totalRSE / pathPoints.length;
	}
	
	/**
	 * Returns debug information
	 * @return Text useful for debugging
	 */
	@Override
	public String toString() {
		return String.format("BezierCurve:[%s -> %s -> %s -> %s]", p[0], p[1], p[2], p[3]);
	}
	
	/**
	 * Checks equality with another object
	 * @param o other object
	 * @return True iff <code>o</code> is a BezierCurve with identical points P1-P4.
	 */
	@Override
	public boolean equals(final Object o) {
		if(this == o) {
			return true;
		}
		if(o == null || getClass() != o.getClass()) {
			return false;
		}
		final BezierCurve that = (BezierCurve) o;
		return Arrays.equals(p, that.p);
	}
	
	/**
	 * HashCode implementation to go with <code>equals(...)</code>
	 * @return a hash code
	 */
	@Override
	public int hashCode() {
		return Arrays.hashCode(p);
	}
}
