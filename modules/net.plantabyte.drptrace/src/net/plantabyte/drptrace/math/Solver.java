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
