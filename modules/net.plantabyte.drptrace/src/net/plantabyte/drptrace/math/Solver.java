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
package net.plantabyte.drptrace.math;

import java.util.function.Function;

/**
 * A generic function parameter optimizer class.
 */
public abstract class Solver {
	/**
	 * Optimizes the provided parameter array to maximize the output of the provided function
	 * @param func The scoring function to maximize, which must be able to take
	 *             <code>initialParams</code> as it's input argument
	 * @param initialParams Initial parameter values
	 * @return Optimized parameter values
	 */
	public abstract double[] maximize(Function<double[], Double> func, double[] initialParams);
	/**
	 * Optimizes the provided parameter array to minimize the output of the provided function
	 * @param func The scoring function to minimize, which must be able to take
	 *             <code>initialParams</code> as it's input argument
	 * @param initialParams Initial parameter values
	 * @return Optimized parameter values
	 */
	public double[] minimize(Function<double[], Double> func, double[] initialParams){
		Function<double[], Double> minus = (double[] params) -> -1 * func.apply(params);
		return maximize(minus, initialParams);
	}
}
