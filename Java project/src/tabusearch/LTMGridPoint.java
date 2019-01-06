package tabusearch;

import java.util.Arrays;

// Class for long-term memory (LTM) grid points.
// This is necessary because int[] is a reference type and so Object.equals must be overridden to compare the arrays with 'equals()'.
// This allows the 'add' method of HashSet to behave as expected when adding grid points to the LTM.
public class LTMGridPoint {
	int[] gridPos = new int[Tabu.dim];

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(gridPos);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LTMGridPoint other = (LTMGridPoint) obj;
		if (!Arrays.equals(gridPos, other.gridPos))
			return false;
		return true;
	}
	
}
