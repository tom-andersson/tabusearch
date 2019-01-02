package tabusearch;

import java.lang.Math;
import java.util.ArrayList;

// Class to evaluate the Schwefel Function of dimension N

public class SchwefelFunction {
	
	private int N; // Number of dimensions of the Schwefel function
	
	public SchwefelFunction(int N) {
		this.N = N;
	} // Constructor to set the number of dimensions for the object
	
	public double f(ArrayList<Double> x) {
		double funcval = 0.0;
		int N = this.N;
		
		if (x.size() == N) {
			for (Double xi : x) {
				funcval = funcval - xi*Math.sin(Math.sqrt(Math.abs(xi)));
			}
		}
		else {
			System.out.println("Error: Dimension of input point must match the Schwefel Function dimension.");
			System.exit(0);
		}
		
		return funcval;
	} // Method to evaluate and return the Schwefel function.

}
