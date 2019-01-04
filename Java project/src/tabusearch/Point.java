package tabusearch;

// Object storing an input location and its associated function value.
// N.B. The Func.Schwef.dim field should be set before an object of this class can be created,
// otherwise an error will occur.

public class Point implements Cloneable {
	
	public double[] x; // Position
	public double fval; // Associated function value
	
	// Constructor to initialise the object
	public Point(double[] x, Function myFunc) {
		this.x = x; 
		fval = myFunc.f(x);
	}

	public Point() {
		// Empty constructor
	}

	@Override
	// This class only contains value type fields so shallow cloning produces independent copies
	protected Point clone() throws CloneNotSupportedException {
		Point p = new Point();
		p.x = this.x;
		p.fval = this.fval;
		return p;
	}

}