package tabusearch;

import java.lang.Math;

// Class to evaluate the Schwefel Function of dimension N, which we seek to minimise

public class Schwef {
	
	public static int dim; // Number of dimensions of the Schwefel function
	
	// Method to evaluate and return the Schwefel function.
	public static double f(double[] x) {
		
		double funcval = 0.0;
		
		if (x.length == dim) {
			for (Double xi : x) {
				funcval = funcval - xi*Math.sin(Math.sqrt(Math.abs(xi)));
			}
		}
		else {
			System.out.println("Error: Dimension of input point must match that of the Schwefel Function dimension.");
			System.exit(0);
		}
		
		return funcval;
	} 

}
