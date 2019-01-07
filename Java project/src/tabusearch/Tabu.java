package tabusearch;

import java.util.LinkedList;
import java.util.Random;

// Class with static fields for holding important search information 
// and static methods for performing typical processes of Tabu search.
public class Tabu {
	
	public static boolean verbose; // True: print progress events of the search.
	public static int num_evals = 0; // Counter for number of objective function evaluations
	public static int eval_limit; // Convergence criterion on the number of objective function evaluations permitted
	public static LinkedList<Point> globSearchHist; // Object to store the entire Tabu search history as a linked list.
	public static Point globalMinPoint; // Current Point with the minimum associate objective function value
	public static Point startingPoint; // Force the local search to begin from this point
	public static int dim; // Input dimension
	public static double constraint; // Upper limit on variable magnitude
	public static Function myFunc; // Function to minimise
	public static double stepSize; // Starting step size for the Tabu local search
	public static double stepReduceFactor; // Constant factor to reduce the step size by after step-size reduction 
	public static Random generator; // Random generator
	public static MTM mtmObj;
	public static LTM ltmObj;
	public static String searchType; // Type of local search to conduct: "initial", "intensify", "diversify" or "ssr"
	public static int counter = 0; // Counter for number of iterations without improvement to minimum value found
	public static int intensifyThresh; // Counter threshold to intensify search
	public static int diversifyThresh; // Counter threshold to diversify search
	public static int ssrThresh; // Counter threshold to perform step-size reduction
	
	// Set the seed of the random generator shared across classes
	public static void setGeneratorSeed(long seed) {
		generator = new Random(seed);
	}
	
	// Check whether this point corresponds the best solution found so far and store it if so 
	public static void checkGlobalMin(Point currentPoint) throws CloneNotSupportedException {
		if (globalMinPoint == null) {
			globalMinPoint = currentPoint.clone();
		}
		else if (currentPoint.fval < globalMinPoint.fval) {
			globalMinPoint = currentPoint.clone();
		} 
	}
	
	// Generate a random input point
	public static Point genRandomPoint() {
		double[] x = new double[dim];
		for (int i = 0; i < x.length; i++) {
			x[i] = generator.nextDouble() * (2*constraint) - constraint;
		} 

		return new Point(x, myFunc);
	}
	
	// Perform a complete Tabu search
	public static void doTabuSearch() throws CloneNotSupportedException {
		mtmObj = new MTM();
		ltmObj = new LTM();
		counter = 0; 
		searchType = "initialise";
		System.out.print("Starting a Tabu search. ");
		if (verbose == true) {
			System.out.println("Printing counter evolution and other search events.");
		}
		
		startingPoint = genRandomPoint(); 
		
		while (num_evals < eval_limit) {
			if (searchType.equals("initialise") && verbose == true) {
				System.out.print("\nInitial search: ");
			}
			
			// Intensify search
			if (searchType.equals("intensify")) {
				startingPoint = MTM.findMTMAvg(mtmObj);
				if (Tabu.verbose == true) {
					System.out.print("\nIntensiying search: ");
				}
			} // Intensify: set starting point to the meant point of the MTM
			
			// Diversify search
			else if (searchType.equals("diversify")) {
				startingPoint = ltmObj.genDiversifiedPoint(); 
			} // Diversify: generate a random point whose grid coordinates are not in the LTM
			
			// Reduce step size
			else if (searchType.equals("ssr")) {
				startingPoint = globalMinPoint; // Restart from the minimum point found so far
				stepSize = stepSize*stepReduceFactor; // Reduce the increment size by a constant factor
				counter = 0; // Reset counter
				if (Tabu.verbose == true) {
					System.out.print("\nReducing step size: ");
				}
			}
			
			// Perform a local search from startingPoint
			LocalSearch LSObj = new LocalSearch(stepSize); 
			LinkedList<Point> localSearchHist = LSObj.doLocalSearch(startingPoint); 
			globSearchHist.addAll(localSearchHist); // Append the local search history of points to the global search history
		}
	}

}
