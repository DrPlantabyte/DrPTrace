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
package net.plantabyte.drptrace.utils;

import net.plantabyte.drptrace.IntMap;
import net.plantabyte.drptrace.intmaps.ZOrderIntMap;

import java.awt.image.BufferedImage;

/**
 * This is a lightweight <code>IntMap</code> wrapper for AWT buffered images.
 */
public class BufferedImageIntMap extends IntMap {
	
	private final BufferedImage bimg;
	
	/**
	 * Constructs an <code>IntMap</code> wrapper for the given
	 * <code>BufferedImage</code>. This does not copy the data, so the
	 * <code>BufferedImage</code> must not change while this object is being used
	 * for tracing.
	 * @param img the image to wrap
	 */
	public BufferedImageIntMap(BufferedImage img){
		this.bimg = img;
	}
	
	/**
	 * Get the pixel ARGB color value at the given x,y coordinate of the image.
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @return An integer value
	 * @throws ArrayIndexOutOfBoundsException thrown if (X,Y) is outside the
	 * bounds of this <code>IntMap</code>
	 */
	@Override
	public int get(final int x, final int y)
			throws ArrayIndexOutOfBoundsException {
		return bimg.getRGB(x, y);
	}
	
	/**
	 * Gets the width of the image
	 * @return The width of this <code>IntMap</code>
	 */
	@Override
	public int getWidth() {
		return bimg.getWidth();
	}
	
	/**
	 * Gets the height of the image
	 * @return The height of this <code>IntMap</code>
	 */
	@Override
	public int getHeight() {
		return bimg.getHeight();
	}
	
	/**
	 * Creates a deep-copy clone of this <code>IntMap</code>. The returned
	 * <code>IntMap</code> is not a <code>BufferedImageIntMap</code> but is a
	 * different implementation of <code>IntMap</code> instead (for better
	 * performance).
	 * @return A new <code>IntMap</code> that is a deep-copy duplicate of this one
	 */
	@Override
	public IntMap clone() {
		ZOrderIntMap out = new ZOrderIntMap(bimg.getWidth(), bimg.getHeight());
		for(int y = 0; y < bimg.getHeight(); y++){
			for(int x = 0; x < bimg.getWidth(); x++){
				out.set(x, y, bimg.getRGB(x, y));
			}
		}
		return out;
	}
}
