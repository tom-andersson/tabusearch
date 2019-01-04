package tabusearch;

import java.util.LinkedList;

// Object to store the entire Tabu search history as a linked list.
// I am making a class for this list because I may want to add methods specific to this list later
// e.g. converting to an array for faster access to the elements, or creating a new list with only
// the input search locations.

public class GlobalSearchHistory {
	
	public LinkedList<Point> globSearchHist;
	
	// Create a new linked list object in the constructor call
	public GlobalSearchHistory() {
		globSearchHist = new LinkedList<Point>(); 
	}
	
}
