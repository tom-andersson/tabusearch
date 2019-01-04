package tabusearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

// Class with static fields and methods for performing a local search as part of Tabu Search.

public class LocalSearch {
	
	// Inner class for the short-term memory (STM)
	private class STM {
		private int N; // STM size
		private ArrayList<Point> STMArray; // Array variable
		
		// Constructor to initialise the STM array 
		public STM(int N) {
			this.N = N;
			STMArray = new ArrayList<Point>(); // Point the array variable at an empty ArrayList object
		}
	}
	
	// Instance variables for this local search object
	public final double inc; // Increment size 
	public final long seed; // Rng seed 
	public LinkedList<Point> localSearchHist = new LinkedList<Point>(); // List to store entire history of points in the local search
	private Point currentPoint; // Point object corresponding to the current position of the local search
	private double localMinVal; // The current minimum function value for the local search
	// Static variables shared by all instances of this class (i.e. all separate local searches)
	public static int dim; // Input dimension
	public static int STMSize; // Size of the short-term memory
	public static Function myFunc; // Function to minimise
	public static double constraint; // Upper limit on variable magnitude
	
	// Constructor for creating a LocalSearch object
	public LocalSearch(double inc, long seed) {
		this.inc = inc;
		this.seed = seed;
	}
	
	// Generate a random input point
	private Point genRandomPoint() {
		System.out.println("generating a random point");
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
	private void updateTestList(LinkedList<double[]> testList, Point currentPoint, double inc) {
		System.out.println("updating test list");
		testList.clear(); // Remove all elements from the list
		
		// Populate the list
		for (int i = 0; i < currentPoint.x.length; i++) {
			double[] xTemp1 = currentPoint.x.clone();
			xTemp1[i] += inc;
			testList.add(xTemp1);

			double[] xTemp2 = currentPoint.x.clone();
			xTemp2[i] -= inc;
			testList.add(xTemp2);
		}
	}
	
	// Generate list to store the permitted non-tabu moves that are within the feasible region
	// The validList object is reused by reference to avoid frequent expensive object creation 
	private void updateValidList(LinkedList<double[]> testList,LinkedList<Point> validList, STM STMObj) {
		System.out.println("updating valid list");
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
				for (Point stmEl : STMObj.STMArray) {
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
		System.out.println("doing tabu increment");
		boolean doPattern = false;
		
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
				Point el = validList.get(i);
				if (el.fval < min) {
					min = el.fval;
					minloc = i; 
				}
			currentPoint = validList.get(minloc).clone(); // Move the current point to the best valid position
			}
		}
		
		if (currentPoint.fval < localMinVal) {
			doPattern = true; 
			localMinVal = currentPoint.fval;
		}
		
		return doPattern;
	}
	

	// Attempt a pattern move and execute it if it improves upon the minimum of the local search
	private void attemptPatternMove(double[] xBase, Point currentPoint) {
		System.out.println("doing pattern move");
		double[] xCurrent = currentPoint.x;
		double[] xPattern = new double[xCurrent.length];
		
		for (int i = 0; i < xPattern.length; i++) {
			xPattern[i] = xCurrent[i] + (xCurrent[i] - xBase[i]);
		}
		
		double fPattern = myFunc.f(xPattern);
		if (fPattern < currentPoint.fval) {
			currentPoint = new Point();
			currentPoint.x = xPattern.clone();
			currentPoint.fval = fPattern;
			localMinVal = fPattern;
		}
	}
	
	// TODO: Explain what this method does
	public LinkedList<Point> doSearch() throws CloneNotSupportedException {
		
		int counter = 0; // Number of iterations for which there has been no improvement on the minimum

		STM STMObj = new STM(STMSize); 
		ArrayList<Point> STMArray = STMObj.STMArray;
		 
		currentPoint = genRandomPoint(); // Generate the initial point
		STMArray.add(currentPoint.clone()); // Add the initial point to the STM
		localSearchHist.add(currentPoint.clone()); // Add the initial point to the local search history
		double[] xBase = currentPoint.x.clone(); // Initial base point
		localMinVal = currentPoint.fval; // Initialise 
		
		// List of coordinates to check for being valid Tabu moves
		LinkedList<double[]> testList = new LinkedList<double[]>();
		// List of points corresponding to valid Tabu moves 
		LinkedList<Point> validList = new LinkedList<Point>();
		
		// Update a list of test points by incrementing and decrementing each variable
		updateTestList(testList,currentPoint,inc);
		// Update a list of points corresponding to valid non-tabu moves
		updateValidList(testList,validList,STMObj);
		// Make the best allowed move
		boolean doPattern = makeBestAllowedMove(validList);
		// Attempt a pattern move if the objective function was reduced
		if (doPattern == true) {
			attemptPatternMove(xBase, currentPoint);
		}

		System.out.println("Valid list is: ");
		for (Point el : validList) {
			System.out.println(Arrays.toString(el.x));
		}

		// TODO: Make this local search increment into a loop 
		
		return localSearchHist;
	}
}
