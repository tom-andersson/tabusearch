package tabusearch;

import java.util.LinkedList;

// Perform the Tabu Search algorithm on the Schwefel Function

public class TSMain {

	// Main method
	public static void main(String[] args) throws CloneNotSupportedException {

		// Using polymorphism to allow for the objective functions to be easily changed here
		Function myFunc = new Schwef();
		final int dim = 2; // Input dimension
		myFunc.setDim(dim); // Set static dimension variable of the the SchwefelFunction class
		
		// Algorithm parameters to be defined
		final double constraint = 500.0; // Upper limit on variable magnitude
		final int N = 4; // Short-term memory (STM) size
		final int M = 4; // Medium-term memory (MRM) size
		double stepSize = 0.5; // Starting step size for the Tabu local search
		final double stepLimit = stepSize/4; // Convergence criterion - stop when stepSize is smaller than stepLimit
		long seed = 100; // Rng seed
		
		// Setting up the Tabu class
		Tabu.mtmObj = new Tabu.MTM(M);
		Tabu.globSearchHist = new LinkedList<Point>(); // Initialise global search history list 

		// Setting up the LocalSearch class
		LocalSearch.stmSize = N;
		LocalSearch.myFunc = myFunc;
		LocalSearch.constraint = constraint;
		LocalSearch.dim = dim;

		// Conduct local searches while the increment size is not below the limit (i.e. until convergence)
		while (stepSize >= stepLimit) {
			LocalSearch LSObj = new LocalSearch(stepSize,seed);
			LinkedList<Point> localSearchHist = LSObj.doSearch();
			Tabu.globSearchHist.addAll(localSearchHist); // Append the local search history of points to the global search history
			stepSize = stepSize/2; // Half the increment size
			seed += 1; // Start from a new location
		}
		
		// TODO: Output global search history list to a JSON and plot the coordinates in python
		
		// PLAN:
		// For now do not use intensification or diversification so that local search can be tested
		
		// Need to make counter, MTM and currentMin static variables of a new public class holding public search information
		
		// After that, search logic should be:
		// Do local loop. When counter reaches MTM limit exit with an exit code indicating whether MTM is triggered or if
		// too many iterations were reached.
		// If too many iterations were reached, print error message and quit.
		// If MTM is triggered, start local search from avg of MTM 
		// Need to update LocalSearch to have optional string specifying the type of local search (initial, intensify or diversify)
		// and be able to take a specified starting point for MTM rather than randomly generating one
		// if MTM boolean is true, the counter limit is now set to diversify limit
		// When diversify limit is reached set limit to REDUCE and sample from uncharted space
		// When REDUCE is reached reduce step size and restart from best solution found so far

	}

}
