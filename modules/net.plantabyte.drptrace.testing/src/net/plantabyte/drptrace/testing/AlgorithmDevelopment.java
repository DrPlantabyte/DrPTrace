package net.plantabyte.drptrace.testing;

import net.plantabyte.drptrace.utils.*;

import com.github.weisj.jsvg.parser.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;

public class AlgorithmDevelopment {
	public static void main(String[] args){
		try {
			for(int n = 1; n <= 1; n++) {
				testOnImage(getTestImage(n));
			}
			// TODO: test algorithms
		} catch(Exception ex) {
			ex.printStackTrace(System.err);
			System.exit(1);
		}
	}

	private static void testOnImage(BufferedImage testImage) throws Exception{
		showImg(testImage, 4);
		var trace = ImageTracer.traceBufferedImage(testImage, 10);
		var svg = new ByteArrayOutputStream();
		SVGWriter.writeToSVG(trace, testImage.getWidth(), testImage.getHeight(), svg);
		var renderer = new SVGLoader().load(new ByteArrayInputStream(svg.toByteArray()));
		var canvas = new BufferedImage(testImage.getWidth(), testImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
		renderer.render(null, canvas.createGraphics());
		showImg(canvas, 4);
	}

	private static BufferedImage getTestImage(int number) throws IOException {
		String rsrc = String.format("test-img-%s.png", number);
		return ImageIO.read(AlgorithmDevelopment.class.getResource(rsrc));
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
