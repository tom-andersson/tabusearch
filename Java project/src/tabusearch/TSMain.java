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
		final int N = 3; // Short-term memory (STM) size

		// Setting up the LocalSearch class
		LocalSearch.STMSize = N;
		LocalSearch.myFunc = myFunc;
		LocalSearch.constraint = constraint;
		LocalSearch.dim = dim;

		// Initialise global search history list
		GlobalSearchHistory globSearchHistObj = new GlobalSearchHistory();
		LinkedList<Point> globSearchHist = globSearchHistObj.globSearchHist; // Point a variable at the search history LinkedList

		// Conduct a local search
		final double startingInc = 0.5; // Increment for the Tabu local search
		long seed = 100; // Rng seed
		LocalSearch LSObj = new LocalSearch(startingInc,seed);
		
		LSObj.doSearch();
		
		// PLAN:
		// For now do not use intensification or diversification so that local search can be tested

	}

}
