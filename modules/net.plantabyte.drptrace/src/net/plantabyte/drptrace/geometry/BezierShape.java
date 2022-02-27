/*
MIT License

Copyright (c) 2021 Dr. Christopher C. Hall, aka DrPlantabyte

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package net.plantabyte.drptrace.geometry;

import java.util.ArrayList;
import java.util.List;

/**
 * A <code>BezierShape</code> represents a sequence of bezier curves either
 * outlining a solid shape or flowing a single open-ended path. If produced by
 * tracing a raster, the color will be set to match the traced shape.
 *
 * This class is not thread safe.
 */
public class BezierShape extends ArrayList<BezierCurve> {
	/** color, whcih might be ARGB or an index */
	private int color = 0;
	/** is closed or open loop? */
	private boolean closedLoop = false;
	
	/**
	 * Default constructor for an empty <code>BezierShape</code>. The color will
	 * be 0 and it will not be closed by default.
	 */
	public BezierShape(){
		super();
	}
	
	/**
	 * Constructor for an empty <code>BezierShape</code>. The color will
	 * be 0 and it will not be closed by default.
	 * @param capacity Reserve memory for at least this many bezier curves to
	 *                 improve performance
	 */
	public BezierShape(int capacity){
		super(capacity);
	}
	
	/**
	 * Constructs a <code>BezierShape</code> from the provided list, marking it
	 * closed if the last point of the last bezier equals the first point of the
	 * first bezier
	 * @param path series of bezier curves
	 */
	public BezierShape(List<BezierCurve> path){
		super(path);
		this.closedLoop = this.get(this.size()-1).getP4().equals(this.get(0).getP1());
	}
	
	/**
	 * Constructs a <code>BezierShape</code> from the provided array, marking it
	 * closed if the last point of the last bezier equals the first point of the
	 * first bezier
	 * @param path series of bezier curves
	 */
	public BezierShape(BezierCurve... path){
		super(path.length);
		for(int i = 0; i < path.length; i++){
			this.add(path[i]);
		}
		this.closedLoop = this.get(this.size()-1).getP4().equals(this.get(0).getP1());
	}
	
	/**
	 * Returns the "color" of this shape, which is the value from which this shape
	 * was traced. Default is zero if no color information is available.
	 * @return Color (eg ARGB integer)
	 */
	public int getColor(){return color;}
	
	/**
	 * Sets the "color" of this shape.
	 * @param argb Color (eg ARGB integer)
	 */
	public void setColor(int argb){this.color = argb;}
	
	/**
	 * A closed shape is one that loops back on itself to connect the start and
	 * end points together.
	 * @return <code>true</code> if this shape has been marked as a closed loop,
	 * <code>false</code> if it has not.
	 */
	public boolean isClosed(){return closedLoop;}
	
	/**
	 * Sets whether this series of bezier curves is meant to enclose an area or
	 * be treated as a line.
	 * @param closed <code>true</code> if this shape is meant to be a closed loop,
	 * <code>false</code> if it is not.
	 */
	public void setClosed(boolean closed){this.closedLoop = closed;}
	
	/**
	 * Returns a string representation of the Bezier path
	 * @return Series of control points with SVG annotations
	 */
	@Override public String toString(){
		return this.toSVGPathString();
	}
	
	/**
	 * Constructs the SVG 1.1 path descriptor string to represent how to draw this
	 * shape in an SVG DOM. Absolute coordinates are used.
	 * @return the <code>d="..."</code> string for a <code>&lt;path/&gt;</code>
	 * element
	 */
	public String toSVGPathString(){
		Vec2 start = null;
		var sb = new StringBuilder();
		for(var p : this){
			if(!p.getP1().equals(start)){
				// gap in path
				sb.append(String.format("M %f %f", p.getP1().x, p.getP1().y));
			}
			if(p.getP1().equals(p.getP2()) && p.getP4().equals(p.getP3())){
				// is a straight line
				sb.append(String.format(" L %f %f ", p.getP4().x, p.getP4().y));
			} else {
				// cubic spline
				sb.append(String.format(" C %f,%f %f,%f %f,%f",
						p.getP2().x, p.getP2().y,
						p.getP3().x, p.getP3().y,
						p.getP4().x, p.getP4().y));
			}
			start = p.getP4();
		}
		if(this.isClosed()) sb.append(" Z");
		return sb.toString();
	}

	/**
	 * Scales this shape around the origin point
	 * @param scalar The scaling value
	 * @param origin The center point to preserve (use (0,0) as the origin if performing a global scaling operation)
	 */
	public void scale(double scalar, Vec2 origin){
		for(int i = 0; i < this.size(); i++){
			this.set(i, this.get(i).scaleAroundPoint(scalar, origin));
		}
	}
	
	/**
	 * Translates this <code>BezierShape</code> along the specified vector
	 * @param translation Translation vector
	 */
	public void translate(Vec2 translation){
		for(int i = 0; i < this.size(); i++){
			this.set(i, this.get(i).translate(translation));
		}
	}
}
