package tabusearch;

import java.lang.Math;

// Class to evaluate the Schwefel Function of dimension N

public class SchwefelFunction {
	
	private int N; // Number of dimensions of the Schwefel function
	public double[] x; // Input point location
	
	public SchwefelFunction(int N) {
		this.N = N;
	} // Constructor to set the number of dimensions for the object
	
	public double f(double[] x) {
		double funcval = 0.0;
		int N = this.N;
		
		if (x.length == N) {
			for (int i = 0; i < N; i++) {
				funcval = funcval - x[i]*Math.sin(Math.sqrt(Math.abs(x[i])));
			}
		}
		else {
			System.out.println("Error: Dimension of input point must match the Schwefel Function dimension.");
			System.exit(0);
		}
		
		
		return funcval;
	} // Method to evaluate and return the Schwefel function.

}
