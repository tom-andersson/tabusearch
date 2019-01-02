package tabusearch;

// Perform the Tabu Search algorithm on the Schwefel Function

public class TSMain {
	
	// Main method
	public static void main(String[] args) {
		
		SchwefelFunction schwef = new SchwefelFunction(2); // Create SchwefelFunction object of dim
		
		double[] x = {1.2,-4.3};
		double funcval = schwef.f(x);
		
		System.out.println("The Schwefel function value is: " + funcval);
		
	}

}
