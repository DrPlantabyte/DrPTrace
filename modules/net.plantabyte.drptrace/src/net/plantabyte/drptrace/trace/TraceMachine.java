package net.plantabyte.drptrace.trace;

import net.plantabyte.drptrace.IntMap;
import net.plantabyte.drptrace.geometry.Vec2;
import net.plantabyte.drptrace.geometry.Vec2i;

import java.util.List;

/**
 * This is the tracing state machine responsible for converting raster shapes into
 * point paths, which can then be used to fit bezier curves
 */
public class TraceMachine{
	private final IntMap src;
	private Corner pos;
	private final Corner initialPos;
	private Corner oldPos;
	private final int color;
	private final List<Vec2> midpoints;
	
	/**
	 * <code>TraceMachine</code> standard constructor to trace a shape starting
	 * at an edge point
	 * @param source IntMap holding the raster data
	 * @param startPoint Where to start tracing from
	 * @param color The "color" we are tracing (the target value in the IntMap)
	 * @param pointTacker List to which traced points will be appended
	 */
	public TraceMachine(IntMap source, Vec2i startPoint, int color, List<Vec2> pointTacker){
		this.src = source;
		this.pos = new Corner(startPoint);
		this.initialPos = pos;
		this.oldPos = pos.down();
		this.color = color;
		this.midpoints = pointTacker;
	}
	private boolean isColor(Vec2i p){
		return src.isInRange(p.x, p.y) && src.get(p.x, p.y) == color;
	}
	
	/**
	 * Check if done tracing
	 * @return returns <code>true</code> if the state machine's position has
	 * returned to the starting position
	 */
	public boolean done(){
		return this.pos.equals(initialPos);
	}
	
	/**
	 * Iterate the state machine once to take a single tracing step around the
	 * shape.
	 */
	public void step(){
		// first, get direction and rotate color checking to match perspective of machine motion
		var dir = pos.dirFrom(oldPos);
		var turn = Dir.NONE;
		int perspective_nl_nr_fl_fr = 0b0000;
		boolean nl = false, nr = false, fl = false, fr = false;
		switch(dir){
			case UP:{
				nl = isColor(pos.bottomLeft);
				nr = isColor(pos.bottomRight);
				fl = isColor(pos.topLeft);
				fr = isColor(pos.topRight);
				break;
			}
			case DOWN:{
				nl = isColor(pos.topRight);
				nr = isColor(pos.topLeft);
				fl = isColor(pos.bottomRight);
				fr = isColor(pos.bottomLeft);
				break;
			}
			case LEFT:{
				nl = isColor(pos.bottomRight);
				nr = isColor(pos.topRight);
				fl = isColor(pos.bottomLeft);
				fr = isColor(pos.topLeft);
				break;
			}
			case RIGHT:{
				nl = isColor(pos.topLeft);
				nr = isColor(pos.bottomLeft);
				fl = isColor(pos.topRight);
				fr = isColor(pos.bottomRight);
				break;
			}
			default:
				throw new IllegalStateException("Machine old and current position equal, cannot move");
		}
		if(nl) perspective_nl_nr_fl_fr |= 0b1000;
		if(nr) perspective_nl_nr_fl_fr |= 0b0100;
		if(fl) perspective_nl_nr_fl_fr |= 0b0010;
		if(fr) perspective_nl_nr_fl_fr |= 0b0001;
		// then decide whether to turn or keep going in same direction
		switch(perspective_nl_nr_fl_fr){
			case 0b1000:
				// ..
				// #.
				// ^^
			case 0b0111:
				// ##
				// .#
				// ^^
			case 0b1001:
				// .#
				// #.
				// ^^
			case 0b1100:
				// ..
				// ##
				// ^^
				turn = Dir.LEFT;
				break;
			case 0b0100:
				// ..
				// .#
				// ^^
			case 0b1011:
				// ##
				// #.
				// ^^
			case 0b0110:
				// #.
				// .#
				// ^^
			case 0b0011:
				// ##
				// ..
				// ^^
				turn = Dir.RIGHT;
				break;
			case 0b1111:
				// ##
				// ##
				// ^^
				throw new IllegalStateException("Not on corner, all four are color");
			case 0b0000:
				// ..
				// ..
				// ^^
				throw new IllegalStateException("Not on corner, all four are off-color");
		}
		Corner newPos;
		if(turn == Dir.NONE){
			// keep going straight
			newPos = pos.move(dir);
		} else if( turn == Dir.LEFT){
			// turn left, aka counter-clockwise
			newPos = pos.move(dir.rotateCounterClockwise());
		} else {
			// turn right, aka clockwise
			newPos = pos.move(dir.rotateClockwise());
		}
		// now add edge point from step
		midpoints.add(pos.midpoint(newPos));
		// finally, update the position
		oldPos = pos;
		pos = newPos;
	}
}
