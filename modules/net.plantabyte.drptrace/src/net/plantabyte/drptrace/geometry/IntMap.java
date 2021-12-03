package net.plantabyte.drptrace.geometry;

public abstract class IntMap {
	
	public abstract int get(int x, int y) throws ArrayIndexOutOfBoundsException;
	public abstract int getWidth();
	public abstract int getHeight();
	public abstract IntMap clone();
	public boolean isInRange(int x, int y){
		return x >= 0 && y >= 0 && x < this.getWidth() && y < this.getHeight();
	}
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
