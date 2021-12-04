package net.plantabyte.drptrace.trace;

import net.plantabyte.drptrace.geometry.Vec2;
import net.plantabyte.drptrace.geometry.Vec2i;

import java.util.Objects;

/**
 * A <code>Corner</code> represents the intersections of a pixel grid. Each
 * <code>Corner</code> holds four pixel coordinates that form a square around the
 * center of the <code>Corner</code> object.
 */
public class Corner{
	/** coordinate of the top-left corner */
	public final Vec2i topLeft;
	/** coordinate of the top-right corner */
	public final Vec2i topRight;
	/** Pixel coordinate of the bottom-left corner */
	public final Vec2i bottomLeft;
	/** Pixel coordinate of the bottom-right corner */
	public final Vec2i bottomRight;
	/** Pixel coordinate of the intersection at the center of this <code>Corner</code> */
	public final Vec2 center;
	
	/**
	 * Constructor
	 * @param topLeft The top-left corner of this <code>Corner</code> (the other
	 *                coords will be generated automatically)
	 */
	public Corner(Vec2i topLeft){
		this(topLeft, topLeft.right(), topLeft.down(), topLeft.down().right());
	}
	private Corner(Vec2i topLeft, Vec2i topRight, Vec2i bottomLeft, Vec2i bottomRight){
		this.topLeft = topLeft;
		this.topRight = topRight;
		this.bottomLeft = bottomLeft;
		this.bottomRight = bottomRight;
		this.center = midPoint(topLeft, bottomRight);
	}
	
	/**
	 * Returns a new <code>Corner</code> that is shifted left by one intersection
	 * @return a new <code>Corner</code> instance
	 */
	public Corner left(){
		return new Corner(this.topLeft.left(), this.topLeft, this.bottomLeft.left(), this.bottomLeft);
	}
	
	/**
	 * Returns a new <code>Corner</code> that is shifted right by one intersection
	 * @return a new <code>Corner</code> instance
	 */
	public Corner right(){
		return new Corner(this.topRight, this.topRight.right(), this.bottomRight, this.bottomRight.right());
	}
	
	/**
	 * Returns a new <code>Corner</code> that is shifted up by one intersection
	 * @return a new <code>Corner</code> instance
	 */
	public Corner up(){
		return new Corner(this.topLeft.up(), this.topRight.up(), this.topLeft, this.topRight);
	}
	
	/**
	 * Returns a new <code>Corner</code> that is shifted down by one intersection
	 * @return a new <code>Corner</code> instance
	 */
	public Corner down(){
		return new Corner(this.bottomLeft, this.bottomRight, this.bottomLeft.down(), this.bottomRight.down());
	}
	
	/**
	 * Returns a new <code>Corner</code> that is shifted by one intersection in
	 * the indicated distance.
	 * @param dir direction to move
	 * @return a new <code>Corner</code> instance
	 */
	public Corner move(Dir dir){
		switch(dir){
			case UP: return up();
			case DOWN: return down();
			case LEFT: return left();
			case RIGHT: return right();
			default: return this;
		}
	}
	
	/**
	 * Identifies the direction of movement from a previous <code>Corner</code>
	 * to this one
	 * @param origin previous <code>Corner</code> that is either up, down, left,
	 *               or right from this one
	 * @return returns the direction of motion from the origin to this instance
	 */
	public Dir dirFrom(Corner origin){
		if(this.topLeft.y > origin.topLeft.y){
			return Dir.UP;
		} else if(this.topLeft.y < origin.topLeft.y){
			return Dir.DOWN;
		} else if(this.topLeft.x > origin.topLeft.x){
			return Dir.RIGHT;
		} else if(this.topLeft.x < origin.topLeft.x){
			return Dir.LEFT;
		} else {
			return Dir.NONE;
		}
	}
	
	/**
	 * Gets the coordinate halfway between this <code>Corner</code> and another
	 * one
	 * @param other another <code>Corner</code>
	 * @return A Vec2 holding the midpoint between the two centers
	 */
	public Vec2 midpoint(Corner other) {
		return this.center.add(other.center).mul(0.5);
	}
	
	/**
	 * Check equality to another object
	 * @param o an object
	 * @return Returns true if <code>o</code> is a <code>Corner</code> with
	 * identical coordinates
	 */
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
	
	/**
	 * HashCode implementation to go with <code>equals(...)</code>
	 * @return a hash code
	 */
	@Override
	public int hashCode() {
		return Objects.hash(topLeft, topRight, bottomLeft, bottomRight);
	}
	
	
	private static Vec2 midPoint(Vec2i a, Vec2i b){
		return new Vec2(0.5*(a.x+b.x), 0.5*(a.y+b.y));
	}
}
