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
package net.plantabyte.drptrace.geometry;

/**
 * Immutagble 2D point data class
 */
public final class Vec2 {
	/** X value */
	public final double x;
	/** Y value */
	public final double y;
	
	/**
	 * Standard constructor
	 * @param x X value
	 * @param y Y value
	 */
	public Vec2(double x, double y){
		this.x = x;
		this.y = y;
	}
	/** origin point */
	public static final Vec2 ORIGIN = new Vec2(0,0);
	
	/**
	 * Creates a string representation of the coordinates
	 * @return the coordinates as a sctring
	 */
	@Override public String toString(){
		return String.format("(%f, %f)", x, y);
	}
	
	/**
	 * Multiplies this Vec2 by a scalar value
	 * @param scalar value to multiply by
	 * @return Multiplied Vec2 result
	 */
	public Vec2 mul(double scalar){
		return new Vec2(this.x * scalar, this.y * scalar);
	}
	
	/**
	 * Adds Vec2 v to this Vec2
	 * @param v Vec2 to add to this one
	 * @return Result of adding the two vectors
	 */
	public Vec2 add(Vec2 v){
		return new Vec2(this.x + v.x, this.y+v.y);
	}
	
	/**
	 * Subtract Vec2 v from this Vec2
	 * @param v Vec2 to subtract from this one
	 * @return Result of subtracting the two vectors
	 */
	public Vec2 sub(Vec2 v){
		return new Vec2(this.x - v.x, this.y-v.y);
	}
	
	/**
	 * Returns the squared distance from this Vec2 to v
	 * @param v other Vec2 point
	 * @return distance squared
	 */
	public double distSquared(Vec2 v){
		double dx = v.x - this.x;
		double dy = v.y - this.y;
		return dx*dx + dy*dy;
	}
	/**
	 * Returns the distance from this Vec2 to v
	 * @param v other Vec2 point
	 * @return distance
	 */
	public double dist(Vec2 v){
		return Math.sqrt(distSquared(v));
	}

	/**
	 * Returns the angle between two vectors
	 * @param a a vector
	 * @param b another vector
	 * @return The angle in radians, ranging from 0 to PI
	 */
	public static double angle(Vec2 a, Vec2 b){
		return Math.acos(dotProduct(a,b) / (a.magnitude() * b.magnitude()));
	}

	/**
	 * Calculates the angle between two vectors drwon from this vector coordinate to the two provided. In other words,
	 * this method calculates the angle ABC, where this Vec2 is coordinate B
	 * @param A coordinate of one end of the angle
	 * @param C coordinate of the other end of the angle
	 * @return Angle between A and C around this point (point B in angle ABC)
	 */
	public double angleBetween(Vec2 A, Vec2 C){
		return angle(A.sub(this), C.sub(this));
	}
	/**
	 * Computes the dot product of two vectors
	 * @param a a vector
	 * @param b another vector
	 * @return the dot product of the two vectors
	 */
	public static double dotProduct(Vec2 a, Vec2 b){
		return a.x * b.x + a.y * b.y;
	}

	/**
	 * Returns the magnitude of this vector
	 * @return the magnitude of this vector
	 */
	public double magnitude(){
		return this.dist(ORIGIN);
	}

	/**
	 * Computes the dot product of this vector and another vector
	 * @param b another vector
	 * @return the dot product of the two vectors
	 */
	public double dot(Vec2 b){
		return dotProduct(this, b);
	}
	
	/**
	 * Checks equality with another Vec2
	 * @param other Other object
	 * @return Returns tru if this Vec2 has same x and y coordinates as other
	 * Vec2 (returns false if other is not a Vec2)
	 */
	@Override public boolean equals(Object other){
		return other == this || (other instanceof Vec2 && this.x == ((Vec2)other).x && this.y == ((Vec2)other).y );
	}
	
	/**
	 * Generates a hash code
	 * @return a hash code
	 */
	@Override public int hashCode(){
		return 2003 * Double.hashCode(this.y) + 1999 * Double.hashCode(this.x);
	}
}
