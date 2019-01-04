package tabusearch;

import java.util.LinkedList;

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
					setMaxMTMPoint(); // Update the maximum MTM point
				} // "If the MTM list is not yet full"
				else {
					mtmList.remove(maxMTMLoc);
					mtmList.addLast(currentPoint.clone());
				} // Replace the point corresponding to the maximum value in the MTM with the 
			}
		}
	}
	
	static public LinkedList<Point> globSearchHist; // Object to store the entire Tabu search history as a linked list.
	static public Point currentMinPoint; // Current Point with the minimum associate objective function value
	static public MTM mtmObj; // MTM object
	
	// Check whether this point corresponds the best solution found so far and store it if so 
	public static void checkGlobalMin(Point currentPoint) throws CloneNotSupportedException {
		if (currentMinPoint == null) {
			currentMinPoint = currentPoint.clone();
		}
		else if (currentPoint.fval < currentMinPoint.fval) {
			currentMinPoint = currentPoint.clone();
		}
	}

}
