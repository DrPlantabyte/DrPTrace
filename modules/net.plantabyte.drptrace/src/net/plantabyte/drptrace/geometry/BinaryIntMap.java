package net.plantabyte.drptrace.geometry;

import java.util.Arrays;

public class BinaryIntMap extends IntMap {
	private final int width;
	private final int height;
	private final int subwidth;
	private final byte[] data;
	
	public BinaryIntMap(final int width, final int height) {
		this.width = width;
		this.subwidth = width/8+1;
		this.height = height;
		this.data = new byte[subwidth*height];
	}
	
	public int set(int x, int y, byte value)
			throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
		if((value & 0xFE) != 0) {
			throw new IllegalArgumentException(String.format("%s can only accept values of 0 or 1", this.getClass().getName()));
		}
		final int i = width*y + x;
		final int index = i >>> 3;
		final byte shift = (byte)(i & 0x07);
		final int bitpos = 1 << shift;
		final int mask = ~bitpos;
		final byte oldVal = data[index];
		final byte newVal = (byte)((value & 0x01) << shift | (oldVal & mask));
		data[index] = newVal;
		return (oldVal & bitpos) >> shift;
	}
	
	public void fill(byte value){
		if(value == 0) {
			Arrays.fill(data, (byte)0);
		} else {
			Arrays.fill(data, (byte)0xFF);
		}
	}
	
	
	@Override
	public int get(final int x, final int y)
			throws ArrayIndexOutOfBoundsException {
		final int i = width*y + x;
		final int index = i >>> 3;
		final byte shift = (byte)(i & 0x07);
		final int bitpos = 1 << shift;
		final byte oldVal = data[index];
		return (oldVal & bitpos) >> shift;
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
		var copy = new BinaryIntMap(this.getWidth(), this.getHeight());
		System.arraycopy(this.data, 0, copy.data, 0, this.data.length);
		return copy;
	}
	
//	@Deprecated public static void main(String[] a){
//		final int w = 100, h = 50;
//		var b = new ByteArrayBitmap(w, h);
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
