package tabusearch;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

// Class with static fields and methods for performing a local search as part of Tabu Search.

public class LocalSearch {
	
	// Inner class for the short-term memory (STM)
	private class STM {
		private final int N; // STM size
		private LinkedList<Point> stmList; // LinkedList variable
		
		// Constructor to initialise the STM array 
		public STM(int N) {
			this.N = N;
			stmList = new LinkedList<Point>(); // Point the array variable at an empty ArrayList object
		}
		
		public int getN() {
			return this.N;
		}
		
		public void tryAddToSTM(Point currentPoint) throws CloneNotSupportedException {
			// If the STM is not yet full
			if (stmList.size() < N) { 
				stmList.add(currentPoint.clone()); // Add the current point to the STM
			}
			else {
				stmList.offerFirst(currentPoint);
				stmList.removeLast();
			} // Replace with the current point in the STM on a first in, first out basis
		}
	}
	
	// Instance variables for this local search object
	public final double stepSize; // Step size 
	public final long seed; // Rng seed 
	public LinkedList<Point> localSearchHist = new LinkedList<Point>(); // List to store entire history of points in the local search
	private Point currentPoint; // Point object corresponding to the current position of the local search
	private double localMinVal; // The current minimum function value for the local search
	// Static variables shared by all instances of this class (i.e. all separate local searches)
	public static int dim; // Input dimension
	public static int stmSize; // Size of the short-term memory
	public static Function myFunc; // Function to minimise
	public static double constraint; // Upper limit on variable magnitude
	
	// Constructor for creating a LocalSearch object
	public LocalSearch(double stepSize, long seed) {
		this.stepSize = stepSize;
		this.seed = seed;
	}
	
	// TODO: Move this to a static method of a new class with ability to diversify using LTM of grid sectors already visited 
	// Generate a random input point
	private Point genRandomPoint() {
		Random gen = new Random(seed); // Unif[0,1) rng
		double[] x = new double[dim];
		for (int i = 0; i < x.length; i++) {
			x[i] = gen.nextDouble() * (2*constraint) - constraint;
		}
		Point point = new Point(x, myFunc);
		
		return point;
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
			currentPoint = validList.get(minloc).clone(); // Move the current point to the best valid position
			
			// If the function is reduced by the step, see if it is the new local minimum value
			if (currentPoint.fval < prevFval) {
				functionReduced = true; 
				if (currentPoint.fval < localMinVal) {
					localMinVal = currentPoint.fval;
				}
			}
			}
		}
		
		return functionReduced;
	}
	

	// Attempt a pattern move and execute it if it improves upon the minimum of the local search
	private boolean attemptPatternMove(double[] xBase, Point currentPoint, STM stmObj) throws CloneNotSupportedException {
		double[] xCurrent = currentPoint.x;
		double[] xPattern = new double[xCurrent.length];
		
		// Perform a pattern move by repeating the vector from the last base point
		for (int i = 0; i < xPattern.length; i++) {
			xPattern[i] = xCurrent[i] + (xCurrent[i] - xBase[i]);
		}
		
		// If the objective function is reduced, store the intermediate Tabu move retain the pattern move
		double fPattern = myFunc.f(xPattern);
		boolean didPattern = false;
		if (fPattern < currentPoint.fval) {
			storePoint(currentPoint, stmObj);
			currentPoint = new Point();
			currentPoint.x = xPattern.clone();
			currentPoint.fval = fPattern;
			didPattern = true;
		}
		
		return didPattern;
	}
	
	// Attempt to store a clone of the current Point object in the various memory objects
	private void storePoint(Point currentPoint, STM stmObj) throws CloneNotSupportedException {
		localSearchHist.add(currentPoint.clone()); // Add the current point to the local search history
		// See if the point should be stored in the STM, the MTM or the global minimum
		stmObj.tryAddToSTM(currentPoint);
		Tabu.mtmObj.tryAddToMTM(currentPoint);
		Tabu.checkGlobalMin(currentPoint);
	}
	
	// TODO: Explain what this method does
	public LinkedList<Point> doSearch() throws CloneNotSupportedException {
		
		int counter = 0; // Number of iterations for which there has been no improvement on the minimum
		
		STM stmObj = new STM(stmSize);  
		LinkedList<Point> stmList = stmObj.stmList;
		
		// Initialisation
		currentPoint = genRandomPoint(); // Generate the initial point
		localSearchHist.add(currentPoint.clone()); // Add the initial point to the local search history
		localMinVal = currentPoint.fval; // Initialise the minimum value found in the local search
		LinkedList<double[]> testList = new LinkedList<double[]>(); // List of coordinates to check for being validity
		LinkedList<Point> validList = new LinkedList<Point>(); // List of points corresponding to valid Tabu moves 
		
		// Begin local search
		while (counter < 10) {
			double[] xBase = currentPoint.x.clone(); // Update the base point
			updateTestList(testList,currentPoint,stepSize);	// Update testList by incrementing and decrementing each variable
			updateValidList(testList,validList,stmObj);	// Update corresponding to valid non-tabu moves
			boolean functionReduced = makeBestAllowedMove(validList); // Make the best allowed move
			// If the objective function was reduced, attempt a pattern move
			if (functionReduced == true) {
				boolean didPattern = attemptPatternMove(xBase, currentPoint, stmObj);
				if (didPattern == true) {
					// ?
				}
				// Reset the counter if a new local minimum value has been found
				if (currentPoint.fval < localMinVal) {
					counter = 0; 
					localMinVal = currentPoint.fval;
				}
			}
			else {
				counter += 1; // Increment the counter
			}
			
			storePoint(currentPoint,stmObj);
		}
		
		System.out.println("Local search terminated due to counter exceeding limit");

		// TODO: Exit if total number of iterations becomes excessive
		
		return localSearchHist;
	}
}
