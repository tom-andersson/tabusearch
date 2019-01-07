package tabusearch;

import java.util.LinkedList;

//Class for the medium-term memory (MTM) storing the 'mtmSize' Points with the least function
//values visited so far 
public class MTM {
	
	public static int mtmSize; // Size of the medium-term memory
	public double maxMTMVal; // Greatest function value in the MTM
	public int maxMTMLoc; // Index into the MTM list of the greatest function value point
	private LinkedList<Point> mtmList = new LinkedList<Point>(); // LinkedList for the MTM

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
			if (mtmList.size() < mtmSize) {
				mtmList.addLast(currentPoint.clone());
				setMaxMTMPoint();
			} // "If the MTM list is not yet full"
			else {
				mtmList.remove(maxMTMLoc);
				mtmList.addLast(currentPoint.clone());
				setMaxMTMPoint();
			} // Replace the point corresponding to the maximum value in the MTM with the current point
		}
	}

	// Sum and average the coordinates of the locations in the MTM and return the
	// corresponding Point object
	public static Point findMTMAvg(MTM mtmObj) {
		double[] avgLoc = new double[Tabu.dim];
		for (int i = 0; i < Tabu.dim; i++) {
			double avgSum = 0;
			for (Point mtmEl : mtmObj.mtmList) {
				avgSum += mtmEl.x[i];
			}
			avgLoc[i] = avgSum / mtmSize;
		}

		return new Point(avgLoc, Tabu.myFunc);
	}
}
