package net.plantabyte.drptrace.utils;

import net.plantabyte.drptrace.geometry.BezierShape;
import net.plantabyte.drptrace.geometry.Vec2;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

public class SVGWriter {
	
	public static class SVGWriterException extends Exception{
		public SVGWriterException(String message, Throwable cause){
			super(message, cause);
		}
	}
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
		Vec2 start = null;
		var sb = new StringBuilder();
		for(var p : s){
			if(!p.getP1().equals(start)){
				// gap in path
				sb.append(String.format("M %f %f", p.getP1().x, p.getP1().y));
			}
			if(p.getP1().equals(p.getP2()) && p.getP4().equals(p.getP3())){
				// is a straight line
				sb.append(String.format(" L %f %f ", p.getP4().x, p.getP4().y));
			} else {
				// cubic spline
				sb.append(String.format(" C %f,%f %f,%f %f,%f",
						p.getP2().x, p.getP2().y,
						p.getP3().x, p.getP3().y,
						p.getP4().x, p.getP4().y));
			}
			start = p.getP4();
		}
		if(s.isClosed()) sb.append(" Z");
		return sb.toString();
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
