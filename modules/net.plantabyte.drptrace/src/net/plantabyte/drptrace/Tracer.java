package net.plantabyte.drptrace;

import net.plantabyte.drptrace.geometry.BezierShape;
import net.plantabyte.drptrace.geometry.Vec2;
import net.plantabyte.drptrace.intmaps.ZOrderBinaryMap;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static net.plantabyte.drptrace.intmaps.IntMapUtil.floodFill;
import static net.plantabyte.drptrace.trace.TraceMachine.followEdge;

/**
 * The Tracer superclass provides a common API for all classes that can turn a series of points into a
 * sequence of bezier curves tracing that path. If tracing a shape, use
 * <code>traceClosedPath(Vec2[])</code>; if tracing a line, use
 * <code>traceOpenPath(Vec2[])</code>. For tracing a whole raster image,
 * use <code>traceAllShapes(IntMap)</code>.
 * <p>
 * Your tracing process should look like this:<br>
 * 1. transfer/store your raster data in an <code>IntMap</code><br>
 * 2. Instantiate a new <code>Tracer</code> instance (eg <code>new IntervalTracer(int)</code><br>
 * 3. Call <code>Tracer.traceAllShapes(IntMap)</code> to trace the raster
 * to a list of <code>BezierShape</code>s<br>
 * 4. Read the bezier curves from the <code>BezierShape</code> list<br>
 */
public abstract class Tracer {
	/**
	 * Traces a series of points as a sequence of bezier curves, looping back to
	 * the beginning to form a closed loop.
	 * @param pathPoints A series of points to trace with bezier curves.
	 * @return Returns a list of <code>BezierCurve</code>s tracing the path of
	 * the points
	 * @throws IllegalArgumentException Thrown if any of the input arguments are
	 * invalid (eg too few points to trace)
	 */
	public BezierShape traceClosedPath(Vec2[] pathPoints)
			throws IllegalArgumentException{
		return tracePath(pathPoints, true);
	}

	/**
	 * Traces a series of points as a sequence of bezier curves.
	 * @param pathPoints A series of points to trace with bezier curves.
	 * @return Returns a list of <code>BezierCurve</code>s tracing the path of
	 * the points
	 * @throws IllegalArgumentException Thrown if any of the input arguments are
	 * invalid (eg too few points to trace)
	 */
	public BezierShape traceOpenPath(Vec2[] pathPoints)
			throws IllegalArgumentException {
		return tracePath(pathPoints, false);
	}

	/**
	 * Traces a series of points as a sequence of bezier curves, looping back to
	 * the beginning to form a closed loop if so specified by the <code>closedLoop</code>
	 * parameter.
	 * @param pathPoints A series of points to trace with bezier curves.
	 * @param closedLoop If true, traceback to the starting point (index 0);
	 *                   if false, trace to the final point
	 * @return Returns a list of <code>BezierCurve</code>s tracing the path of
	 * the points
	 * @throws IllegalArgumentException Thrown if any of the input arguments are
	 * invalid (eg too few points)
	 */
	public abstract BezierShape tracePath(Vec2[] pathPoints, boolean closedLoop)
			throws IllegalArgumentException;

	/**
	 * Traces every shape (including the background) of the provided raster bitmap.
	 * @param bitmap A 2D array of integer values, such that each contiguous area
	 *               of a number is considered to be a single shape.
	 * @return Returns a list of <code>BezierShape</code> objects, each representing
	 * one shape from the raster. The order is important: the shapes should be drawn
	 * in the order such that the first index is in the back and the last index is
	 * in the front.
	 */
	public List<BezierShape> traceAllShapes(final IntMap bitmap) {
		final int w = bitmap.getWidth(), h = bitmap.getHeight();
		var searchedMap = new ZOrderBinaryMap(w, h);
		var output = new LinkedList<BezierShape>();
		// algorithm: flood fill each patch, and for
		// each flood fill, trace the outer edge
		for(int y = 0; y < h; y++){
			for(int x = 0; x < w; x++){
				if(searchedMap.get(x, y) == 0){ // pixel not yet searched
					// first, find a patch of color in the bitmap
					int color = bitmap.get(x, y);
					// next, trace the outer perimeter of the color patch
					var circumference = followEdge(bitmap, x, y);
					var vectorized = traceClosedPath(circumference);
					vectorized.setColor(color);
					vectorized.setClosed(true);
					output.add(vectorized);
					// finally, flood-fill the patch in the searched map
					floodFill(bitmap, searchedMap, x, y);
					searchedMap.set(x, y, (byte)1);
				}
			}
		}
		return output;
	}
	
	/**
	 * Traces a specific color from an <code>IntMap</code>, returning a series of
	 * <code>BezierShape</code> paths tracing that color from the provided
	 * <code>IntMap</code>. Note that the colors of the returned <code>BezierShape</code>s
	 * will either be 1 (outside edge of a color patch) or 0 (inside edge of a
	 * color patch).
	 * @param bitmap <code>IntMap</code> to trace
	 * @param target the target color in <code>bitmap</code> to trace
	 * @return A list of <code>BezierShape</code> paths tracing that color. The
	 * colors of the returned <code>BezierShape</code>s will either be
	 * 1 (outside edge of a color patch) or 0 (inside edge of a color patch).
	 */
	public List<BezierShape> traceColor(final IntMap bitmap, final int target){
		// first, pad with 1-pixel frame and binary map to 1 for color and 0 for all else
		var paddedIntMap = new IntMap(){
			@Override
			public int get(final int x, final int y)
					throws ArrayIndexOutOfBoundsException {
				if(bitmap.get(x-1, y-1) == target){
					return 1;
				} else {
					return 0;
				}
			}
			
			@Override
			public int getWidth() {
				return bitmap.getWidth()+2;
			}
			
			@Override
			public int getHeight() {
				return bitmap.getHeight()+2;
			}
			
			@Override
			public IntMap clone() {
				throw new UnsupportedOperationException("Cannot clone this anonymous IntMap class");
			}
		};
		// second, trace all shapes
		var shapes = this.traceAllShapes(paddedIntMap);
		// third, exclude frame shape and shift -1 to X and Y
		var deframedShapes = new ArrayList<BezierShape>(shapes.size()-1);
		final var t = new Vec2(-1, -1);
		final var bb1 = new Vec2(-0.1, -0.1);
		final var bb2 = new Vec2(bitmap.getWidth()+0.1, bitmap.getHeight()+0.1);
		for(var s : shapes){
			s.translate(t);
			if(allInRange(bb1, bb2, s)) {
				deframedShapes.add(s);
			}
		}
		// done
		return deframedShapes;
	}

	private static boolean allInRange(Vec2 min, Vec2 max, BezierShape s){
		for(var p : s){
			if(!p.withinBoundingBox(min, max)) return false;
		}
		return true;
	}
}
