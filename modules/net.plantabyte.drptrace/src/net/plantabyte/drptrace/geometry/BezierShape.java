package net.plantabyte.drptrace.geometry;

import java.util.ArrayList;
import java.util.List;

public class BezierShape extends ArrayList<BezierCurve> {
	private int color = 0;
	private boolean closedLoop = false;
	
	public BezierShape(){
		super();
	}
	public BezierShape(int capacity){
		super(capacity);
	}
	public BezierShape(List<BezierCurve> path){
		super(path);
	}
	
	public BezierShape(BezierCurve... path){
		super(path.length);
		for(int i = 0; i < path.length; i++){
			this.add(path[i]);
		}
	}
	
	public int getColor(){return color;}
	public void setColor(int argb){this.color = argb;}
	
	public boolean isClosed(){return closedLoop;}
	public void setClosed(boolean closed){this.closedLoop = closed;}
}
