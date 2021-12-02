package net.plantabyte.drptrace.utils;

import net.plantabyte.drptrace.Tracer;
import net.plantabyte.drptrace.geometry.BezierCurve;
import net.plantabyte.drptrace.geometry.ZOrderBinaryMap;

import java.awt.image.BufferedImage;
import java.util.*;

public class ImageTracer {
	public static Collection<List<BezierCurve>> traceColor(int smoothness, BufferedImage img, int argb, boolean useAlpha){
		// First, convert image to bemap
		// TODO: optimize with direct raster access and z-order bitmap
		int mask = 0xFFFFFFFF;
		if(!useAlpha){
			mask = 0x00FFFFFF;
		}
		argb = argb & mask;
		var bitmap = new ZOrderBinaryMap(img.getWidth(), img.getHeight());
		for(int y = 0; y < img.getHeight(); y++){
			for(int x = 0; x < img.getWidth(); x++){
				bitmap.set(
						x, y,
						(img.getRGB(x, y) & mask) == argb ? (byte)1 : (byte)0
				);
			}
		}
		// then trace the paths
		Tracer tracer = new Tracer();
		return tracer.traceAllShapes(bitmap);
	}
}
