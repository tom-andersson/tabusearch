package tabusearch;

import java.util.ArrayList;

// Perform the Tabu Search algorithm on the Schwefel Function

public class TSMain {
	
	// Main method 
	public static void main(String[] args) {
		
		Schwef.dim = 2; // Set the SchwefelFunction class static dimension variable
		
		ArrayList<Double> x = new ArrayList<Double>();
		x.add(1.2);
		x.add(-4.3);
		
		double funcval = Schwef.f(x);
		
		System.out.println("The Schwefel function value is: " + funcval);
		
	}

}
