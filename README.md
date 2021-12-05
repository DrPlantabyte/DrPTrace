# DrPTrace
A bitmap image vector tracing library for Java!

![explain_sm](https://user-images.githubusercontent.com/1922739/144734948-4645da2c-a602-4728-a148-85e59ba8f619.png)


## About
DrPTrace is a pure Java library for converting raster arrays (aka bitmap images) into vector paths, similar to Peter Selinger's Potrace library. 

DrPTrace has two JPMS modules: `net.plantabyte.drptrace` and `net.plantabyte.drptrace.utils`. Only `net.plantabyte.drptrace` is required to use this library and it has no external dependencies (it only requires the `java.base` module). However, if you are working with `BufferedImage` objects or saving SVG XML files, then the utility functions in `net.plantabyte.drptrace.utils` will be very helpful to you (`net.plantabyte.drptrace.utils` depends on `java.xml` and `java.desktop`). 

## How it Works
DrPTrace starts with a bitmap image (stored as an `IntMap` data class) and then iteratively searched for patches of pixels sharing the same color. For each patch, DrPTrace follows the outer edge and records a coordinate point on each pixel edge. It then divides the point path into several bezier curves and uses a parameter solver to optimize the placement of the control points to fit the point path. 
![explain1_sm](https://user-images.githubusercontent.com/1922739/144734949-3ccabc1b-dadc-44c7-8067-e286679322a6.png)
![explain2_sm](https://user-images.githubusercontent.com/1922739/144734950-cb69f418-503b-4f16-8242-e5b99ab0d3e2.png)
![explain3_sm](https://user-images.githubusercontent.com/1922739/144734954-e0e68057-e8a9-49d2-95c7-40f268f87290.png)
![explain4_sm](https://user-images.githubusercontent.com/1922739/144734956-9bfff2b5-16c2-4ce6-a3eb-1d9cb2a8d71b.png)


## Usage Examples

Using DrPTrace has three steps:
1. Trasfer your raster data to an `IntMap`
2. Use `Tracer.traceAllShapes(IntMap, int)` to vectorize the raster
3. Convert the resulting `BezierShapes` into whatever format you intend to store the bezier curve vectors (typically SVG XML)

### Fit a bezier curve to a series of points

```java
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import net.plantabyte.drptrace.*;
import net.plantabyte.drptrace.geometry.*;
import net.plantabyte.drptrace.intmaps.*;
import net.plantabyte.drptrace.utils.*;

public class Main {
	public static void main(String[] args){
		int h = 100, w = 200;
		List<Vec2> pathPoints = new ArrayList<>();
		double r = 33.0, centerX = 50, centerY = 50;
		for(double theta = 0; theta < Math.PI; theta += 0.2){
			var p = new Vec2(Math.round(-r * Math.sin(2*theta) + centerX), Math.round(r*Math.cos(theta) + centerY));
			pathPoints.add(p);
		}
		var bimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		var g2d = bimg.createGraphics();
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0,0,w,h);
		g2d.setColor(Color.GRAY);
		for(var p : pathPoints){
			g2d.fillRect((int)(p.x-1), (int)(p.y-1), 3, 3);
		}
		showImg(bimg);
		var b = new BezierCurve(new Vec2(centerX, centerY-r), new Vec2(centerX, centerY+r));
		print("fitting...");
		b.fitToPoints(pathPoints);
		print("...done fitting");
		BezierPlotter.drawBezierWithControlPoints(b,g2d,Color.BLUE, Color.RED);
		showImg(bimg);
	}
	private static void showImg(final BufferedImage bimg) {
		JOptionPane.showMessageDialog(null, new JLabel(new ImageIcon(bimg)));
	}
}
```

### Generate a raster and then save it as a SVG vector image:

```java
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import net.plantabyte.drptrace.*;
import net.plantabyte.drptrace.geometry.*;
import net.plantabyte.drptrace.intmaps.*;

public class Main {
	public static void main(String[] args){
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
		Tracer tracer = new Tracer();
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
}
```

### Convert an image to SVG image

```java
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import net.plantabyte.drptrace.*;
import net.plantabyte.drptrace.geometry.*;
import net.plantabyte.drptrace.intmaps.*;
import net.plantabyte.drptrace.utils.*;

public class Main {
	public static void main(String[] args){
		final int smoothness = 10;
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
			System.exit(0);
		}
	}
}

```

## Building DrPTrace

`javac`, `javadoc`, and `jar` arguments are provided in the corresponding *-args.txt files. Thus you can easily build the project with the following java commands in the main project directory:

```bash
javac @javac-args.txt
jar @jar-core-args.txt
jar @jar-utils-args.txt
javadoc @javadoc-args.txt
```
