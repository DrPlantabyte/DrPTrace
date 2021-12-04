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
package net.plantabyte.drptrace.utils;

import net.plantabyte.drptrace.geometry.BezierCurve;
import net.plantabyte.drptrace.geometry.Vec2;

import java.awt.*;
import java.util.Optional;

public class BezierPlotter {
	
	public static void drawBezierWithControlPoints(BezierCurve b, Graphics2D g, Color ptColor, Color lineColor){
		double r = 2;
		Vec2[] parr = {b.getP1(), b.getP2(), b.getP3(), b.getP4()};
		g.setColor(ptColor);
		g.drawLine((int)parr[0].x, (int)parr[0].y, (int)parr[1].x, (int)parr[1].y);
		g.drawLine((int)parr[3].x, (int)parr[3].y, (int)parr[2].x, (int)parr[2].y);
		for(var p : parr) {
			g.drawOval((int) (p.x - r), (int) (p.y - r), (int) (2 * r), (int) (2 * r));
		}
		double sumDist = 0;
		for(int i = 0; i < 3; i++){
			sumDist += parr[i].dist(parr[i+1]);
		}
		drawBezier(b, g, Optional.of(lineColor), Optional.empty());
	}
	
	public static void drawBezier(BezierCurve b, Graphics2D g){
		drawBezier(b, g, Optional.empty(), Optional.empty());
	}
	public static void drawBezier(BezierCurve b, Graphics2D g, Optional<Color> lineColor, Optional<Stroke> stroke){
		Vec2[] parr = {b.getP1(), b.getP2(), b.getP3(), b.getP4()};
		double sumDist = 0;
		for(int i = 0; i < 3; i++){
			sumDist += parr[i].dist(parr[i+1]);
		}
		if(lineColor.isPresent()) g.setColor(lineColor.get());
		if(stroke.isPresent()) g.setStroke(stroke.get());
		int count = (int)Math.min(sumDist+1, 1000);
		for(int i = 0; i < count; i++){
			double t0 = (double)i / (double)count;
			double t1 = (double)(i+1) / (double)count;
			var p0 = b.f(t0);
			var p1 = b.f(t1);
			g.drawLine((int)p0.x, (int)p0.y, (int)p1.x, (int)p1.y);
		}
	}
}