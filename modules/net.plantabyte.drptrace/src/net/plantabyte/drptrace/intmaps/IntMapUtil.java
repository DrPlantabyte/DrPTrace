package net.plantabyte.drptrace.intmaps;

import net.plantabyte.drptrace.IntMap;
import net.plantabyte.drptrace.WritableIntMap;
import net.plantabyte.drptrace.geometry.Vec2i;

import java.util.LinkedList;

/**
 * This class provides static utility functions to facilitate the usage of <code>IntMap</code>s
 */
public class IntMapUtil {

	/**
	 * Performs a flood-fill operation in <code>source</code>, setting the corresponding
	 * filled bits in <code>searchedMap</code> to 1.
	 * @param source Source IntMap
	 * @param searchedMap Map used to keep track of what is (already) filled
	 * @param x x coordinate of start of flood fill
	 * @param y y coordinate of start of flood fill
	 */
	public static void floodFill(final IntMap source, final ZOrderBinaryMap searchedMap, final int x, final int y){
		final int color = source.get(x,y);
		final var Q = new LinkedList<Vec2i>();
		Q.push(new Vec2i(x, y));
		while(Q.size() > 0){
			var pop = Q.pop();
			searchedMap.set(pop.x, pop.y, (byte)1);
			Vec2i[] neighbors = {pop.up(), pop.left(), pop.down(), pop.right()};
			for(var n : neighbors){
				if(source.isInRange(n.x, n.y) && source.get(n.x, n.y) == color && searchedMap.get(n.x, n.y) == 0){
					// n is same color and not yet searched
					Q.push(n);
				}
			}
		}
	}

	/**
	 * Wraps the provided IntMap, such that calling <code>.get(x,y)</code> on the returned instance will return a 1 if
	 * the source position equals <code>targetValue</code> and 0 otherwise.
	 * @param source An IntMap
	 * @param targetValue The filter target value
	 * @return 1 or 0
	 */
	public static IntMap filterByValueToBinaryMap(final IntMap source, final int targetValue){
		return new IntMap(){
			@Override
			public int get(int x, int y) throws ArrayIndexOutOfBoundsException {
				final int i = source.get(x, y);
				return i == targetValue ? 1 : 0;
			}

			@Override
			public int getWidth() {
				return source.getWidth();
			}

			@Override
			public int getHeight() {
				return source.getHeight();
			}

			@Override
			public IntMap clone() {
				return filterByValueToBinaryMap(source.clone(), targetValue);
			}
		};
	}
	
	/**
	 * Copies a region of elements from the source <code>IntMap</code> into the
	 * given <code>WritableIntMap</code>
	 * @param src The <code>IntMap</code> to draw onto <code>canvas</code>
	 * @param srcX X coordinate of upper-left corner of region to copy
	 * @param srcY Y coordinate of upper-left corner of region to copy
	 * @param w Width of region to copy
	 * @param h Height of region to copy
	 * @param canvas The destination <code>WritableIntMap</code> to modify
	 * @param destX X coordinate of upper-left corner of the destination
	 * @param destY Y coordinate of upper-left corner of the destination
	 * @throws IllegalArgumentException Thrown if the data in <code>src</code> is
	 * not compatible with <code>canvas</code>
	 */
	public static void drawOnto(
			final IntMap src, final int srcX, final int srcY,
			final int w, final int h,
			final WritableIntMap canvas, final int destX, final int destY
	) throws IllegalArgumentException {
		for(int dy = 0; dy < h; ++dy){
			final int iy = srcY+dy;
			final int oy = destY+dy;
			for(int dx = 0; dx < w; ++dx){
				final int ix = srcX+dx;
				final int ox = destX+dx;
				if(canvas.isInRange(ox,oy)) {
					canvas.set(ox, oy, src.get(ix, iy));
				}
			}
		}
	}
}
