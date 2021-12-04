package net.plantabyte.drptrace.intmaps;

import net.plantabyte.drptrace.IntMap;

import java.util.Arrays;

/**
 * For simple tracing of black/white (0/1) shapes, this <code>IntMap</code> class
 * provides high performance and very small memory usage by storing the pixels/cells
 * as individual bits.
 */
public final class ZOrderBinaryMap extends IntMap {
	private final int width;
	private final int height;
	private final int chunksPerRow;
	private final long[] data;
	
	/**
	 * Constructs a new instance with the given width and height. All values start
	 * as zero.
	 * @param width width of the raster
	 * @param height height of the raster
	 */
	public ZOrderBinaryMap(final int width, final int height) {
		this.width = width;
		this.chunksPerRow = width/8+1;
		this.height = height;
		this.data = new long[chunksPerRow*(height/8+1)];
	}
	
	
	private static int zorder3bito6bit(final int x, final int y){
		final byte[] ZLUT = {
				0b00000000,
				0b00000001,
				0b00000100,
				0b00000101,
				0b00010000,
				0b00010001,
				0b00010100,
				0b00010101,
		};
		final int xBits = ZLUT[x & 0x07];
		final int yBits = ZLUT[y & 0x07] << 1;
		return xBits | yBits;
	}
	private int chunkIndex(final int x, final int y){
		return chunksPerRow * (y >>> 3) + (x >>> 3);
	}
	
	/**
	 * Sets the value at a given coordinate to the specified value, either 0 or 1.
	 * Attempting to set the value to any other number will result in an
	 * <code>IllegalArgumentException</code>.
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param value 0 or 1
	 * @return Returns the previous value that was overwritten.
	 * @throws ArrayIndexOutOfBoundsException Thrown if coordinate (X,Y) is out
	 * of bounds
	 * @throws IllegalArgumentException Thrown if <code>value</code> is not valid
	 */
	public int set(int x, int y, byte value)
			throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
		if((value & 0xFE) != 0) {
			throw new IllegalArgumentException(String.format("%s can only accept values of 0 or 1", this.getClass().getName()));
		}
		final int index = chunkIndex(x, y);
		final long shift = zorder3bito6bit(x, y);
		final long bitpos = 1L << shift;
		final long mask = ~bitpos;
		final long oldVal = data[index];
		final long newVal = (value & 0x01L) << shift | (oldVal & mask);
		data[index] = newVal;
		return (int)(oldVal >>> shift) & 0x01;
	}
	
	/**
	 * Sets all pixels/cells to the specified value
	 * @param value 0 or 1
	 * @throws IllegalArgumentException Thrown if <code>value</code> is not valid
	 */
	public void fill(byte value) throws IllegalArgumentException{
		if((value & 0xFE) != 0) {
			throw new IllegalArgumentException(String.format("%s can only accept values of 0 or 1", this.getClass().getName()));
		}
		if(value == 0) {
			Arrays.fill(data, (byte)0);
		} else {
			Arrays.fill(data, (byte)0xFF);
		}
	}
	
	/**
	 * Get the pixel color/cell value at the given (X,Y) coordinate.
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @return 1 or 0
	 * @throws ArrayIndexOutOfBoundsException thrown if (X,Y) is outside the
	 * bounds of this <code>IntMap</code>
	 */
	@Override
	public int get(final int x, final int y)
			throws ArrayIndexOutOfBoundsException {
		
		final int index = chunkIndex(x, y);
		final long shift = zorder3bito6bit(x, y);
		final long val = data[index];
		return (int)(val >>> shift) & 0x01;
	}
	
	/**
	 * Gets the width of this <code>IntMap</code>
	 * @return The width of this <code>IntMap</code>
	 */
	@Override
	public int getWidth() {
		return width;
	}
	
	/**
	 * Gets the height of this <code>IntMap</code>
	 * @return The height of this <code>IntMap</code>
	 */
	@Override
	public int getHeight() {
		return height;
	}
	/**
	 * Creates a deep-copy clone
	 * @return A new <code>IntMap</code> with identical data to this one.
	 */
	@Override
	public IntMap clone() {
		var copy = new ZOrderBinaryMap(this.getWidth(), this.getHeight());
		System.arraycopy(this.data, 0, copy.data, 0, this.data.length);
		return copy;
	}
	
	/**
	 * Returns the binary map as 1's and 0's
	 * @return a block of text containing 1's and 0's representing the raster buffer
	 */
	@Override
	public String toString() {
		var sb = new StringBuilder();
		for(int y = getHeight()-1; y >= 0; y--){
			for(int x = 0; x < getWidth(); x++){
				sb.append(this.get(x,y));
			}
			sb.append('\n');
		}
		return sb.toString();
	}
	
	//	@Deprecated public static void main(String[] a){
//		final int w = 100, h = 50;
//		var b = new ZOrderBinaryMap(w, h);
//		for(int y = 0; y < b.getHeight(); y++) {
//			for(int x = 0; x < b.getWidth(); x++) {
//				if(Math.sqrt(x*x+y*y) < 50){
//					b.set(x, y, (byte)1);
//				}
//			}
//		}
//		for(int y = 0; y < b.getHeight(); y++) {
//			for(int x = 0; x < b.getWidth(); x++) {
//				if(Math.sqrt(x*x+y*y) < 50){
//					if(b.get(x, y) != 1) throw new RuntimeException("FUCK!");
//				} else {
//					if(b.get(x, y) != 0) throw new RuntimeException("FUCK2!");
//				}
//			}
//		}
//		System.out.println(b);
//	}
}
