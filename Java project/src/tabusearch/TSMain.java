package tabusearch;

import java.util.*;

// Perform the Tabu Search algorithm on the Schwefel Function

public class TSMain {
	
	// Main method 
	public static void main(String[] args) {
		
		// Using polymorphism here to allow the objective functions to be easily changed
		Function myFunc = new Schwef();
		int dim = 2; // Input dimension
		myFunc.setDim(dim); // Set static dimension variable of the the SchwefelFunction class

		// Setting up and testing the STM
		int N = 3; // STM size
		STM STMObj = new STM(N);
		System.out.println(Arrays.toString(STMObj.STMArray));
		
		// Setting seed and generating initial point
		// Input constraints for the Schwefel function
		double max =  500.0; 
		double min =  -500.0; 
		long seed = 100; // Rng seed
		Random gen = new Random(seed); // Unif[0,1) rng
		double[] xInitial = new double[dim];
		for (int i = 0; i < xInitial.length; i++) {
			xInitial[i] = gen.nextDouble()*(max - min) + min;
		}
		System.out.println(Arrays.toString(xInitial));
		Point pointInitial = new Point(xInitial,myFunc);
		
	}
	
	private static void LocalSearch() {
		
	}

}
