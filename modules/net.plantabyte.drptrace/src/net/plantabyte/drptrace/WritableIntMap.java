package net.plantabyte.drptrace;

/**
 * A mutable <code>IntMap</code> superclass. See <code>net.plantabyte.drptrace.IntMap</code>
 * for details.
 */
public abstract class WritableIntMap extends IntMap{
	
	
	/**
	 * Sets the value at a given coordinate to the specified value.
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param value value to store at (X,Y)
	 * @return Returns the previous value that was overwritten.
	 * @throws ArrayIndexOutOfBoundsException Thrown if coordinate (X,Y) is out
	 * of bounds
	 * @throws IllegalArgumentException Thrown if <code>value</code> is not valid
	 * for this type of IntMap (eg trying to store a "2" in a binary map)
	 */
	public abstract int set(final int x, final int y, final int value)
			throws ArrayIndexOutOfBoundsException, IllegalArgumentException;
}
