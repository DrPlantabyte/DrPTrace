package net.plantabyte.drptrace.testing;

import net.plantabyte.drptrace.IntervalTracer;
import net.plantabyte.drptrace.geometry.*;
import net.plantabyte.drptrace.intmaps.*;
import net.plantabyte.drptrace.utils.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {
	public static void main(String[] args){
		System.out.println("Starting...");
		for(int i = 0; i < args.length; i++){
			System.out.println(i+": "+args[i]);
		}
		//
		AlgorithmDevelopment.main(args);
		System.exit(0);
		//
		test1();
		test2();
		test3();
		test4();
		test5();
		test6();
		System.exit(0);
	}
	
	private static void test6() {
		print("Test 6");
		// initialize raster with red target pattern
		int w = 200, h = 100;
		int pixelsPerNode = 5;
		ZOrderIntMap raster = new ZOrderIntMap(w, h);
		for(int y = 0; y < h; y++){
			for(int x = 0; x < w; x++){
				double r = Math.sqrt(Math.pow(x-w/2, 2) + Math.pow(y-h/2, 2));
				if(r < 10 || (r > 20 && r < 30)){
					raster.set(x, y, Color.RED.getRGB());
				} else {
					raster.set(x, y, Color.WHITE.getRGB());
				}
			}
		}
		// trace the raster to vector shapes
		IntervalTracer tracer = new IntervalTracer();
		final List<BezierShape> bezierShapes =
				tracer.traceAllShapes(raster, pixelsPerNode);
		// write to SVG file
		try(BufferedWriter out = Files.newBufferedWriter(
				Paths.get("out.svg"), StandardCharsets.UTF_8)
		){
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
			out.write(String.format("<svg width=\"%s\" height=\"%s\" id=\"svgroot\" version=\"1.1\" viewBox=\"0 0 %s %s\" xmlns=\"http://www.w3.org/2000/svg\">", w, h, w, h));
			for(BezierShape shape : bezierShapes) {
				Color c = new Color(shape.getColor());
				String hexColor = String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
				out.write(String.format(
						"<path style=\"fill:%s\" d=\"%s\" />",
						hexColor, shape.toSVGPathString()));
			}
			out.write("</svg>");
		} catch(IOException e){
			e.printStackTrace(System.err);
		}
	}
	private static void test5() {
		print("Test 5");
		final int smoothness = 20;
		final int maxColors = 64;
		JFileChooser jfc = new JFileChooser();
		jfc.setDialogTitle("Select an image");
		jfc.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(final File f) {
				String n = f.getName().toLowerCase();
				return f.isDirectory() || n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".gif");
			}
			
			@Override
			public String getDescription() {
				return "Images (.png, .jpg, .gif)";
			}
		});
		if(jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
			var f = jfc.getSelectedFile();
			System.out.println("Tracing image "+f.toString()+"...");
			try {
				var bimg = ImageIO.read(f);
				var bezierShapes =
						ImageTracer.traceBufferedImage(bimg, smoothness,
								maxColors
						);
				SVGWriter.writeToSVG(bezierShapes, bimg.getWidth(), bimg.getHeight(),
						Files.newOutputStream(Paths.get("out.svg")));
			} catch(IOException | SVGWriter.SVGWriterException e) {
				e.printStackTrace(System.err);
				System.out.println("...operation failed. Abort.");
				System.exit(1);
			}
			System.out.println("...Done");
		}
	}
	private static void test4() {
		print("Test 4");
		int h = 100, w = 200;
		var bimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		var brush = bimg.createGraphics();
		brush.setColor(Color.WHITE);
		brush.fillRect(0,0,w,h);
		//
		brush.setColor(Color.BLACK);
		brush.fillOval(w/4,h/4,w/2,h/2);
		brush.fillOval(w/16,h/16,w/16,h/8);
		brush.setColor(Color.WHITE);
		brush.fillOval(w/4+4,h/4+4,w/2-8,h/2-8);
		brush.setColor(Color.BLACK);
		brush.fillOval(w/4+16,h/4+16,w/2-32,h/2-32);
		brush.setColor(Color.WHITE);
		brush.fillRect(w/2,h/4+16,1,1);
		brush.fillRect(w/2+1,h/4+16+1,1,1);
		brush.fillRect(w/2+2,h/4+16+2,1,1);
		brush.fillRect(w/2+2,h/4+16,1,1);
		brush.fillRect(w/2+3,h/4+16+1,1,1);
		brush.fillRect(w/2+4,h/4+16+2,1,1);
		//
		showImg(bimg, 4);
		//
		IntervalTracer tracer = new IntervalTracer();
		final int smoothness = 10;
		//var pp = tracer.followEdge(new BufferedImageIntMap(bimg),w/2, h/2);
		long t0 = System.currentTimeMillis();
		var results = tracer.traceAllShapes(new BufferedImageIntMap(bimg), smoothness);
		long t1 = System.currentTimeMillis();
		System.out.printf("Tracing took %s ms\n", (t1-t0));
		for(var e : results){
			int color = e.getColor();
			var trace = e;
			
			brush.setColor(Color.RED);
			for(var bezier : trace){
//				BezierPlotter.drawBezierWithControlPoints(bezier, brush, Color.BLUE, Color.RED);
				BezierPlotter.drawBezier(bezier, brush);
			}
		}
		showImg(bimg, 4);
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
		var tracer = new IntervalTracer();
		List<BezierCurve> trace = tracer.traceClosedPath(pathPoints.toArray(new Vec2[0]), 7);
		for(var b : trace){
			BezierPlotter.drawBezierWithControlPoints(b, brush, Color.BLUE, Color.RED);
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
		BezierPlotter.drawBezierWithControlPoints(b, painter1, Color.BLUE, Color.RED);
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
		BezierPlotter.drawBezierWithControlPoints(b,painter2,Color.BLUE, Color.RED);
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