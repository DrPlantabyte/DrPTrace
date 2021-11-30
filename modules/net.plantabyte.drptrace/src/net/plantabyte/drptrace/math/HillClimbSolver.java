package net.plantabyte.drptrace.math;

import java.util.Arrays;
import java.util.function.Function;

public class HillClimbSolver extends Solver {
	private final double precision;
	private final int iterationLimit;
	
	/**
	 * Standard constructor
	 * @param precision The desired precision (epsilon) for parameter optimization
	 * @param iterationLimit The maximum number of iterations to use when optimizing parameters
	 */
	public HillClimbSolver(double precision, int iterationLimit){
		if(precision <= 0.0) throw new IllegalArgumentException("Precision must be greator than zero");
		this.iterationLimit = iterationLimit;
		this.precision = precision;
	}
	
	/**
	 * Constructor with default iteration limit (1 million iterations)
	 * @param precision The desired precision (epsilon) for parameter optimization
	 */
	public HillClimbSolver(double precision){
		this(precision, 1000000);
	}
	
	@Override
	public double[] maximize(
			final Function<double[], Double> func, final double[] initialParams
	) {
		final int numParams = initialParams.length;
		double[] params = Arrays.copyOf(initialParams, numParams);
		double[] jumpSizes = new double[numParams];
		Arrays.fill(jumpSizes, 16*precision);
		int iters = 0;
		double baseVal = func.apply(params);
		do {
			for(int i = 0; i < numParams; i++){
				double[] leftJump = Arrays.copyOf(params, numParams);
				leftJump[i] = leftJump[i] - jumpSizes[i];
				double[] leftLongJump = Arrays.copyOf(params, numParams);
				leftLongJump[i] = leftLongJump[i] - 2*jumpSizes[i];
				double[] rightJump = Arrays.copyOf(params, numParams);
				rightJump[i] = rightJump[i] + jumpSizes[i];
				double[] rightLongJump = Arrays.copyOf(params, numParams);
				rightLongJump[i] = rightLongJump[i] + 2*jumpSizes[i];
				double[][] pArray = {params, leftJump, rightJump, leftLongJump, rightLongJump};
				double[] valArray = {
						baseVal, func.apply(leftJump), func.apply(rightJump),
						func.apply(leftLongJump), func.apply(rightLongJump)
				};
				int bestIndex = Util.indexOfMax(valArray);
				baseVal = valArray[bestIndex];
				params = pArray[bestIndex];
				if(bestIndex == 0){
					// existing param already best, shrink step size
					jumpSizes[i] = jumpSizes[i] * 0.25;
				} else if(bestIndex > 2){
					// long jump gave best result, expand step size
					jumpSizes[i] = jumpSizes[i] * 4;
				}
			}
		} while(iters++ < iterationLimit && Util.max(jumpSizes) > precision);
		return params;
	}
}
