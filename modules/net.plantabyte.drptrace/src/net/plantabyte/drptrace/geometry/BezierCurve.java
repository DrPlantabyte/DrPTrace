package net.plantabyte.drptrace.geometry;

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
	private static double cube(double x){
		return x*x*x;
	}
	private static double square(double x){
		return x*x;
	}
}
