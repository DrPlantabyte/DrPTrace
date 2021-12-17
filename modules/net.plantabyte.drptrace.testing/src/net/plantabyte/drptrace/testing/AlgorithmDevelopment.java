package net.plantabyte.drptrace.testing;

import net.plantabyte.drptrace.geometry.Vec2;
import net.plantabyte.drptrace.utils.*;

import com.github.weisj.jsvg.parser.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.Optional;
import java.util.stream.Collectors;

public class AlgorithmDevelopment {
	public static void main(String[] args){
		try {
			for(int n = 1; n <= 4; n++) {
				testOnImage(getTestImage(n));
			}
			// TODO: test algorithms
		} catch(Exception ex) {
			ex.printStackTrace(System.err);
			System.exit(1);
		}
	}

	private static void testOnImage(BufferedImage testImage) throws Exception{
		final int magnify = Math.max(1, 550 / Math.max(testImage.getWidth(), testImage.getHeight()));
		print("Tracing image...");
		var trace = ImageTracer.traceBufferedImage(testImage, 10);
		trace.stream().forEach((var s) -> s.scale(magnify, Vec2.ORIGIN));
		//

		var lineOverlayed = scaleImage(testImage, magnify);
		var brush = lineOverlayed.createGraphics();
		trace.stream().forEach(
				(var shape) -> shape.stream().forEach(
						(var bezier) -> BezierPlotter.drawBezierWithControlPoints(bezier, brush, Color.BLUE, Color.RED)
				)
		);
		//
		print("Renderring SVG output...");
		var svg = new ByteArrayOutputStream();
		SVGWriter.writeToSVG(trace, testImage.getWidth()*magnify, testImage.getHeight()*magnify, svg);
		var renderer = new SVGLoader().load(new ByteArrayInputStream(svg.toByteArray()));
		var canvas = new BufferedImage(testImage.getWidth()*magnify, testImage.getHeight()*magnify, BufferedImage.TYPE_INT_ARGB);
		renderer.render(null, canvas.createGraphics());
		//
		print("...Done!");
		showImg(scaleImage(testImage, magnify), lineOverlayed, canvas);
	}

	private static BufferedImage getTestImage(int number) throws IOException {
		String rsrc = String.format("test-img-%s.png", number);
		return ImageIO.read(AlgorithmDevelopment.class.getResource(rsrc));
	}

	private static void showImg(final BufferedImage... bimg) {
		showImg(1, bimg);
	}
	private static void showImg(final int mag, final BufferedImage... bimg) {
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
		for(var img : bimg){
			pane.add(new JLabel(new ImageIcon(scaleImage(img, mag))));
		}
		JOptionPane.showMessageDialog(null, pane);
	}

	private static BufferedImage scaleImage(final BufferedImage bimg, int mag){
		if(mag <= 1) mag = 1;
		int w = bimg.getWidth();
		int h = bimg.getHeight();
		BufferedImage bimg2 = new BufferedImage(w*mag, h*mag, BufferedImage.TYPE_INT_ARGB);
		AffineTransform at = new AffineTransform();
		at.scale(mag, mag);
		AffineTransformOp scaleOp =
				new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		bimg2 = scaleOp.filter(bimg, bimg2);
		return bimg2;
	}

	private static void print(Object... args) {
		for(var arg : args){
			System.out.print(String.valueOf(arg));
			System.out.print(' ');
		}
		System.out.println();
	}
}
