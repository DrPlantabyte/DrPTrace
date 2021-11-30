package net.plantabyte.drptrace.testing;

import net.plantabyte.drptrace.Tracer;
import net.plantabyte.drptrace.geometry.BezierCurve;
import net.plantabyte.drptrace.geometry.Vec2;
import net.plantabyte.drptrace.utils.BezierPlotter;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Main {
	public static void main(String[] args){
		System.out.println("Starting...");
		for(int i = 0; i < args.length; i++){
			System.out.println(i+": "+args[i]);
		}
		//
		
//		test1();
//		test2();
		test3();
		System.exit(0);
	}
	
	private static void test3() {
		print("Test 3");
		int h = 100, w = 200;
		var bimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		var brush = bimg.createGraphics();
		brush.setColor(Color.WHITE);
		brush.fillRect(0,0,w,h);
		brush.setColor(Color.GRAY);
		//
		List<Vec2> pathPoints = new ArrayList<>();
		double r = 33.0, centerX = 50, centerY = 50;
		for(double theta = 0; theta < 2*Math.PI; theta += 0.2){
			var p = new Vec2(
					Math.round(-r * Math.sin(theta) + 0.3*r*Math.sin(3.3*theta) + centerX),
					Math.round(r*Math.cos(theta) + centerY));
			pathPoints.add(p);
		}
		for(var p : pathPoints){
			brush.fillRect((int)(p.x-1), (int)(p.y-1), 3, 3);
		}
		showImg(bimg, 4);
		var tracer = new Tracer();
		List<BezierCurve> trace = tracer.tracePointPath(pathPoints.toArray(new Vec2[0]), 5);
		for(var b : trace){
			BezierPlotter.drawBezierWithPoints(b, brush, Color.BLUE, Color.RED);
		}
		showImg(bimg, 4);
	}
	
	private static void test1() {
		print("Test 1");
		int h = 100, w = 200;
		var bimg1 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		var painter1 = bimg1.createGraphics();
		painter1.setColor(Color.WHITE);
		painter1.fillRect(0,0,w,h);
		painter1.setColor(Color.BLACK);
		painter1.fillOval(10, 20, 30, 40);
		var b = new BezierCurve(new Vec2(25, 20), new Vec2(10, 20), new Vec2(10, 60), new Vec2(25,60));
		BezierPlotter.drawBezierWithPoints(b, painter1, Color.BLUE, Color.RED);
		showImg(bimg1);
	}
	
	private static void test2() {
		print("Test 2");
		int h = 100, w = 200;
		List<Vec2> pathPoints = new ArrayList<>();
		double r = 33.0, centerX = 50, centerY = 50;
		for(double theta = 0; theta < Math.PI; theta += 0.2){
			var p = new Vec2(Math.round(-r * Math.sin(2*theta) + centerX), Math.round(r*Math.cos(theta) + centerY));
			pathPoints.add(p);
		}
		var bimg2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		var painter2 = bimg2.createGraphics();
		painter2.setColor(Color.WHITE);
		painter2.fillRect(0,0,w,h);
		painter2.setColor(Color.GRAY);
		for(var p : pathPoints){
			painter2.fillRect((int)(p.x-1), (int)(p.y-1), 3, 3);
		}
		showImg(bimg2);
		var b = new BezierCurve(new Vec2(centerX, centerY-r), new Vec2(centerX, centerY+r));
		print("fitting...");
		b.fitToPoints(pathPoints);
		print("...done fitting");
		BezierPlotter.drawBezierWithPoints(b,painter2,Color.BLUE, Color.RED);
		showImg(bimg2);
		System.out.println("...Done");
	}
	
	private static void showImg(final BufferedImage bimg) {
		JOptionPane.showMessageDialog(null, new JLabel(new ImageIcon(bimg)));
	}
	private static void showImg(final BufferedImage bimg, int mag) {
		int w = bimg.getWidth();
		int h = bimg.getHeight();
		BufferedImage bimg2 = new BufferedImage(w*mag, h*mag, BufferedImage.TYPE_INT_ARGB);
		AffineTransform at = new AffineTransform();
		at.scale(mag, mag);
		AffineTransformOp scaleOp =
				new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		bimg2 = scaleOp.filter(bimg, bimg2);
		JOptionPane.showMessageDialog(null, new JLabel(new ImageIcon(bimg2)));
	}
	
	private static void print(Object... args) {
		for(var arg : args){
			System.out.print(String.valueOf(arg));
			System.out.print(' ');
		}
		System.out.println();
	}
}