package net.plantabyte.drptrace.testing;

import net.plantabyte.drptrace.geometry.BezierCurve;
import net.plantabyte.drptrace.geometry.Vec2;
import net.plantabyte.drptrace.utils.BezierPlotter;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Main {
	public static void main(String[] args){
		System.out.println("Starting...");
		for(int i = 0; i < args.length; i++){
			System.out.println(i+": "+args[i]);
		}
		//
		int h = 100, w = 200;
		var bimg1 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		var painter1 = bimg1.createGraphics();
		painter1.setColor(Color.WHITE);
		painter1.fillRect(0,0,w,h);
		painter1.setColor(Color.BLACK);
		painter1.fillOval(10, 20, 30, 40);
		var b = new BezierCurve(new Vec2(25, 20), new Vec2(10, 20), new Vec2(10, 60), new Vec2(25,60));
		BezierPlotter.drawBezierWithPoints(b, painter1, Color.BLUE, Color.RED);
		JOptionPane.showMessageDialog(null, new JLabel(new ImageIcon(bimg1)));
		
		var prng = new Random(12345);
		
		
		
		System.out.println("...Done");
	}
}