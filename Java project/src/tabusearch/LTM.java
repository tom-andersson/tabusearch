package tabusearch;

import java.util.HashSet;
import java.util.Set;

// Class for the long-term memory (LTM) grid for search diversification. This divides the feasible region into a grid and 
// stores the grid segments visited during the Tabu search in a HashSet. In diversification, when a new random starting
// point is generated it must not lie in one of the grid segments already visited.
public class LTM {

	public Set<LTMGridPoint> ltmSet = new HashSet<LTMGridPoint>(); // Set of grid coordinates already visited
	private static double segSize; // Segment size for the LTM grid
	public boolean ltmFull = false; // True if the LTM is full

	public static void setSegSize(double segSizeVal) {
		segSize = segSizeVal;

		if (Tabu.constraint % segSize != 0.0) {
			System.out.println(
					"Warning: It is recommended that the constraint be divisible by the LTM grid segment size.");
		}
	}

	// Convert current point into 'grid' coordinates and try to add it to the LTM set
	public boolean storeInLTM(double[] currentPos) {
		// Convert current point to grid coordinates
		double factor = Tabu.constraint / segSize; // Half of the number of grid segments for each variable
		LTMGridPoint gridPoint = new LTMGridPoint();

		for (int i = 0; i < Tabu.dim; i++) {
			gridPoint.gridPos[i] = (int) Math.ceil(currentPos[i] / Tabu.constraint * factor);
		}

		// Attempt to add new grid coordinates to the set. Will not add if the set
		// already contains this grid position.
		boolean addingToLTM = ltmSet.add(gridPoint);

		if (addingToLTM == true && ltmFull == false) {
			if (ltmSet.size() == (int) Math.pow(2 * factor, Tabu.dim) && Tabu.constraint % segSize == 0) {
				ltmFull = true;
				System.out.println("(LTM set now full) ");
			} // "If a grid point was added to the LTM and it is now full"
		}

		return addingToLTM;
	}

	// Generate a random input point
	public Point genDiversifiedPoint() {
		Point p = Tabu.genRandomPoint();
		int num_attempts = 0;

		if (ltmFull == false) {
			while (storeInLTM(p.x) == false) {
				p = Tabu.genRandomPoint();
				num_attempts++;
			} // "While the generated point is already in LTM, generate a new point and try again."
		}
		
		if (Tabu.verbose == true) {
			System.out.print("\nDiversifying after " + num_attempts + " rejected samples: ");
		}
		return p;
	}

	// How to diversify: treat each element individually. make grid with segments of
	// fixed size segSize
	// Find 'segment coordinates' with round(coords/constraint *
	// constraint/segSize). 'ban' each new segment
	// by adding it to a set (?) of type int[]
	// only do this check every 10 iterations for efficiency (unlikely to move into
	// new region in 1 step)
	// put this into generatePoint. have logic for if entire space has been banned.
	// if generated point is in
	// set, reject it and generate another.
}
