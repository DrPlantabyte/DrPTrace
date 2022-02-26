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
package net.plantabyte.drptrace;

import net.plantabyte.drptrace.intmaps.ZOrderBinaryMap;
import net.plantabyte.drptrace.intmaps.ZOrderByteMap;
import net.plantabyte.drptrace.intmaps.ZOrderIntMap;

/**
 * The <code>IntMap</code> superclass is the primary data storage structure for
 * DrPTrace. Several high-performance implementations are provided depending on
 * whether you are working with colors (eg <code>ZOrderIntMap</code>) or logic
 * (eg <code>ZOrderBinaryMap</code>). See the package
 * <code>net.plantabyte.drptrace.intmaps</code> for more default implementations.
 * You can also provide your own implementation.
 *
 * Note that implementations have their own <code>set(...)</code> methods. The
 * reason that the <code>IntMap</code> superclass does not is that what
 * constitutes a valid value depends on the specific implementation (and some
 * implementations may be read-only).
 *
 * This class and provided implementations are not thread-safe.
 */
public abstract class IntMap {
	/**
	 * Utility function to create an <code>IntMap</code> from a given matrix.
	 * @param matrix a square 2D array that is indexed in the order of [row][column]
	 * @return An <code>IntMap</code> that holds a copy of the given matrix data.
	 */
	public static IntMap fromMatrix(int[][] matrix){
		return ZOrderIntMap.fromMatrix(matrix);
	}
	/**
	 * Utility function to create an <code>IntMap</code> from a given matrix.
	 * @param matrix a square 2D array that is indexed in the order of [row][column]
	 * @return An <code>IntMap</code> that holds a copy of the given matrix data.
	 */
	public static IntMap fromMatrix(byte[][] matrix){
		var im = new ZOrderByteMap(matrix[0].length, matrix.length);
		for(int row = 0; row < matrix.length; row++){
			for(int col = 0; col < matrix.length; col++){
				im.set(col, row, matrix[row][col]);
			}
		}
		return im;
	}
	/**
	 * Utility function to create an <code>IntMap</code> from a given matrix.
	 * @param matrix a square 2D array that is indexed in the order of [row][column]
	 * @return An <code>IntMap</code> that holds a copy of the given matrix data.
	 */
	public static IntMap fromMatrix(boolean[][] matrix){
		var im = new ZOrderBinaryMap(matrix[0].length, matrix.length);
		for(int row = 0; row < matrix.length; row++){
			for(int col = 0; col < matrix.length; col++){
				im.set(col, row, matrix[row][col] ? (byte)1 : (byte)0);
			}
		}
		return im;
	}
	
	public static void drawOnto(
			final IntMap src, final int srcX, final int srcY,
			final int w, final int h,
			final IntMap canvas, final int destX, final int destY
	) throws IllegalArgumentException {
		for(int dy = 0; dy < h; ++dy){
			final int iy = srcY+dy;
			final int oy = destY+dy;
			for(int dx = 0; dx < w; ++dx){
				final int ix = srcX+dx;
				final int ox = destX+dx;
				if(canvas.isInRange(ox,oy)) {
					canvas.set(ox, oy, src.get(ix, iy));
				}
			}
		}
	}
	
	/**
	 * Get the pixel color/cell value at the given x,y coordinate.
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @return An integer value
	 * @throws ArrayIndexOutOfBoundsException thrown if (X,Y) is outside the
	 * bounds of this <code>IntMap</code>
	 */
	public abstract int get(int x, int y) throws ArrayIndexOutOfBoundsException;
	
	
	/**
	 * Sets the value at a given coordinate to the specified value.
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param value value to store at (X,Y)
	 * @return Returns the previous value that was overwritten.
	 * @throws ArrayIndexOutOfBoundsException Thrown if coordinate (X,Y) is out
	 * of bounds
	 * @throws IllegalArgumentException Thrown if <code>value</code> is not valid
	 * for this type of IntMap (eg trying to store a "2" in a binary map)
	 */
	public abstract int set(final int x, final int y, final int value)
			throws ArrayIndexOutOfBoundsException, IllegalArgumentException;
	
	/**
	 * Gets the width of this <code>IntMap</code>
	 * @return The width of this <code>IntMap</code>
	 */
	public abstract int getWidth();
	
	/**
	 * Gets the height of this <code>IntMap</code>
	 * @return The height of this <code>IntMap</code>
	 */
	public abstract int getHeight();
	
	/**
	 * Implementations must create a deep-copy clone when this method is invoked.
	 * @return A new <code>IntMap</code> with identical data to this one.
	 */
	public abstract IntMap clone();
	
	/**
	 * Returns <code>true</code> if and only if the coordinate (X,Y) is valid
	 * (ie calling <code>get(x,y)</code> will return a value without error)
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @return <code>true</code> if and only if the coordinate (X,Y) is valid
	 */
	public boolean isInRange(int x, int y){
		return x >= 0 && y >= 0 && x < this.getWidth() && y < this.getHeight();
	}
	
	/**
	 * Returns a debug string describing this object (implementations may
	 * override this behavior)
	 * @return A debug string
	 */
	@Override public String toString(){
		var sb = new StringBuilder();
//		for(int y = 0; y < this.getHeight(); y++){
//			for(int x = 0; x < this.getWidth(); x++){
//				sb.append(this.get(x, y));
//			}
//			sb.append('\n');
//		}
		sb.append("[(IntMap) ").append(getClass().getName()).append(": ")
				.append(this.getWidth()).append("x").append(this.getHeight())
				.append("]");
		return sb.toString();
	}
}
