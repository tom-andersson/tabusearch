package tabusearch;

import java.lang.Math;

// Class containing functions which we seek to minimise.

public class Schwef implements Function {
		
	private int dim; // Input dimension

	// Setter for the dimension variable 'dim'
	public void setDim(int dim) {
		this.dim = dim;

		if (dim <= 0) {
			System.out.println("Error: Input dimension must be a positive integer.");
			System.exit(0);
		}
	}

	// Method to evaluate and return the Schwefel function (dim must be defined).
	public double f(double[] x) {

		double funcval = 0.0;

		if (x.length == dim) {
			for (Double xi : x) {
				funcval = funcval - xi * Math.sin(Math.sqrt(Math.abs(xi)));
			}
		} else if (dim == 0) {
			System.out.println("Error: You must initialise the input dimension before attempting evaluation.");
			System.exit(0);
		} else {
			System.out.println("Error: Dimension of input point must match the defined input dimension.");
			System.exit(0);
		}

		return funcval;
	}

}


