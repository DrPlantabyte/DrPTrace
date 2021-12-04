package net.plantabyte.drptrace.trace;

/**
 * This enum is used to track the direction of the tracing state machine
 */
public enum Dir{
	UP(0), LEFT(1), DOWN(2), RIGHT(3), NONE(4);
	private byte index;
	Dir(int index){
		this.index = (byte)index;
	}
	private static Dir[] counterClockwiseArray = {UP,LEFT,DOWN,RIGHT};
	
	/**
	 * Rotate the direction counter-clockwise (eg up  -> left -> down -> right)
	 * @return the new Dir
	 */
	public Dir rotateCounterClockwise(){
		return counterClockwiseArray[(this.index + 1) % 4];
	}
	
	/**
	 * Rotate the direction clockwise (eg up  -> right -> down -> left)
	 * @return the new Dir
	 */
	public Dir rotateClockwise(){
		return counterClockwiseArray[(this.index + 3) % 4];
	}
}
