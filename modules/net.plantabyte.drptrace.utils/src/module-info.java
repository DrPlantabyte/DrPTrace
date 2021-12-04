/**
 * The <code>net.plantabyte.drptrace.utils</code> module provides various integration
 * tools to facilitate using DrPTrace with common Java libraries (e.g. AWT/Swing and
 * javax.XML)
 */
module net.plantabyte.drptrace.utils {
	exports net.plantabyte.drptrace.utils;
	requires net.plantabyte.drptrace;
	requires java.desktop;
	requires java.xml;

}