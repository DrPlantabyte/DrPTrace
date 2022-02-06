package net.plantabyte.drptrace.intmaps;

import net.plantabyte.drptrace.IntMap;
import net.plantabyte.drptrace.geometry.Vec2i;

import java.util.LinkedList;

public class IntMapUtil {

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
}
