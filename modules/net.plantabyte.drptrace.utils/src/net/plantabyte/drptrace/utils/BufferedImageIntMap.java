package net.plantabyte.drptrace.utils;

import net.plantabyte.drptrace.geometry.IntMap;

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
