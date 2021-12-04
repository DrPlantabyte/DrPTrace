package net.plantabyte.drptrace.geometry;

import java.util.Arrays;

public class ZOrderBinaryMap extends IntMap {
	private final int width;
	private final int height;
	private final int chunksPerRow;
	private final long[] data;
	
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
	
	public void fill(byte value){
		if((value & 0xFE) != 0) {
			throw new IllegalArgumentException(String.format("%s can only accept values of 0 or 1", this.getClass().getName()));
		}
		if(value == 0) {
			Arrays.fill(data, (byte)0);
		} else {
			Arrays.fill(data, (byte)0xFF);
		}
	}
	
	
	@Override
	public int get(final int x, final int y)
			throws ArrayIndexOutOfBoundsException {
		
		final int index = chunkIndex(x, y);
		final long shift = zorder3bito6bit(x, y);
		final long val = data[index];
		return (int)(val >>> shift) & 0x01;
	}
	
	@Override
	public int getWidth() {
		return width;
	}
	
	@Override
	public int getHeight() {
		return height;
	}
	
	@Override
	public IntMap clone() {
		var copy = new ZOrderBinaryMap(this.getWidth(), this.getHeight());
		System.arraycopy(this.data, 0, copy.data, 0, this.data.length);
		return copy;
	}
	
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
