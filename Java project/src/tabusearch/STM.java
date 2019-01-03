package tabusearch;

// Object for storing the short-term memory (STM) for the Tabu Search.
// The STM is a list of the last N locations successfully visited.

public class STM {

	public int N; // STM size
	public Point[] STMArray; // Array variable
	
	// Constructor to initialise the STM array 
	public STM(int N) {
		this.N = N;
		STMArray = new Point[N]; // Point the array variable at an empty array object
	}
	
}
