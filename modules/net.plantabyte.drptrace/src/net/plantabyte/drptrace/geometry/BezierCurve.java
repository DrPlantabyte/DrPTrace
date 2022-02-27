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

import static net.plantabyte.drptrace.math.Util.RMSE;

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
	 * Scales this bezier curve by multiplying each point by the given scalar value. This will also translate the
	 * position of the bezier curve.
	 * @param scalar A scaling factor
	 * @return A new scaled bezier curve
	 */
	public BezierCurve scale(double scalar){
		return new BezierCurve(p[0].mul(scalar), p[1].mul(scalar), p[2].mul(scalar), p[3].mul(scalar));
	}

	/**
	 * Scales this bezier curve by multiplying each point by the given scalar value, keeping it centered relative to the
	 * given center point (eg the center of a shape made of multiple bezier curves)
	 * @param scalar A scaling factor
	 * @param center The center point around which this bezier should be scaled
	 * @return A new scaled bezier curve
	 */
	public BezierCurve scaleAroundPoint(double scalar, Vec2 center){
		return new BezierCurve(
				p[0].sub(center).mul(scalar).add(center),
				p[1].sub(center).mul(scalar).add(center),
				p[2].sub(center).mul(scalar).add(center),
				p[3].sub(center).mul(scalar).add(center)
		);
	}
	
	/**
	 * Translates this <code>BezierCurve</code> in the direction specified by the
	 * given Vec2
	 * @param translation X-Y vector
	 * @return A new <code>BezierCurve</code> that is translated relatvie to this one
	 */
	public BezierCurve translate(final Vec2 translation) {
		return new BezierCurve(
				p[0].add(translation),
				p[1].add(translation),
				p[2].add(translation),
				p[3].add(translation)
		);
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
		fitToPoints(pathPoints, 0, pathPoints.length);
	}

	/**
	 *
	 * Adjusts the control points of this instance to fit to the provided point
	 * path.
	 * @param pathPoints Series of points outlining the desired path from P1 to P4 (inclusive of P1 and P4)
	 * @param startIndex Index at start of range within <code>pathPoints</code>
	 * @param length length of range within <code>pathPoints</code>
	 */
	public void fitToPoints(final Vec2[] pathPoints, int startIndex, int length) {
		final int limit = startIndex + length;;
		if(length == 0){
			// nothing at all
			return;
		}else if(length <= 2){
			// no fitting, line segment
			p[1] = pathPoints[startIndex];
			p[2] = pathPoints[limit-1];
			return;
		}
		// check for straight lines
		var origin = pathPoints[startIndex];
		var endPoint = pathPoints[limit-1];
		var line = endPoint.sub(origin);
		var theta = Math.atan2(line.y, line.x);
		final double tolerance = 0.03125;
		boolean isLine = true;
		for(int i = startIndex+1; i < limit; i++){
			var L = pathPoints[i].sub(origin);
			if(Math.abs(Math.atan2(L.y, L.x) - theta) > tolerance){
				isLine = false;
				break;
			}
		}
		if(isLine){
			p[1] = pathPoints[startIndex];
			p[2] = pathPoints[limit-1];
			return;
		}
		// setup for using a function solver
		double[] paramArray = {p[1].x, p[1].y, p[2].x, p[2].y};
		Function<double[], Double> optiFunc = (double[] params) -> RMSE(
				new BezierCurve(this.getP1(), new Vec2(params[0], params[1]), new Vec2(params[2], params[3]), this.getP4()),
				pathPoints, startIndex, length
		);
				//+ (this.getP1().distSquared(this.getP2()) + this.getP4().distSquared(this.getP3())) / (this.getP1().distSquared(this.getP4())); // add bias against long control handles
		Solver solver = new HillClimbSolver(0.1, 10000);
		double[] optimizedArray = solver.minimize(optiFunc, paramArray);
		this.p[1] = new Vec2(optimizedArray[0], optimizedArray[1]);
		this.p[2] = new Vec2(optimizedArray[2], optimizedArray[3]);
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
	
	/**
	 * Checks if all points of this <code>BezierCurve</code> are within the given
	 * bounding box.
	 * @param p1 one corner of the bounding box
	 * @param p2 the opposite corner of the bounding box
	 * @return true if all points of this <code>BezierCurve</code> are within the
	 * given bounding box, false otherwise
	 */
	public boolean withinBoundingBox(final Vec2 p1, final Vec2 p2) {
		final double xmin = Math.min(p1.x, p2.x);
		final double ymin = Math.min(p1.y, p2.y);
		final double xmax = Math.max(p1.x, p2.x);
		final double ymax = Math.max(p1.y, p2.y);
		for(var pt : p){
			if(pt.x < xmin || pt.x > xmax || pt.y < ymin || pt.y > ymax)  return false;
		}
		return true;
	}
}
