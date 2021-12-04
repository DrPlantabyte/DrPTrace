package net.plantabyte.drptrace.utils;

import imagemagick.Quantize;
import net.plantabyte.drptrace.Tracer;
import net.plantabyte.drptrace.geometry.BezierShape;
import net.plantabyte.drptrace.geometry.ZOrderIntMap;

import java.awt.image.BufferedImage;
import java.util.*;

public class ImageTracer {
	public static List<BezierShape> traceBufferedImage(
			BufferedImage img, int smoothness, int numColors
			) {
		int[][] imgMatrix = new int[img.getHeight()][img.getWidth()];
		for(int y = 0; y < img.getHeight(); y++){
			for(int x = 0; x < img.getWidth(); x++){
				imgMatrix[y][x] = img.getRGB(x, y);
			}
		}
		if(numColors > 0) {
			var palette = Quantize.quantizeImage(imgMatrix, numColors);
			for(int y = 0; y < img.getHeight(); y++){
				for(int x = 0; x < img.getWidth(); x++){
					imgMatrix[y][x] = palette[imgMatrix[y][x]];
				}
			}
		}
		Tracer t = new Tracer();
		return t.traceAllShapes(ZOrderIntMap.fromMatrix(imgMatrix), smoothness);
	}
	
	public static List<BezierShape> traceBufferedImage(
			BufferedImage img, int smoothness
	) {
		Tracer t = new Tracer();
		return t.traceAllShapes(new BufferedImageIntMap(img), smoothness);
	}
}
