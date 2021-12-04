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

import imagemagick.Quantize;
import net.plantabyte.drptrace.Tracer;
import net.plantabyte.drptrace.geometry.BezierShape;
import net.plantabyte.drptrace.intmaps.ZOrderIntMap;

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
