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

import java.awt.image.BufferedImage;

public class BufferedImageIntMap extends IntMap {
	
	private final BufferedImage bimg;
	
	public BufferedImageIntMap(BufferedImage img){
		this.bimg = img;
	}
	@Override
	public int get(final int x, final int y)
			throws ArrayIndexOutOfBoundsException {
		return bimg.getRGB(x, y);
	}
	
	@Override
	public int getWidth() {
		return bimg.getWidth();
	}
	
	@Override
	public int getHeight() {
		return bimg.getHeight();
	}
	
	@Override
	public IntMap clone() {
		return new BufferedImageIntMap(this.bimg);
	}
}
