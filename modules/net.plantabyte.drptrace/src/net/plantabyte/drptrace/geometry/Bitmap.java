package net.plantabyte.drptrace.geometry;

public abstract class Bitmap {
	
	public abstract int get(int x, int y) throws ArrayIndexOutOfBoundsException;
	public abstract int getWidth();
	public abstract int getHeight();
}
