package tabusearch;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
		final int dim = 5; // Input dimension
		myFunc.setDim(dim); // Set static dimension variable of the the SchwefelFunction class
		
		// Setting up the classes
		Tabu.myFunc = myFunc;
		Tabu.dim = dim;
		// Algorithm parameters to be defined
		Tabu.verbose = false; // True: print progress events of the search.
		Tabu.seed = 40; // Set the rng seed
		Tabu.intensifyThresh = 10; // Counter limit to intensify search using MTM
		Tabu.diversifyThresh = 15; // Counter limit to diversify search using long-term memory (LTM)
		Tabu.ssrThresh = 25; // Counter limit to perform step-size reduction
		Tabu.stepSize = 25; // Starting step size for the Tabu local search
		Tabu.stepLimit = 1E-13; // Lower limit on the step size (>1E-13 to prevent double precision issues)
		Tabu.stepReduceFactor = 0.75; // Constant factor to reduce stepSize by after step-size reduction
		Tabu.eval_limit = 10000; // Convergence criterion - stop when num_evals reaches eval_limit
		LocalSearch.STM.stmSize = 8; // Short-term memory (STM) size
		MTM.mtmSize = 4; // Medium-term memory (MTM) size
		Tabu.constraint = 500.0; // Upper limit on variable magnitude
		LTM.setSegSize(100.0); // Long-term memory (LTM) segment size for grid
		
		
		// TEMP EXPERIMENTS FOR PYTHON USING JSON FILES
		
		// Perform a loop of Tabu searches
		int nTabuRuns = 1; // Number of iterations to average over with different random seeds
		// Nested array of zero-held function evolutions
		ArrayList<LinkedList<Double>> allZeroHeldHistories = new ArrayList<LinkedList<Double>>(nTabuRuns);
		ArrayList<LinkedList<Double>> allZeroHeldMinVals = new ArrayList<LinkedList<Double>>(nTabuRuns);
		Tabu.seed = 1; // Initial seed
		System.out.println("Starting a loop of Tabu runs");
		for (int k = 0; k < nTabuRuns; k++) {
			Tabu.doTabuSearch(); 
			allZeroHeldHistories.add(Tabu.globfEvolZeroHold);
			allZeroHeldMinVals.add(Tabu.globMinValZeroHold);
			Tabu.seed++;
		}

		// Save the search data to a .json file for analysis with Python
		System.out.println("\nSearch completed. Saving the data to a .json file.");
		
		String jsonFilename = "mydata.json";
		
		JSONObject jsonObj = new JSONObject();
		List<String> tabuPath = Tabu.globSearchHist.stream().map(Point::getStringx).collect(Collectors.toList());
		List<String> fEvolution = Tabu.globSearchHist.stream().map(Point::getStringFval).collect(Collectors.toList());
		//jsonObj.put("tabu_path", tabuPath);
		//jsonObj.put("f_evolution", fEvolution);
		//jsonObj.put("num_eval_evolution", Tabu.numEvalEvolution);
		jsonObj.put("all_zero_held_evols", allZeroHeldHistories); 
		jsonObj.put("all_zero_held_minvals", allZeroHeldMinVals); 
		//String jsonFilename = "Seed" + String.valueOf(Tabu.seed) + "Data.json";
		
		String workingdir = System.getProperty("user.dir");
		String parentdir = workingdir.substring(0,workingdir.lastIndexOf('\\'));
		String jsondir = parentdir + "\\json\\";
		
		try (FileWriter file = new FileWriter(jsondir + jsonFilename)) {
			file.write(jsonObj.toJSONString());
		}

		System.out.println("Json file saved.");
	}

}
