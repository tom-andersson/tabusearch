package tabusearch;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

// Class with static fields for holding important search information 
// and static methods for performing typical processes of Tabu search.
public class Tabu {
	
	// Nested class for the medium-term memory (MTM)
	public static class MTM {
		public double maxMTMVal; // Greatest function value in the MTM
		public int maxMTMLoc; // Index into the MTM list of the greatest function value point
		private final int M; // MTM size
		private LinkedList<Point> mtmList; // LinkedList variable
		
		// Constructor to initialise the MTM array 
		public MTM(int M) {
			this.M = M;
			mtmList = new LinkedList<Point>(); // Point the array variable at an empty ArrayList object
		}
		
		public int getM() {
			return this.M;
		}
		
		// Update the greatest value in the MTM and its location
		public void setMaxMTMPoint() {
			if (mtmList.size() > 0) {
				maxMTMVal = mtmList.getFirst().fval;
				maxMTMLoc = 0;
				for (int i = 1; i < mtmList.size(); i++) {
					Point p = mtmList.get(i);
					if (p.fval > maxMTMVal) {
						maxMTMVal = p.fval;
						maxMTMLoc = i;
					}
				}
			}
		}
		
		public void tryAddToMTM(Point currentPoint) throws CloneNotSupportedException {
			// Only call this if the current value is lower than the greatest in the MTM
			// Replace the corresponding Point in MTM with the currentPoint
			if (currentPoint.fval < maxMTMVal) {
				if (mtmList.size() < M) {
					mtmList.addLast(currentPoint.clone());
					setMaxMTMPoint();
				} // "If the MTM list is not yet full"
				else {
					mtmList.remove(maxMTMLoc);
					mtmList.addLast(currentPoint.clone());
					setMaxMTMPoint();
				} // Replace the point corresponding to the maximum value in the MTM with the 
			}
		}
		
		// Sum and average the coordinates of the locations in the MTM and return the corresponding Point object
		public static Point findMTMAvg(MTM mtmObj) {
			double[] avgLoc = new double[dim];
			for (int i = 0; i < dim; i++) {
				double avgSum = 0;
				for (Point mtmEl : mtmObj.mtmList) {
					avgSum += mtmEl.x[i];
				}
				avgLoc[i] = avgSum/mtmSize;
			}	
			
			return new Point(avgLoc,Tabu.myFunc);
		}
	}
	
	public static LinkedList<Point> globSearchHist; // Object to store the entire Tabu search history as a linked list.
	public static Point globalMinPoint; // Current Point with the minimum associate objective function value
	public static MTM mtmObj; // MTM object
	public static Point startingPoint; // Force the local search to begin from this point
	public static int dim; // Input dimension
	public static double constraint; // Upper limit on variable magnitude
	public static int stmSize; // Size of the short-term memory
	public static int mtmSize; // Size of the medium-term memory
	public static Function myFunc; // Function to minimise
	public static double stepSize; // Starting step size for the Tabu local search
	public static double stepLimit; 
	public static long seed; // Rng seed
	
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
	private static Point genRandomPoint() {
		Random gen = new Random(); // Unif[0,1) rng
		double[] x = new double[dim];
		for (int i = 0; i < x.length; i++) {
			x[i] = gen.nextDouble() * (2*constraint) - constraint;
		}
		
		// TODO: LTM logic
		
		return new Point(x, Tabu.myFunc);
	}
	
	// Perform a complete Tabu search
	public static void doTabuSearch() throws CloneNotSupportedException {
		mtmObj = new Tabu.MTM(mtmSize);
		counter = 0; 
		searchType = "initialise";
		System.out.println("Starting search");
		startingPoint = genRandomPoint(); 
		
		while (stepSize >= stepLimit) {
			// Intensify search
			if (searchType.equals("intensify")) {
				startingPoint = MTM.findMTMAvg(mtmObj);
				System.out.println("Intensiying search");
			} // Intensify: set starting point to the meant point of the MTM
			
			// Diversify search
			else if (Tabu.searchType.equals("diversify")) {
				startingPoint = genRandomPoint(); 
				System.out.println("Diversifying search");
			} // Diversify: generate a random point
			
			// Reduce step size
			else if (searchType.equals("ssr")) {
				startingPoint = globalMinPoint; // Restart from the minimum point found so far
				stepSize = stepSize*0.75; // Reduce the increment size
				counter = 0; // Reset counter
				System.out.println("Reducing step size");
			}
			
			LocalSearch LSObj = new LocalSearch(stepSize,seed);
			LinkedList<Point> localSearchHist = LSObj.doLocalSearch(); 
			globSearchHist.addAll(localSearchHist); // Append the local search history of points to the global search history
			
			// TODO: change random generation
			seed += 10; // Start from a new location
		}
	}

}
