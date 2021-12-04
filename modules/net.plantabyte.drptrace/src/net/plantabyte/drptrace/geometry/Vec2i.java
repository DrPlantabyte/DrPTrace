package net.plantabyte.drptrace.geometry;

/**
 * Immutagble 2D point data class specialized for index coordinates
 */
public final class Vec2i {
	/** X value */
	public final int x;
	/** Y value */
	public final int y;
	
	/**
	 * Standard constructor
	 * @param x X value
	 * @param y Y value
	 */
	public Vec2i(int x, int y){
		this.x = x;
		this.y = y;
	}
	/** origin point */
	public static final Vec2i ORIGIN = new Vec2i(0,0);
	
	/**
	 * Creates a string representation of the coordinates
	 * @return the coordinates as a sctring
	 */
	@Override public String toString(){
		return String.format("(%d, %d)", x, y);
	}
	
	/**
	 * Multiplies this Vec2 by a scalar value
	 * @param scalar value to multiply by
	 * @return Multiplied Vec2 result
	 */
	public Vec2i mul(int scalar){
		return new Vec2i(this.x * scalar, this.y * scalar);
	}
	
	
	/**
	 * Multiplies this Vec2 by a scalar value
	 * @param scalar value to multiply by
	 * @return Multiplied Vec2 result
	 */
	public Vec2i mul(double scalar){
		return new Vec2i((int)(this.x * scalar), (int)(this.y * scalar));
	}
	
	/**
	 * Adds Vec2 v to this Vec2
	 * @param v Vec2 to add to this one
	 * @return Result of adding the two vectors
	 */
	public Vec2i add(Vec2i v){
		return new Vec2i(this.x + v.x, this.y+v.y);
	}
	
	/**
	 * Subtract Vec2 v from this Vec2
	 * @param v Vec2 to subtract from this one
	 * @return Result of subtracting the two vectors
	 */
	public Vec2i sub(Vec2i v){
		return new Vec2i(this.x - v.x, this.y-v.y);
	}
	
	/**
	 * Returns the squared distance from this Vec2 to v
	 * @param v other Vec2 point
	 * @return distance squared
	 */
	public double distSquared(Vec2i v){
		double dx = v.x - this.x;
		double dy = v.y - this.y;
		return dx*dx + dy*dy;
	}
	/**
	 * Returns the distance from this Vec2 to v
	 * @param v other Vec2 point
	 * @return distance
	 */
	public double dist(Vec2i v){
		return Math.sqrt(distSquared(v));
	}
	
	/**
	 * Checks equality with another Vec2
	 * @param other Other object
	 * @return Returns tru if this Vec2 has same x and y coordinates as other
	 * Vec2 (returns false if other is not a Vec2)
	 */
	@Override public boolean equals(Object other){
		return other == this || (other instanceof Vec2i && this.x == ((Vec2i)other).x && this.y == ((Vec2i)other).y );
	}
	
	/**
	 * Generates a hash code
	 * @return a hash code
	 */
	@Override public int hashCode(){
		return 2003 * Integer.hashCode(this.y) + 1999 * Integer.hashCode(this.x);
	}
	
	/**
	 * Returns a new Vec2i that is shifted up by the specified amount
	 * @param dist distance to move up
	 * @return a new Vec2i
	 */
	public Vec2i up(int dist){
		return new Vec2i(this.x, this.y+dist);
	}
	
	/**
	 * Returns a new Vec2i that is shifted up by 1
	 * @return a new Vec2i
	 */
	public Vec2i up(){
		return new Vec2i(this.x, this.y+1);
	}
	
	/**
	 * Returns a new Vec2i that is shifted left by the specified amount
	 * @param dist distance to move left
	 * @return a new Vec2i
	 */
	public Vec2i left(int dist){
		return new Vec2i(this.x-dist, this.y);
	}
	
	/**
	 * Returns a new Vec2i that is shifted left by 1
	 * @return a new Vec2i
	 */
	public Vec2i left(){
		return new Vec2i(this.x-1, this.y);
	}
	
	/**
	 * Returns a new Vec2i that is shifted right by the specified amount
	 * @param dist distance to move right
	 * @return a new Vec2i
	 */
	public Vec2i right(int dist){
		return new Vec2i(this.x+dist, this.y);
	}
	
	/**
	 * Returns a new Vec2i that is shifted right by 1
	 * @return a new Vec2i
	 */
	public Vec2i right(){
		return new Vec2i(this.x+1, this.y);
	}
	/**
	 * Returns a new Vec2i that is shifted down by the specified amount
	 * @param dist distance to move down
	 * @return a new Vec2i
	 */
	public Vec2i down(int dist){
		return new Vec2i(this.x, this.y-dist);
	}
	
	/**
	 * Returns a new Vec2i that is shifted down by 1
	 * @return a new Vec2i
	 */
	public Vec2i down(){
		return new Vec2i(this.x, this.y-1);
	}
}
