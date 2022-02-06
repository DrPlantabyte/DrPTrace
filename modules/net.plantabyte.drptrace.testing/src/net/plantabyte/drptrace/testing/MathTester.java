package net.plantabyte.drptrace.testing;

import net.plantabyte.drptrace.geometry.Vec2;
import net.plantabyte.drptrace.math.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class MathTester {
	public static void main(String[] args){
		var prng = new Random();
		int count = 20;
		double refSlope = (prng.nextDouble() - 0.5) * 2;
		double refOffset = prng.nextDouble()*2+4;
		System.out.println(String.format("Y = %.2fX + %.2f", refSlope, refOffset));
		var pts = new Vec2[count];
		final int pxPerUnit = 32;
		final double unitsPerPx = 1.0 / 32;
		final int iSize = pxPerUnit * 12;
		BufferedImage img = new BufferedImage(iSize, iSize, BufferedImage.TYPE_INT_ARGB);
		var g = img.createGraphics();
		g.setColor(Color.WHITE);
		g.fill(new Rectangle(0,0, iSize, iSize));
		g.setColor(Color.BLUE);
		g.drawLine(0, iSize-(int)(refOffset * pxPerUnit), (int)(12*pxPerUnit), iSize - (int)((12*refSlope + refOffset)*pxPerUnit));
		for(int i = 0; i < count; i++){
			double t = prng.nextDouble() * 10;
			double x = t + (prng.nextDouble()-0.5) * 2;
			double y = (refSlope * t + refOffset) + (prng.nextDouble()-0.5) * 2;
			pts[i] = new Vec2(x, y);
			g.drawOval((int)(x*pxPerUnit - 1), iSize - (int)(y*pxPerUnit-1), 3, 3);
			//System.out.println(String.format("%.2f, %.2f", x, y));
		}

		var result = Util.linearRegression(pts);
		System.out.println(String.format("Y'= %.2fX + %.2f\tRMSE=%.2f", result.slope, result.yOffset, result.rmse));

		g.setColor(Color.RED);
		g.drawLine(0, iSize-(int)(result.yOffset * pxPerUnit), (int)(12*pxPerUnit), iSize - (int)((12*result.slope + result.yOffset)*pxPerUnit));



		JOptionPane.showMessageDialog(null, new JLabel(new ImageIcon(img)));
		System.exit(0);
	}
}
