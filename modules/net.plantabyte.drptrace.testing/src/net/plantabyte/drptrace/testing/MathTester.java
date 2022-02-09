package net.plantabyte.drptrace.testing;

import net.plantabyte.drptrace.geometry.Vec2;

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

		var result = linearRegression(pts);
		System.out.println(String.format("Y'= %.2fX + %.2f\tRMSE=%.2f", result.slope, result.yOffset, result.rmse));
		g.setColor(Color.RED);
		g.drawLine(0, iSize-(int)(result.yOffset * pxPerUnit), (int)(12*pxPerUnit), iSize - (int)((12*result.slope + result.yOffset)*pxPerUnit));

		g.setColor(Color.MAGENTA);
		result = continuousLinearRegression(pts);
		System.out.println(String.format("Y\"= %.2fX + %.2f\tRMSE=%.2f", result.slope, result.yOffset, result.rmse));


		JOptionPane.showMessageDialog(null, new JLabel(new ImageIcon(img)));
		System.exit(0);
	}


	public static final class LineRegressionResult{
		public final double slope;
		public final double yOffset;
		public final double rmse;

		public LineRegressionResult(double slope, double yOffset, double rmse) {
			this.slope = slope;
			this.yOffset = yOffset;
			this.rmse = rmse;
		}
	}

	public static LineRegressionResult linearRegression(Vec2[] points){
		final int count = points.length;
		final double inverseCount = 1.0 / count;
		double xSum = 0;
		double ySum = 0;
		double SS_xy = 0;
		double SS_xx = 0;
		for(int i = 0; i < points.length; i++){
			final var p = points[i];
			xSum += p.x;
			ySum += p.y;
			SS_xy += p.x*p.y;
			SS_xx += p.x*p.x;
		}
		final double xMean = xSum / count;
		final double yMean = ySum / count;
		//
		SS_xy -= count * xMean * yMean;
		SS_xx -= count * xMean * xMean;
		final double slope = SS_xy / SS_xx;
		final double offset = yMean - slope * xMean;
		double se = 0;
		for(int i = 0; i < points.length; i++){
			final var p = points[i];
			final double e = p.x * slope + offset;
			se += e * e;
		}
		final double rmse = Math.sqrt(se/count);
		return new LineRegressionResult(slope, offset, rmse);
	}

	public static LineRegressionResult continuousLinearRegression(Vec2[] points){
		double slope = 0;
		double offset = 0;
		double rmse = 0; // Sorry, can't calc RMSE continuously
		double SS_xy = 0;
		double SS_xx = 0;
		double xMean = 0;
		double yMean = 0;
		int oldCount = 1;
		// testing single-pass algorithm
		for(int count = 1; count <= points.length; count++){
			final var p = points[count-1];
			//
			final double inverseCount = 1.0 / count;
			xMean = inverseCount * (xMean * oldCount + p.x);
			yMean = inverseCount * (yMean * oldCount + p.y);
			SS_xy += p.x*p.y;
			SS_xx += p.x*p.x;
			slope = (SS_xy - count * xMean * yMean) / (SS_xx - count * xMean * xMean);
			offset = yMean - slope * xMean;
			//
			oldCount = count;
		}
		rmse =  Double.NaN;

		return new LineRegressionResult(slope, offset, rmse);
	}

	public static class ContinuousLineRegression{
		private int count = 0;
		private double inverseCount = Double.NaN;
		private double xMean = 0;
		private double yMean = 0;

		public ContinuousLineRegression(){}

		public void addPoint(Vec2 pt){
			final int oldCount = count;
			count += 1;
			inverseCount = 1.0 / count;
			xMean = xMean * oldCount + pt.x * inverseCount;
			yMean = yMean * oldCount + pt.y * inverseCount;
			throw new UnsupportedOperationException("WIP");
		}
	}
}
