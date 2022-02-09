/**
 * This package contains the main high-level DrPTrace API, with data classes and
 * implementation details stored in sub-packages.
 *
 * Tracing is done with a Tracer class, such as <code>IntervalTracer</code> class, which requires an <code>IntMap</code> representing
 * the raster image you wish to trace. The <code>net.plantabyte.drptrace.utils</code>
 * module provides convenience utilities to further simplify usage.
 *
 * Example usage: tracing a red target
 <code>
 <br>
 // initialize raster with red target pattern<br>
 int w = 200, h = 100;<br>
 int pixelsPerNode = 10;<br>
 ZOrderIntMap raster = new ZOrderIntMap(w, h);<br>
 for(int y = 0; y &lt; h; y++){<br>
 &nbsp;&nbsp;for(int x = 0; x &lt; w; x++){<br>
 &nbsp;&nbsp;&nbsp;&nbsp;double r = Math.sqrt(Math.pow(x-w/2, 2) + Math.pow(y-h/2, 2));<br>
 &nbsp;&nbsp;&nbsp;&nbsp;if(r &lt; 10 || (r &gt; 20 &amp;&amp; r &lt; 30)){<br>
 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;raster.set(x, y, Color.RED.getRGB());<br>
 &nbsp;&nbsp;&nbsp;&nbsp;} else {<br>
 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;raster.set(x, y, Color.WHITE.getRGB());<br>
 &nbsp;&nbsp;&nbsp;&nbsp;}<br>
 &nbsp;&nbsp;}<br>
 }<br>
 // trace the raster to vector shapes<br>
 Tracer tracer = new IntervalTracer(pixelsPerNode);<br>
 final List&lt;BezierShape&gt; bezierShapes =<br>
 tracer.traceAllShapes(raster);<br>
 // write to SVG file<br>
 try(BufferedWriter out = Files.newBufferedWriter(<br>
 Paths.get("target.svg"), StandardCharsets.UTF_8)<br>
 ){<br>
 &nbsp;&nbsp;out.write("&lt;?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?&gt;");<br>
 &nbsp;&nbsp;out.write(String.format("&lt;svg width=\"%s\" height=\"%s\" id=\"svgroot\" version=\"1.1\" viewBox=\"0 0 %s %s\" xmlns=\"http://www.w3.org/2000/svg\"&gt;", w, h, w, h));<br>
 &nbsp;&nbsp;for(BezierShape shape : bezierShapes) {<br>
 &nbsp;&nbsp;&nbsp;&nbsp;Color c = new Color(shape.getColor());<br>
 &nbsp;&nbsp;&nbsp;&nbsp;String hexColor = String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());<br>
 &nbsp;&nbsp;&nbsp;&nbsp;out.write(String.format(<br>
 &nbsp;&nbsp;&nbsp;&nbsp;"&lt;path style=\"fill:%s\" d=\"%s\" /&gt;",<br>
 &nbsp;&nbsp;&nbsp;&nbsp;hexColor, shape.toSVGPathString()));<br>
 &nbsp;&nbsp;}<br>
 &nbsp;&nbsp;out.write("&lt;/svg&gt;");<br>
 } catch(IOException e){<br>
 &nbsp;&nbsp;e.printStackTrace(System.err);<br>
 }
 </code>
 */
package net.plantabyte.drptrace;
