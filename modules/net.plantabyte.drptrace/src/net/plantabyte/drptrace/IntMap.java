package net.plantabyte.drptrace;

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
	 * Get the pixel color/cell value at the given x,y coordinate.
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @return An integer value
	 * @throws ArrayIndexOutOfBoundsException thrown if (X,Y) is outside the
	 * bounds of this <code>IntMap</code>
	 */
	public abstract int get(int x, int y) throws ArrayIndexOutOfBoundsException;
	
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
