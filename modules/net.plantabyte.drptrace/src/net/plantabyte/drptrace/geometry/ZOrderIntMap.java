package net.plantabyte.drptrace.geometry;

public class ZOrderIntMap extends IntMap{
	private final int width;
	private final int height;
	private final int[] data;
	
	public ZOrderIntMap(int width, int height){
		this.width = width;
		this.height = height;
		final int size = 256 * (width >> 4 + 1) * (height >> 4 + 1);
		this.data = new int[size];
	}
	private static final int[] ZLUT = {
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
	private static int zorder4bto8it(final int x, final int y){
		final int xBits = ZLUT[x & 0x0F];
		final int yBits = ZLUT[y & 0x0F] << 1;
		return xBits | yBits;
	}
	private int index(final int x, final int y){
		final int subW = this.width >>> 4;
		final int subX = x >>> 4;
		final int subY = y >>> 4;
		return ((subY*subW+subX) << 8) | zorder4bto8it(x, y);
	}
	
	
	@Override
	public int get(final int x, final int y)
			throws ArrayIndexOutOfBoundsException {
		final int i = index(x, y);
		return data[i];
	}
	
	public int set(final int x, final int y, final int value)
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
	public ZOrderIntMap clone() {
		var b = new ZOrderIntMap(getWidth(), getHeight());
		System.arraycopy(this.data, 0, b.data, 0, this.data.length);
		return b;
	}
	
	@Deprecated public static void main(String[] a){
		final int w = 100, h = 50;
		var b = new ZOrderIntMap(w, h);
		for(int y = 0; y < b.getHeight(); y++) {
			for(int x = 0; x < b.getWidth(); x++) {
				if(Math.sqrt(x*x+y*y) < 50){
					b.set(x, y, x*x+y*y);
				}
			}
		}
		for(int y = 0; y < b.getHeight(); y++) {
			for(int x = 0; x < b.getWidth(); x++) {
				if(Math.sqrt(x*x+y*y) < 50){
					if(b.get(x, y) != x*x+y*y) {
						throw new RuntimeException("FUCK!");
					}
				} else {
					if(b.get(x, y) != 0) {
						throw new RuntimeException("FUCK2!");
					}
				}
			}
		}
		System.out.println(b);
	}
}
