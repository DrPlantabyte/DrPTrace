package net.plantabyte.drptrace.geometry;

public abstract class Bitmap {
	
	public abstract int get(int x, int y) throws ArrayIndexOutOfBoundsException;
	public abstract int getWidth();
	public abstract int getHeight();
	public abstract Bitmap clone();
	@Override public String toString(){
		var sb = new StringBuilder();
		for(int y = 0; y < this.getHeight(); y++){
			for(int x = 0; x < this.getWidth(); x++){
				sb.append(this.get(x, y));
			}
			sb.append('\n');
		}
		return sb.toString();
	}
}
