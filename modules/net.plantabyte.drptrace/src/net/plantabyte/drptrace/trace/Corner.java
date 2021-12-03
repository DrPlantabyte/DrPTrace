package net.plantabyte.drptrace.trace;

import net.plantabyte.drptrace.geometry.Vec2;
import net.plantabyte.drptrace.geometry.Vec2i;

import java.util.Objects;

public class Corner{
	public final Vec2i topLeft;
	public final Vec2i topRight;
	public final Vec2i bottomLeft;
	public final Vec2i bottomRight;
	public final Vec2 center;
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
	public Corner left(){
		return new Corner(this.topLeft.left(), this.topLeft, this.bottomLeft.left(), this.bottomLeft);
	}
	
	public Corner right(){
		return new Corner(this.topRight, this.topRight.right(), this.bottomRight, this.bottomRight.right());
	}
	
	public Corner up(){
		return new Corner(this.topLeft.up(), this.topRight.up(), this.topLeft, this.topRight);
	}
	
	public Corner down(){
		return new Corner(this.bottomLeft, this.bottomRight, this.bottomLeft.down(), this.bottomRight.down());
	}
	
	public Corner move(Dir dir){
		switch(dir){
			case UP: return up();
			case DOWN: return down();
			case LEFT: return left();
			case RIGHT: return right();
			default: return this;
		}
	}
	
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
	
	public Vec2 midpoint(Corner other) {
		return this.center.add(other.center).mul(0.5);
	}
	
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
	
	@Override
	public int hashCode() {
		return Objects.hash(topLeft, topRight, bottomLeft, bottomRight);
	}
	
	
	private static Vec2 midPoint(Vec2i a, Vec2i b){
		return new Vec2(0.5*(a.x+b.x), 0.5*(a.y+b.y));
	}
}
