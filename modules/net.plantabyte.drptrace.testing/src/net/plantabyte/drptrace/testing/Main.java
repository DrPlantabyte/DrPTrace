package net.plantabyte.drptrace.testing;

import net.plantabyte.drptrace.geometry.Vec2;

import javax.swing.*;
import java.awt.*;
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
		int h = 100, w = 200;
//		var bimg1 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
//		var painter1 = bimg1.createGraphics();
//		painter1.setColor(Color.WHITE);
//		painter1.fillRect(0,0,w,h);
//		painter1.setColor(Color.BLACK);
//		painter1.fillOval(10, 20, 30, 40);
//		var b = new BezierCurve(new Vec2(25, 20), new Vec2(10, 20), new Vec2(10, 60), new Vec2(25,60));
//		BezierPlotter.drawBezierWithPoints(b, painter1, Color.BLUE, Color.RED);
//		JOptionPane.showMessageDialog(null, new JLabel(new ImageIcon(bimg1)));
		
		List<Vec2> pathPoints = new ArrayList<>();
		double r = 33.0, centerX = 50, centerY = 50;
		for(double theta = 0; theta < Math.PI; theta += 0.2){
			var p = new Vec2(Math.round(-r * Math.sin(theta) + centerX), Math.round(r*Math.cos(theta) + centerY));
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
		JOptionPane.showMessageDialog(null, new JLabel(new ImageIcon(bimg2)));
		System.out.println("...Done");
	}
}