/**
 * This package contains the main high-level DrPTrace API, with data classes and
 * implementation details stored in sub-packages.
 *
 * Tracing is done with the <code>Tracer</code> class, which requires an <code>IntMap</code> representing
 * the raster image you wish to trace. The <code>net.plantabyte.drptrace.utils</code>
 * module provides convenience utilities to further simplify usage.
 *
 * Example usage: tracing a red target
 <pre>
// initialize raster with red target pattern
int w = 200, h = 100;
int pixelsPerNode = 10;
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
Paths.get("target.svg"), StandardCharsets.UTF_8)
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
  out.write("&lt;/svg&gt;");
} catch(IOException e){
  e.printStackTrace(System.err);
}
 </pre>
 */
package net.plantabyte.drptrace;
