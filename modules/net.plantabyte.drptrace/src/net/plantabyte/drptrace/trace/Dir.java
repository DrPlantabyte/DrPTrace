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
package net.plantabyte.drptrace.trace;

/**
 * This enum is used to track the direction of the tracing state machine
 */
public enum Dir{
	UP(0), LEFT(1), DOWN(2), RIGHT(3), NONE(4);
	private byte index;
	Dir(int index){
		this.index = (byte)index;
	}
	private static Dir[] counterClockwiseArray = {UP,LEFT,DOWN,RIGHT};
	
	/**
	 * Rotate the direction counter-clockwise (eg up  -> left -> down -> right)
	 * @return the new Dir
	 */
	public Dir rotateCounterClockwise(){
		return counterClockwiseArray[(this.index + 1) % 4];
	}
	
	/**
	 * Rotate the direction clockwise (eg up  -> right -> down -> left)
	 * @return the new Dir
	 */
	public Dir rotateClockwise(){
		return counterClockwiseArray[(this.index + 3) % 4];
	}
}
