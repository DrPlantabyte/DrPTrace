package net.plantabyte.drptrace.geometry;

import java.util.ArrayList;
import java.util.List;

public class BezierSeries extends ArrayList<BezierCurve> {
	public BezierSeries(){
		super();
	}
	public BezierSeries(int capacity){
		super(capacity);
	}
	public BezierSeries(List<BezierCurve> path){
		super(path);
	}
	
	public BezierSeries(BezierCurve... path){
		super(path.length);
		for(int i = 0; i < path.length; i++){
			this.add(path[i]);
		}
	}
}
