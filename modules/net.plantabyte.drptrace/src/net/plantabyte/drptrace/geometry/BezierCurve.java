package net.plantabyte.drptrace.geometry;

import net.plantabyte.drptrace.math.*;

import java.util.List;
import java.util.function.Function;

public class BezierCurve {
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
	
	public BezierCurve clone(){
		return new BezierCurve(p[0], p[1], p[2], p[3]);
	}
	
	public Vec2 getP1(){
		return p[0];
	}
	public Vec2 getP2(){
		return p[1];
	}
	public Vec2 getP3(){
		return p[2];
	}
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
	
	public void fitToPoints(final List<Vec2> pathPoints) {
		this.fitToPoints(pathPoints.toArray(new Vec2[pathPoints.size()]));
	}
	public void fitToPoints(final Vec2[] pathPoints) {
		// setup for using a function solver
		double[] paramArray = {p[1].x, p[1].y, p[2].x, p[2].y};
		Function<double[], Double> optiFunc = (double[] params) -> -1*RMSE(
				new BezierCurve(this.getP1(), new Vec2(params[0], params[1]), new Vec2(params[2], params[3]), this.getP4()),
				pathPoints
		);
		Solver solver = new HillClimbSolver(0.1, 10000);
		double[] optimizedArray = solver.maximize(optiFunc, paramArray);
		this.p[1] = new Vec2(optimizedArray[0], optimizedArray[1]);
		this.p[2] = new Vec2(optimizedArray[2], optimizedArray[3]);
	}
	private static double RMSE(final BezierCurve b, final Vec2[] pathPoints){
		final int k = 16; // tune for balancing performance and accuracy
		double totalRSE = 0;
		final Vec2[] bPoints = b.makePoints(k);
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
		return totalRSE / pathPoints.length;
	}
}
