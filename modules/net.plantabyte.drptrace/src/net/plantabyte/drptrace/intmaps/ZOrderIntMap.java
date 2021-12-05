/*
MIT License

Copyright (c) 2021 Dr. Christopher C. Hall, aka DrPlantabyte

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package net.plantabyte.drptrace.intmaps;

import net.plantabyte.drptrace.IntMap;

/**
 * Standard <code>IntMap</code> implementation. It uses Z-Ordering to improve
 * cache coherence and therefore tracing performance.
 */
public final class ZOrderIntMap extends IntMap {
	private final int width;
	private final int height;
	private final int chunksPerRow; // number of chunks wide
	private final int[] data;
	
	/**
	 * Constructs a new instance with the given width and height. All values start
	 * as zero.
	 * @param width width of the raster
	 * @param height height of the raster
	 */
	public ZOrderIntMap(int width, int height){
		this.width = width;
		this.height = height;
		this.chunksPerRow = ((width >> 4) + 1);
		final int size = 256 * chunksPerRow * ((height >> 4) + 1);
		this.data = new int[size];
	}
	
	/**
	 * Constructs a new instance from the given 2D array, copying the data from
	 * the provided array. The 2D array is assumed to be in matrix format, so it is
	 * indexed as <code>rowColMatrix[y][x]</code>
	 * @param rowColMatrix 2D array in matrix index ordering (that is,
	 *                     rowColMatrix[row index][column index], aka y-x order)
	 */
	public static ZOrderIntMap fromMatrix(int[][] rowColMatrix){
		final int w=rowColMatrix[0].length, h=rowColMatrix.length;
		var out = new ZOrderIntMap(w, h);
		for(int y = 0; y < h; y++){
			for(int x = 0; x < w; x++){
				out.set(x, y, rowColMatrix[y][x]);
			}
		}
		return out;
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
	
	
	/**
	 * Get the value at the given (X,Y) coordinate.
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @return A byte value as an <code>int</code>
	 * @throws ArrayIndexOutOfBoundsException thrown if (X,Y) is outside the
	 * bounds of this <code>IntMap</code>
	 */
	@Override
	public int get(final int x, final int y)
			throws ArrayIndexOutOfBoundsException {
		final int i = index(x, y);
		return data[i];
	}
	
	/**
	 * Sets the value at a given coordinate to the specified value.
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param value value to store at (X,Y)
	 * @return Returns the previous value that was overwritten.
	 * @throws ArrayIndexOutOfBoundsException Thrown if coordinate (X,Y) is out
	 * of bounds
	 */
	public int set(final int x, final int y, final int value)
			throws ArrayIndexOutOfBoundsException {
		final int i = index(x, y);
		int t = data[i];
		data[i] = value;
		return t;
	}
	
	/**
	 * Gets the width of this <code>IntMap</code>
	 * @return The width of this <code>IntMap</code>
	 */
	@Override
	public int getWidth() {
		return this.width;
	}
	
	/**
	 * Gets the height of this <code>IntMap</code>
	 * @return The height of this <code>IntMap</code>
	 */
	@Override
	public int getHeight() {
		return this.height;
	}
	
	/**
	 * Creates a deep-copy clone
	 * @return A new <code>IntMap</code> with identical data to this one.
	 */
	@Override
	public ZOrderIntMap clone() {
		var b = new ZOrderIntMap(getWidth(), getHeight());
		System.arraycopy(this.data, 0, b.data, 0, this.data.length);
		return b;
	}
	
//	@Deprecated public static void main(String[] a){
//		final int w = 100, h = 50;
//		var b = new ZOrderIntMap(w, h);
//		for(int y = 0; y < b.getHeight(); y++) {
//			for(int x = 0; x < b.getWidth(); x++) {
//				if(Math.sqrt(x*x+y*y) < h){
//					b.set(x, y, x*100+y);
//				}
//			}
//		}
//		for(int y = 0; y < b.getHeight(); y++) {
//			for(int x = 0; x < b.getWidth(); x++) {
//				if(Math.sqrt(x*x+y*y) < h){
//					if(b.get(x, y) != x*100+y) {
//						throw new RuntimeException("FUCK!");
//					}
//				} else {
//					if(b.get(x, y) != 0) {
//						throw new RuntimeException("FUCK2!");
//					}
//				}
//			}
//		}
//		System.out.println(b);
//	}
}
