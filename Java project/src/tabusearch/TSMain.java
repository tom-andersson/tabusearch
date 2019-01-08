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
		
		// Setting up the classes
		Tabu.myFunc = myFunc;
		Tabu.dim = dim;
		// Algorithm parameters to be defined
		Tabu.verbose = true; // True: print progress events of the search.
		Tabu.seed = 40; // Set the rng seed
		Tabu.globSearchHist = new LinkedList<Point>(); // Initialise global search history list 
		Tabu.intensifyThresh = 10; // Counter limit to intensify search using MTM
		Tabu.diversifyThresh = 15; // Counter limit to diversify search using long-term memory (LTM)
		Tabu.ssrThresh = 25; // Counter limit to perform step-size reduction
		Tabu.stepSize = 25; // Starting step size for the Tabu local search
		Tabu.stepReduceFactor = 0.75; // Constant factor to reduce stepSize by after step-size reduction
		Tabu.eval_limit = 5000; // Convergence criterion - stop when num_evals reaches eval_limit
		LocalSearch.STM.stmSize = 8; // Short-term memory (STM) size
		MTM.mtmSize = 4; // Medium-term memory (MTM) size
		Tabu.constraint = 500.0; // Upper limit on variable magnitude
		LTM.setSegSize(250.0); // Long-term memory (LTM) segment size for grid
		
		// Perform a Tabu search
		Tabu.doTabuSearch(); 

		// Save the search data to a .json file for analysis with Python
		System.out.println("\nSearch completed. Saving the path to a .json file.");
		
		JSONObject jsonObj = new JSONObject();
		List<String> tabuPath = Tabu.globSearchHist.stream().map(Point::getStringx).collect(Collectors.toList());
		List<String> fEvolution = Tabu.globSearchHist.stream().map(Point::getStringFval).collect(Collectors.toList());
		jsonObj.put("tabu_path", tabuPath);
		jsonObj.put("f_evolution", fEvolution);
		jsonObj.put("num_eval_evolution", Tabu.numEvalEvolution);
		String jsonFilename = "Seed" + String.valueOf(Tabu.seed) + "Data.json";
		System.out.println("Json file saved.");
		
		String workingdir = System.getProperty("user.dir");
		String parentdir = workingdir.substring(0,workingdir.lastIndexOf('\\'));
		String jsondir = parentdir + "\\json\\";
		
		try (FileWriter file = new FileWriter(jsondir + jsonFilename)) {
			file.write(jsonObj.toJSONString());
		}
		
		// TODO: deal with seeds
	}

}
