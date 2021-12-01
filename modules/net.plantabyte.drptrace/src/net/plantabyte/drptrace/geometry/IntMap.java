package net.plantabyte.drptrace.geometry;

public abstract class IntMap {
	
	public abstract int get(int x, int y) throws ArrayIndexOutOfBoundsException;
	public abstract int getWidth();
	public abstract int getHeight();
	public abstract IntMap clone();
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
