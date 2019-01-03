package tabusearch;

// Interface for functions to be minimised by Tabu Search.
// All classes which implement this interface must have all of the methods stated in the interface.

public interface Function {

	public void setDim(int dimension);
	public double f(double[] x);
	
}
