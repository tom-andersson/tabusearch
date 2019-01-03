package tabusearch;

// Perform the Tabu Search algorithm on the Schwefel Function

public class TSMain {
	
	// Main method 
	public static void main(String[] args) {
		
		Schwef.dim = 2; // Set static dimension variable of the the SchwefelFunction class
		
		double[] x = {1.2,4.3};
		
		double funcval = Schwef.f(x);
		
		System.out.println("The Schwefel function value is: " + funcval);
		
	}

}
