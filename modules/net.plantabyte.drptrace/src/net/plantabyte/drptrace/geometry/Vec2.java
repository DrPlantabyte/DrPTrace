package net.plantabyte.drptrace.geometry;

public class Vec2 {
	/** X value */
	public final double x;
	/** Y value */
	public final double y;
	
	/**
	 * Standard constructor
	 * @param x X value
	 * @param y Y value
	 */
	public Vec2(double x, double y){
		this.x = x;
		this.y = y;
	}
	/** origin point */
	public static final Vec2 ORIGIN = new Vec2(0,0);
	
	@Override public String toString(){
		return String.format("(%f, %f)", x, y);
	}
	
	/**
	 * Multiplies this Vec2 by a scalar value
	 * @param scalar value to multiply by
	 * @return Multiplied Vec2 result
	 */
	public Vec2 mul(double scalar){
		return new Vec2(this.x * scalar, this.y * scalar);
	}
	
	/**
	 * Adds Vec2 v to this Vec2
	 * @param v Vec2 to add to this one
	 * @return Result of adding the two vectors
	 */
	public Vec2 add(Vec2 v){
		return new Vec2(this.x + v.x, this.y+v.y);
	}
	
	/**
	 * Returns the squared distance from this Vec2 to v
	 * @param v other Vec2 point
	 * @return distance squared
	 */
	public double distSquared(Vec2 v){
		return this.x*v.x + this.y*v.y;
	}
	/**
	 * Returns the distance from this Vec2 to v
	 * @param v other Vec2 point
	 * @return distance
	 */
	public double dist(Vec2 v){
		return Math.sqrt(distSquared(v));
	}
	
}
