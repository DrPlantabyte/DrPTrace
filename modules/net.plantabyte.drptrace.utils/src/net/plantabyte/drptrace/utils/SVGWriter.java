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

import net.plantabyte.drptrace.geometry.BezierShape;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * This is a utility class to help convert <code>BezierShape</code>s into SVG
 * image files.
 */
public class SVGWriter {
	/**
	 * This exception wraps various kinds of exceptions that may be thrown when
	 * writing an SVG file.
	 */
	public static class SVGWriterException extends Exception{
		/**
		 * Exception constructor
		 * @param message error message
		 * @param cause The actual exception that caused the problem
		 */
		public SVGWriterException(String message, Throwable cause){
			super(message, cause);
		}
	}
	
	/**
	 * Converts a list of <code>BezierShape</code> objects (such as would be returned by
	 * <code>Tracer.traceAllShapes(...)</code>) into SVG XML and writes it to the
	 * given <code>OutputStream</code>
	 * @param bezierPaths a list of <code>BezierShape</code> objects
	 * @param width SVG image width (in pixels)
	 * @param height SVG image height (in pixels)
	 * @param out An <code>OutputStream</code> to write to (such as
	 *            <code>Files.newOutputStream(...)</code> or a
	 *            <code>ByteArrayOutputStream</code>)
	 * @throws SVGWriterException Thrown if any of the operations fail, with the
	 * intitial thrown exception being set as the cause of this excption.
	 */
	public static void writeToSVG(List<BezierShape> bezierPaths, final int width, final int height, OutputStream out)
			throws SVGWriterException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = factory.newDocumentBuilder();
			//
			var doc = dBuilder.newDocument();
			var root = doc.createElementNS("http://www.w3.org/2000/svg", "svg");
			root.setAttribute("width", String.format("%s", width));
			root.setAttribute("height", String.format("%s", height));
			root.setAttribute("viewBox", String.format("0 0 %s %s", width, height));
			root.setAttribute("version", "1.1");
			root.setAttribute("id", "svgroot");
			doc.appendChild(root);
			var group = doc.createElement("g");
			group.setAttribute("id", "mainGroup");
			root.appendChild(group);
			int id = 0;
			for(var s : bezierPaths) {
				if(s.size() == 0) continue;
				final int color = s.getColor();
				String hexColor = intToHexColor(color);
				var path = doc.createElement("path");
				path.setAttribute("id", String.format("path%s", ++id));
				path.setAttribute("style", String.format(
						"fill:%s;stroke-width:1;stroke-linecap:round;stroke:%s;stroke-opacity:1",
						hexColor, hexColor
				));
				path.setAttribute("d", svgPathString(s));
				group.appendChild(path);
			}
			//
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			
			StreamResult result = new StreamResult(out);
			
			//t.setParameter(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(source, result);
		} catch(TransformerException|ParserConfigurationException e){
			throw new SVGWriterException("Failed to write XML document", e);
		}
	}
	
	private static String svgPathString(final BezierShape s) {
		return s.toSVGPathString();
	}
	
	private static String intToHexColor(int argb){
		return "#"
				//.concat(leftpad(Integer.toHexString((argb >> 24) & 0xFF), '0', 2))
				.concat(leftpad(Integer.toHexString((argb >> 16) & 0xFF), '0', 2))
				.concat(leftpad(Integer.toHexString((argb >>  8) & 0xFF), '0', 2))
				.concat(leftpad(Integer.toHexString((argb      ) & 0xFF), '0', 2));
	}
	private static String leftpad(String x, char pad, int width){
		if(x.length() >= width) return x;
		char[] padder = new char[width-x.length()];
		Arrays.fill(padder, pad);
		return new String(padder).concat(x);
	}
}
