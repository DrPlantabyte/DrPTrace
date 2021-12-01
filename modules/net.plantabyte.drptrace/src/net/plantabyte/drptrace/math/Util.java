package net.plantabyte.drptrace.math;

import net.plantabyte.drptrace.geometry.Vec2;

public class Util {
	
	public static double distFromPointToLineSegment(Vec2 L1, Vec2 L2, Vec2 p){
		// vector AB
		Vec2 AB = new Vec2(
			L2.x - L1.x,
			L2.y - L1.y
		);
		// vector BP
		Vec2 BE = new Vec2(
			p.x - L2.x,
			p.y - L2.y
		);
		// vector AP
		Vec2 AE = new Vec2(
			p.x - L1.x,
			p.y - L1.y
		);
		// Variables to store dot product
		double AB_BE, AB_AE;
		// Calculating the dot product
		AB_BE = (AB.x * BE.x + AB.y * BE.y);
		AB_AE = (AB.x * AE.x + AB.y * AE.y);
		// Minimum distance from
		// point E to the line segment
		double dist = 0;
		// Case 1
		if (AB_BE > 0)
		{
			
			// Finding the magnitude
			double y = p.y - L2.y;
			double x = p.x - L2.x;
			dist = Math.sqrt(x * x + y * y);
		}
		// Case 2
		else if (AB_AE < 0)
		{
			double y = p.y - L1.y;
			double x = p.x - L1.x;
			dist = Math.sqrt(x * x + y * y);
		}
		// Case 3
		else
		{
			// Finding the perpendicular distance
			double x1 = AB.x;
			double y1 = AB.y;
			double x2 = AE.x;
			double y2 = AE.y;
			double mod = Math.sqrt(x1 * x1 + y1 * y1);
			dist = Math.abs(x1 * y2 - y1 * x2) / mod;
		}
		return dist;
	}
	
	/**
	 * Like Math.min(a, b), but for arrays
	 * @param darr array of values
	 * @return lowest value in the array
	 */
	public static double min(double[] darr){
		double m = darr[0];
		for(int i = 1; i < darr.length; i++){
			double n = darr[i];
			if(n < m){
				m = n;
			}
		}
		return m;
	}
	
	/**
	 * Like Math.max(a, b), but for arrays
	 * @param darr array of values
	 * @return highest value in the array
	 */
	public static double max(double[] darr){
		double m = darr[0];
		for(int i = 1; i < darr.length; i++){
			double n = darr[i];
			if(n > m){
				m = n;
			}
		}
		return m;
	}
	
	/**
	 * Returns the index of the highest value in the array
	 * @param darr array of values
	 * @return index in the array
	 */
	public static int indexOfMax(double[] darr){
		double m = darr[0];
		int index = 0;
		for(int i = 1; i < darr.length; i++){
			double n = darr[i];
			if(n > m){
				m = n;
				index = i;
			}
		}
		return index;
	}
	/**
	 * Returns the index of the lowest value in the array
	 * @param darr array of values
	 * @return index in the array
	 */
	public static int indexOfMin(double[] darr){
		double m = darr[0];
		int index = 0;
		for(int i = 1; i < darr.length; i++){
			double n = darr[i];
			if(n < m){
				m = n;
				index = i;
			}
		}
		return index;
	}
}