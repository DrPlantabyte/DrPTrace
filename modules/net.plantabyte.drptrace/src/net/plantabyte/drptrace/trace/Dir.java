package net.plantabyte.drptrace.trace;

public enum Dir{
	UP(0), LEFT(1), DOWN(2), RIGHT(3), NONE(4);
	private byte index;
	Dir(int index){
		this.index = (byte)index;
	}
	private static Dir[] counterClockwiseArray = {UP,LEFT,DOWN,RIGHT};
	
	public Dir rotateCounterClockwise(){
		return counterClockwiseArray[(this.index + 1) % 4];
	}
	public Dir rotateClockwise(){
		return counterClockwiseArray[(this.index + 3) % 4];
	}
}
