package tabusearch;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;

// Perform the Tabu Search algorithm on the Schwefel Function

public class TSMain {

	// Main method
	public static void main(String[] args) throws CloneNotSupportedException, IOException {

		// Using polymorphism to allow for the objective functions to be easily changed here
		Function myFunc = new Schwef();
		final int dim = 2; // Input dimension
		myFunc.setDim(dim); // Set static dimension variable of the the SchwefelFunction class
		
		//TODO: don't bother having these as variables in main
		
		// Setting up the Tabu class
		Tabu.myFunc = myFunc;
		Tabu.dim = dim;
		// Algorithm parameters to be defined
		Tabu.seed = 100; // Rng seed
		Tabu.globSearchHist = new LinkedList<Point>(); // Initialise global search history list 
		Tabu.intensifyThresh = 10; // Counter limit to intensify search using MTM
		Tabu.diversifyThresh = 15; // Counter limit to diversify search using long-term memory (LTM)
		Tabu.ssrThresh = 25; // Counter limit to perform step-size reduction
		Tabu.stepSize = 15; // Starting step size for the Tabu local search
		Tabu.stepLimit = Tabu.stepSize/16; // Convergence criterion - stop when stepSize is smaller than stepLimit
		Tabu.stmSize = 4; // Short-term memory (STM) size
		Tabu.mtmSize = 4; // Medium-term memory (MTM) size
		Tabu.constraint = 500.0; // Upper limit on variable magnitude
		
		// Perform a Tabu search
		Tabu.doTabuSearch();

		// Save the search data to a .json file for analysis with Python
		JSONObject jsonObj = new JSONObject();
		List<String> tabuPath = Tabu.globSearchHist.stream().map(Point::getStringx).collect(Collectors.toList());
		jsonObj.put("tabu_path", tabuPath);
		String jsonFilename = "tabupath.json";
		
		String workingdir = System.getProperty("user.dir");
		String parentdir = workingdir.substring(0,workingdir.lastIndexOf('\\'));
		String jsondir = parentdir + "\\json\\";
		
		System.out.println(jsondir + jsonFilename);
		
		try (FileWriter file = new FileWriter(jsondir + jsonFilename)) {
			file.write(jsonObj.toJSONString());
		}
								
		// PLAN:
		
		// Need to make counter, MTM and currentMin static variables of a new public class holding public search information
		
		// After that, search logic should be:
		// Do local loop. When counter reaches MTM limit exit with an exit code indicating whether MTM is triggered or if
		// too many iterations were reached.
		// If too many iterations were reached, print error message and quit.
		// If MTM is triggered, start local search from avg of MTM 
		// Need to update LocalSearch to have optional string specifying the type of local search (initial, intensify or diversify)
		// and be able to take a specified starting point for MTM rather than randomly generating one
		// if MTM boolean is true, the counter limit is now set to diversify limit
		// When diversify limit is reached set limit to REDUCE and sample from uncharted space
		// When REDUCE is reached reduce step size and restart from best solution found so far
		
		// How to diversify: treat each element individually. make grid with segments of fixed size segSize
		// Find 'segment coordinates' with round(coords/constraint * constraint/segSize). 'ban' each new segment
		// by adding it to a set (?) of type int[]
		// only do this check every 10 iterations for efficiency (unlikely to move into new region in 1 step)
		// put this into generatePoint. have logic for if entire space has been banned. if generated point is in 
		// set, reject it and generate another.

		// Should I exit doSearch (the local search) when counter is reached and do search type logic outside of it? 
		// probably yes because this makes a better hierarchy. on termination update the Tabu.searchType string. then
		// make this the input of the next localSearch
		// set string to 'initial' 
		
		// TODO: deal with seeds
	}

}
