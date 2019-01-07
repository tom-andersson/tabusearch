package tabusearch;

import java.util.Arrays;
import java.util.LinkedList;

// Class for performing a local search as part of Tabu Search.

public class LocalSearch {
	
	// Inner class for the short-term memory (STM)
	public static class STM {
		public static int stmSize; // STM size
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
	
	// Instance variables for a local search object
	public double stepSize; // Step size 
	public LinkedList<Point> localSearchHist = new LinkedList<Point>(); // List to store entire history of points in the local search
	STM stmObj = new STM(); 
	private Point currentPoint; // Point object corresponding to the current position of the local search
	private double localMinVal; // The current minimum function value for the local search
	public static int stmSize; // Size of the short-term memory

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
				if (Math.abs(testEl[i]) > Tabu.constraint) {
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
				validList.add(new Point(testEl,Tabu.myFunc));
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
	private void attemptPatternMove(double[] xBase, Point currentPoint, STM stmObj) throws CloneNotSupportedException {
		double[] xCurrent = currentPoint.x;
		double[] xPattern = new double[xCurrent.length];
		
		// Perform a pattern move by repeating the vector from the last base point
		for (int i = 0; i < xPattern.length; i++) {
			xPattern[i] = xCurrent[i] + (xCurrent[i] - xBase[i]);
		}
		
		// If the objective function is reduced, store the intermediate Tabu move retain the pattern move
		double fPattern = Tabu.myFunc.f(xPattern);
		if (fPattern < currentPoint.fval) {
			storePoint(currentPoint, stmObj);
			currentPoint = new Point();
			currentPoint.x = xPattern.clone();
			currentPoint.fval = fPattern;
		}
	}
	
	// Attempt to store a clone of the current Point object in the various memory objects
	private void storePoint(Point currentPoint, STM stmObj) throws CloneNotSupportedException {
		localSearchHist.add(currentPoint.clone()); // Add the current point to the local search history
		// See if the point should be stored in the STM, the MTM or the global minimum
		stmObj.tryAddToSTM(currentPoint);
		Tabu.mtmObj.tryAddToMTM(currentPoint);
		Tabu.checkGlobalMin(currentPoint);
	}
	
	// Perform a local search starting at startingPoint and ending when the counter threshold
	// has been reached
	public LinkedList<Point> doLocalSearch(Point startingPoint) throws CloneNotSupportedException {
				
		// Initialisation
		currentPoint = startingPoint.clone(); // Generate the initial point
		storePoint(currentPoint,stmObj);
		localMinVal = currentPoint.fval; // Initialise the minimum value found in the local search
		
		LinkedList<double[]> testList = new LinkedList<double[]>(); // List of coordinates to check for validity
		LinkedList<Point> validList = new LinkedList<Point>(); // List of points corresponding to valid Tabu moves 
		int num_its = 0; // Total number of local search iterations
		int counterThresh; // Counter limit for this local search
		
		if (Tabu.searchType.matches("initialise|ssr")) {
			counterThresh = Tabu.intensifyThresh;
		}
		else if (Tabu.searchType.equals("intensify")) {
			counterThresh = Tabu.diversifyThresh;
		}
		else {
			counterThresh = Tabu.ssrThresh;
		}
		
		// Begin local search
		while (Tabu.counter < counterThresh) {
			if (Tabu.verbose == true) {
				System.out.print(Tabu.counter + " ");
			}
			
			double[] xBase = currentPoint.x.clone(); // Update the base point
			updateTestList(testList,currentPoint,stepSize);	
			updateValidList(testList,validList,stmObj);	
			boolean functionReduced = makeBestAllowedMove(validList); // Make the best allowed move
			
			// If the objective function was reduced, attempt a pattern move
			if (functionReduced == true) {
				attemptPatternMove(xBase, currentPoint, stmObj);
				if (currentPoint.fval < localMinVal) {
					localMinVal = currentPoint.fval;
				}
			}
			// Reset the counter if a new global minimum value has been found
			if (currentPoint.fval < Tabu.globalMinPoint.fval) {
				Tabu.counter = 0; 
			}
			else {
				Tabu.counter += 1; // Increment the counter
			}
			
			storePoint(currentPoint,stmObj);
			
			// Attempt to store current point in LTM after an integer number of iterations,
			// where the integer is the number of Tabu steps that fit within each grid segment.
			if (num_its % Math.ceil(LTM.getSegSize()/Tabu.stepSize) == 0) {
				Tabu.ltmObj.storeInLTM(currentPoint.x);
			}
			
			num_its++;

			if (num_its > 10000) {
				System.out.println("Error: Excessive number of local search iterations reached without triggering counter threshold.");
				System.exit(0);
			}
			
		}
		
		if (Tabu.searchType.matches("initialise|ssr")) {
			Tabu.searchType = "intensify";
		}
		else if (Tabu.searchType.equals("intensify")) {
			Tabu.searchType = "diversify";
		}
		else {
			Tabu.searchType = "ssr";
		}
		
		return localSearchHist;
	}
}
