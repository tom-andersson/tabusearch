package tabusearch;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

// Class with static fields for holding important search information 
// and static methods for performing typical processes of Tabu search.
public class Tabu {
	
	
	
	// Inner class for performing a local search (with access to all enclosing class members)
	class LocalSearch {
		
		
		
		// Inner class for the short-term memory (STM)
		class STM {
			public int stmSize = Tabu.stmSize; // STM size
			private LinkedList<Point> stmList = new LinkedList<Point>(); // LinkedList variable
			
			public void tryAddToSTM(Point currentPoint) throws CloneNotSupportedException {
				// If the STM is not yet full
				if (stmList.size() < stmSize) { 
					stmList.add(currentPoint.clone()); // Add the current point to the STM
				}
				else {
					stmList.offerFirst(currentPoint.clone());
					stmList.removeLast();
				} // Replace with the current point in the STM on a first in, first out basis
			}
		}
		
		
		
		// LocalSearch members ----------------------------------------------------------------------------------------
		
		public LinkedList<Point> localSearchHist = new LinkedList<Point>(); // List to store history of points in the local search
		// Local history of objective function value zero-held with # of function evaluations
		public LinkedList<Double> localfEvolZeroHold = new LinkedList<Double>(); 
		STM stmObj = new STM(); 
		private double stepSize; // Step size 
		private Point currentPoint; // Point object corresponding to the current position of the local search
		private int nEvalsLastIter; // Number of function evaluations during the previous iteration
		private int num_its = 0; // Total number of local search iterations

		// Constructor for creating a LocalSearch object
		public LocalSearch(double stepSize) {
			this.stepSize = stepSize;
		}

		// Generate a linked list of points around 'currentPoint' to test
		// This is done by incrementing and decrementing each variable of the current point
		// The testList object is reused by reference to avoid frequent expensive object creation 
		private void updateTestList(LinkedList<double[]> testList, Point currentPoint, double stepSize) {
			testList.clear(); // Remove all elements from the list
			
			// Populate the list
			for (int i = 0; i < currentPoint.x.length; i++) {
				double[] xTemp1 = currentPoint.x.clone();
				xTemp1[i] += stepSize;
				testList.add(xTemp1);

				double[] xTemp2 = currentPoint.x.clone();
				xTemp2[i] -= stepSize;
				testList.add(xTemp2);
			}
		}
		
		// Generate list to store the permitted non-tabu moves that are within the feasible region
		// The validList object is reused by reference to avoid frequent expensive object creation 
		private void updateValidList(LinkedList<double[]> testList,LinkedList<Point> validList, STM STMObj) {
			validList.clear(); // Remove all elements from the list
			
			for (double[] testEl : testList) {			
				boolean validCheck1 = true; // Feasible region check
				for (int i = 0; i < testEl.length; i++) {
					if (Math.abs(testEl[i]) > constraint) {
						validCheck1 = false;
					} // "If any of the variables violate the constraints"
				}
				
				boolean validCheck2 = true; // Non-tabu check
				if (validCheck1 == true) {
					for (Point stmEl : STMObj.stmList) {
						if (Arrays.equals(testEl,stmEl.x)) {
							validCheck2 = false;
						} // "If the test point is in the STM"
					}
				}
				
				if (validCheck1==true && validCheck2==true) {
					validList.add(new Point(testEl,myFunc));
				}
			}
		}
		
		// Make the best allowed move of the current and return true of the objective function was reduced
		private boolean makeBestAllowedMove(LinkedList<Point> validList) throws CloneNotSupportedException {
			boolean functionReduced = false;
			double prevFval = currentPoint.fval;
			
			if (validList.peekFirst() == null) {
				// This is very unlikely to occur unless the input space is 1D 
				// and the search has reached the feasible region boundary
				// or if the stepSize is below double precision (~1E-16)
				System.out.println("Error: There are no valid, non-tabu moves in the current local search.");
				System.exit(0); 
			}
			else {
				// Find the element with minimum function value
				double min = validList.peekFirst().fval;
				int minloc = 0;
				for (int i = 0; i < validList.size(); i++) {
					Point p = validList.get(i);
					if (p.fval < min) {
						min = p.fval;
						minloc = i; 
					}
				}
				currentPoint = validList.get(minloc).clone(); // Move the current point to the best valid position
				
				// If the function is reduced by the step, see if it is the new local minimum value
				if (currentPoint.fval < prevFval) {
					functionReduced = true; 
				}
			}
			
			return functionReduced;
		}
		

		// Attempt a pattern move and execute it if it improves upon the minimum of the local search
		private void attemptPatternMove(double[] xBase, Point currentPoint, STM stmObj) throws CloneNotSupportedException {
			double[] xCurrent = currentPoint.x;
			double[] xPattern = new double[xCurrent.length];
			
			// Perform a pattern move by repeating the vector from the last base point
			for (int i = 0; i < xPattern.length; i++) {
				xPattern[i] = xCurrent[i] + (xCurrent[i] - xBase[i]);
			}
			
			// Check whether the pattern move is still in the feasible region
			boolean validCheck = true; // Feasible region check
			for (int i = 0; i < xPattern.length; i++) {
				if (Math.abs(xPattern[i]) > constraint) {
					validCheck = false;
				}
			}
			
			if (validCheck == true) {
				// If the objective function is reduced, store the intermediate Tabu move and retain the pattern move
				double fPattern = myFunc.f(xPattern);
				if (fPattern < currentPoint.fval) {
					storePoint(currentPoint);
					currentPoint = new Point();
					currentPoint.x = xPattern.clone();
					currentPoint.fval = fPattern;
				}
			}
		}
		
		// Attempt to store a clone of the current Point object in the various memory objects
		private void storePoint(Point currentPoint) throws CloneNotSupportedException {
			// Update best solution if the best objective has been reduced
			checkBestSolution(currentPoint);

			// Zero hold the function evolution and best solution values by 
			// the number of evaluations since the last point was stored
			nEvalsLastIter = numEvals - numEvalsLagged; 
			for (int i = 0; i < nEvalsLastIter; i++) {
				localfEvolZeroHold.add(currentPoint.fval); // Add the current point to the local search history
				globMinValZeroHold.add(bestSolution.fval); // Add the current optimal solution
			}
			
			localSearchHist.add(currentPoint.clone()); // Add the current point to the local search history
			// See if the point should be stored in the STM, the MTM or the global minimum
			stmObj.tryAddToSTM(currentPoint);
			mtmObj.tryAddToMTM(currentPoint);
			// Attempt to store current point in LTM after an integer number of iterations,
			// where the integer is the number of Tabu steps that fit within each grid segment.
			if (num_its % ltmUpdateRate == 0) {
				ltmObj.storeInLTM(currentPoint.x);
			}
			numEvalEvolution.add(numEvals); // Record number of objective function evaluations
			
			numEvalsLagged = numEvals; // Number of evaluations since previous stored point
		}
		
		// Perform a local search starting at startingPoint and ending when the counter threshold
		// has been reached
		public void doLocalSearch(Point startingPoint) throws CloneNotSupportedException {
					
			// Initialisation
			currentPoint = startingPoint.clone(); // Generate the initial point
			storePoint(currentPoint);
			
			LinkedList<double[]> testList = new LinkedList<double[]>(); // List of coordinates to check for validity
			LinkedList<Point> validList = new LinkedList<Point>(); // List of points corresponding to valid Tabu moves 
			int counterThresh; // Counter limit for this local search
			
			if (searchType.matches("initialise|ssr")) {
				counterThresh = intensifyThresh;
			}
			else if (searchType.equals("intensify")) {
				counterThresh = diversifyThresh;
			}
			else {
				counterThresh = ssrThresh;
			}
			
			// Begin local search
			while (counter < counterThresh) {
				if (verbose == true) {
					System.out.print(counter + " ");
				}
				
				double[] xBase = currentPoint.x.clone(); // Update the base point
				updateTestList(testList,currentPoint,stepSize);	
				updateValidList(testList,validList,stmObj);	
				boolean functionReduced = makeBestAllowedMove(validList); // Move currentPoint by best allowed move

				// Reset the counter if a new global minimum value has been found
				if (currentPoint.fval < bestSolution.fval) {
					counter = 0; 
				}
				else {
					counter += 1; // Increment the counter
				}
				
				storePoint(currentPoint);
				
				// If the objective function was reduced, attempt a pattern move
				if (functionReduced == true) {
					attemptPatternMove(xBase, currentPoint, stmObj);
				}
				
				num_its++;
				if (num_its > 1000000) {
					System.out.println("Error: Excessive number of local search iterations reached without triggering counter threshold.");
					System.exit(0);
				}
				
			}
			
			if (searchType.matches("initialise|ssr")) {
				searchType = "intensify";
			}
			else if (searchType.equals("intensify")) {
				searchType = "diversify";
			}
			else {
				searchType = "ssr";
			}
		}
	}
	
	
	
	//// Tabu members ----------------------------------------------------------------------------------------
	
	// Shared static variables
	public static boolean verbose; // True: print progress events of the search.
	public static int evalLimit; // Convergence criterion on the number of objective function evaluations permitted
	public static int dim; // Input dimension
	public static double constraint; // Upper limit on variable magnitude
	public static Function myFunc; // Function to minimise
	public static double startingStepSize; // Starting step size for the Tabu local search
	public static double stepLimit; // Lower limit on the step size
	public static double stepReduceFactor; // Constant factor to reduce the step size by after step-size reduction 
	public static long seed; // Rng seed
	public static int stmSize; // Short-term memory (STM) size
	public static int intensifyThresh; // Counter threshold to intensify search
	public static int diversifyThresh; // Counter threshold to diversify search
	public static int ssrThresh; // Counter threshold to perform step-size reduction
	public static Random generator; // Random generator (shared across the package)
	public static int numEvals; // Counter for number of objective function evaluations

	// Instance variables
	private int ltmUpdateRate; // Local search iterations per update to the LTM
	public Point bestSolution; // Current Point with the minimum associate objective function value
	private Point startingPoint = new Point(); // Force the local search to begin from this point
	public double stepSize; // Variable step size
	private MTM mtmObj = new MTM();
	private LTM ltmObj = new LTM();
	private int numEvalsLagged = 0; // Number of function evaluations up to the previous Tabu iteration
	private String searchType; // Type of local search to conduct: "initial", "intensify", "diversify" or "ssr"
	public LinkedList<Point> globSearchHist = new LinkedList<Point>(); // Object to store the entire Tabu search history as a linked list
	public LinkedList<Double> globfEvolZeroHold= new LinkedList<Double>(); // History zero held with # of function evaluations
	public LinkedList<Integer> numEvalEvolution = new LinkedList<Integer>(); // Evolution of the number of function evaluations
	public LinkedList<Double> globMinValZeroHold = new LinkedList<Double>(); // History of optimal solution found so far (zero held)
	private int counter; // Counter for number of iterations without improvement to minimum value found

	// Generate a random input point
	public static Point genRandomPoint() {
		double[] x = new double[dim];
		for (int i = 0; i < x.length; i++) {
			x[i] = generator.nextDouble() * (2*constraint) - constraint;
		} 

		return new Point(x, myFunc);
	}
	
	// Check whether this point corresponds the best solution found so far and store it if so 
	private void checkBestSolution(Point currentPoint) throws CloneNotSupportedException {
		if (bestSolution == null) {
			bestSolution = currentPoint.clone();
		}
		else if (currentPoint.fval < bestSolution.fval) {
			bestSolution = currentPoint.clone();
		} 
	}
	
	// Perform a complete Tabu search
	public void doTabuSearch() throws CloneNotSupportedException {
		// Initialisation
		generator = new Random(seed);
		numEvals = 0;
		counter = 0; 
		stepSize = startingStepSize;
		ltmUpdateRate = (int) Math.ceil(LTM.getSegSize()/(2*stepSize));
		searchType = "initialise";
		if (verbose == true) {
			System.out.println("\n\nStarting a Tabu search. Printing counter evolution and other search events.");
		}
		
		startingPoint = genRandomPoint(); 
		
		while (numEvals < evalLimit) {
			if (searchType.equals("initialise") && verbose == true) {
				System.out.print("Initial search: ");
			}
			
			// Intensify search
			if (searchType.equals("intensify")) {
				startingPoint = MTM.findMTMAvg(mtmObj);
				if (verbose == true) {
					System.out.print("\nIntensiying search: ");
				}
			} // Intensify: set starting point to the meant point of the MTM
			
			// Diversify search
			else if (searchType.equals("diversify")) {
				startingPoint = ltmObj.genDiversifiedPoint(); 
			} // Diversify: generate a random point whose grid coordinates are not in the LTM
			
			// Reduce step size
			else if (searchType.equals("ssr")) {
				startingPoint = bestSolution; // Restart from the minimum point found so far
				// If the step size limit will not be violated, reduce the step size
				if (stepSize*stepReduceFactor > stepLimit) {
					stepSize = stepSize*stepReduceFactor; // Reduce the increment size by a constant factor
				}
				counter = 0; // Reset counter
				ltmUpdateRate = (int) Math.ceil(LTM.getSegSize()/(2*stepSize));
				if (verbose == true) {
					System.out.print("\nReducing step size: ");
				}
			}
			
			// Perform a local search from startingPoint
			LocalSearch LSObj = new LocalSearch(stepSize); 
			LSObj.doLocalSearch(startingPoint); 
			globSearchHist.addAll(LSObj.localSearchHist); // Append the local search history of points to the global search history
			globfEvolZeroHold.addAll(LSObj.localfEvolZeroHold); // Append zero-held objective history
		}
	}
}
