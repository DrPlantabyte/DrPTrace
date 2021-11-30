package net.plantabyte.drptrace.geometry;

public class ByteArrayBitmap extends Bitmap{
	private final int width;
	private final int height;
	private final byte[] data;
	
	public ByteArrayBitmap(final int width, final int height) {
		this.width = width;
		this.height = height;
		this.data = new byte[width*height];
	}
	
	public int set(byte value, int x, int y){
		byte old = data[width*y + x];
		data[width*y + x] = value;
		return old;
	}
	
	
	@Override
	public int get(final int x, final int y)
			throws ArrayIndexOutOfBoundsException {
		return data[width*y + x];
	}
	
	@Override
	public int getWidth() {
		return width;
	}
	
	@Override
	public int getHeight() {
		return height;
	}
}
