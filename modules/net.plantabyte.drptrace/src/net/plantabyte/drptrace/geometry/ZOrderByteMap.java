package net.plantabyte.drptrace.geometry;

public class ZOrderByteMap extends IntMap{
	private final int width;
	private final int height;
	private final int chunksPerRow; // number of chunks wide
	private final byte[] data;
	
	public ZOrderByteMap(int width, int height){
		this.width = width;
		this.height = height;
		this.chunksPerRow = ((width >> 4) + 1);
		final int size = 256 * chunksPerRow * ((height >> 4) + 1);
		this.data = new byte[size];
	}
	
	private static int zorder4bito8bit(final int x, final int y){
		final byte[] ZLUT = {
				0b00000000,
				0b00000001,
				0b00000100,
				0b00000101,
				0b00010000,
				0b00010001,
				0b00010100,
				0b00010101,
				0b01000000,
				0b01000001,
				0b01000100,
				0b01000101,
				0b01010000,
				0b01010001,
				0b01010100,
				0b01010101
		};
		final int xBits = ZLUT[x & 0x0F];
		final int yBits = ZLUT[y & 0x0F] << 1;
		return xBits | yBits;
	}
	private int index(final int x, final int y){
		int chunk = chunksPerRow * (y >>> 4) + (x >>> 4);
		return (chunk << 8) | zorder4bito8bit(x, y);
	}
	
	
	@Override
	public int get(final int x, final int y)
			throws ArrayIndexOutOfBoundsException {
		final int i = index(x, y);
		return data[i];
	}
	
	public int set(final int x, final int y, final byte value)
			throws ArrayIndexOutOfBoundsException {
		final int i = index(x, y);
		int t = data[i];
		data[i] = value;
		return t;
	}
	
	@Override
	public int getWidth() {
		return this.width;
	}
	
	@Override
	public int getHeight() {
		return this.height;
	}
	
	@Override
	public ZOrderByteMap clone() {
		var b = new ZOrderByteMap(getWidth(), getHeight());
		System.arraycopy(this.data, 0, b.data, 0, this.data.length);
		return b;
	}
	
}