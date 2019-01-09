package tabusearch;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

// Class with static fields for holding important search information 
// and static methods for performing typical processes of Tabu search.
public class Tabu {
	
	public static boolean verbose; // True: print progress events of the search.
	public static int numEvals = 0; // Counter for number of objective function evaluations
	public static int numEvalsLagged = 0; // Number of function evaluations up to the previous Tabu iteration
	public static LinkedList<Integer> numEvalEvolution; // Evolution of the number of function evaluations
	public static int eval_limit; // Convergence criterion on the number of objective function evaluations permitted
	public static LinkedList<Point> globSearchHist; // Object to store the entire Tabu search history as a linked list
	public static LinkedList<Double> globfEvolZeroHold; // History zero held with # of function evaluations
	public static LinkedList<Double> globMinValZeroHold; // History of optimal solution found so far (zero held)
	public static Point globalMinPoint; // Current Point with the minimum associate objective function value
	public static Point startingPoint; // Force the local search to begin from this point
	public static int dim; // Input dimension
	public static double constraint; // Upper limit on variable magnitude
	public static Function myFunc; // Function to minimise
	public static double stepSize; // Starting step size for the Tabu local search
	public static double stepLimit; // Lower limit on the step size
	public static double stepReduceFactor; // Constant factor to reduce the step size by after step-size reduction 
	public static Random generator; // Random generator (shared across the package)
	public static long seed; // Rng seed
	public static MTM mtmObj;
	public static LTM ltmObj;
	public static int ltmUpdateRate; // Local search iterations per update to the LTM
	public static String searchType; // Type of local search to conduct: "initial", "intensify", "diversify" or "ssr"
	public static int counter = 0; // Counter for number of iterations without improvement to minimum value found
	public static int intensifyThresh; // Counter threshold to intensify search
	public static int diversifyThresh; // Counter threshold to diversify search
	public static int ssrThresh; // Counter threshold to perform step-size reduction
	
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
		// Initialisation
		globSearchHist = new LinkedList<Point>(); 
		globfEvolZeroHold = new LinkedList<Double>();
		globMinValZeroHold = new LinkedList<Double>();
		mtmObj = new MTM();
		ltmObj = new LTM();
		numEvalEvolution = new LinkedList<Integer>();
		generator = new Random(seed);
		numEvals = 0;
		counter = 0; 
		ltmUpdateRate = (int) Math.ceil(LTM.getSegSize()/(2*stepSize));
		searchType = "initialise";
		if (verbose == true) {
			System.out.println("Starting a Tabu search. Printing counter evolution and other search events.");
		}
		
		startingPoint = genRandomPoint(); 
		
		while (numEvals < eval_limit) {
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
				// If the step size limit will not be violated, reduce the step size
				if (stepSize*stepReduceFactor > stepLimit) {
					stepSize = stepSize*stepReduceFactor; // Reduce the increment size by a constant factor
				}
				counter = 0; // Reset counter
				ltmUpdateRate = (int) Math.ceil(LTM.getSegSize()/(2*stepSize));
				if (Tabu.verbose == true) {
					System.out.print("\nReducing step size: ");
				}
			}
			
			// Perform a local search from startingPoint
			LocalSearch LSObj = new LocalSearch(stepSize); 
			LSObj.doLocalSearch(startingPoint); 
			globSearchHist.addAll(LocalSearch.localSearchHist); // Append the local search history of points to the global search history
			globfEvolZeroHold.addAll(LocalSearch.localfEvolZeroHold); // Append zero held history
		}
	}

}
