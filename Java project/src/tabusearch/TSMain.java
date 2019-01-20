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
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws CloneNotSupportedException, IOException {

		// Setting up the objective function to minimise
		Function myFunc = new Schwef(); // Using polymorphism to allow for the objective function to be easily changed here
		final int dim = 5; // Input dimension
		myFunc.setDim(dim); // Set static dimension variable of the the SchwefelFunction class
		
		// Setting up the classes
		Tabu.myFunc = myFunc;
		Tabu.dim = dim;
		// Default algorithm parameters to be defined (may be varied later in experiments)
		Tabu.verbose = false; // True: print progress events of the search.
		Tabu.seed = 50; // Set the rng seed
		Tabu.intensifyThresh = 1; // Counter limit to intensify search using MTM
		Tabu.diversifyThresh = 6; // Counter limit to diversify search using long-term memory (LTM)
		Tabu.ssrThresh = 16; // Counter limit to perform step-size reduction
		Tabu.startingStepSize = 175; // Starting step size for the Tabu local search
		Tabu.stepLimit = 1E-13; // Lower limit on the step size (>1E-13 to prevent double precision issues)
		Tabu.stepReduceFactor = 0.985; // Constant factor to reduce stepSize by after step-size reduction
		Tabu.evalLimit = 20000; // Convergence criterion - stop when numEvals reaches evalLimit
		Tabu.stmSize = 8; // Short-term memory (STM) size
		MTM.mtmSize = 4; // Medium-term memory (MTM) size
		Tabu.constraint = 500.0; // Upper limit on variable magnitude
		LTM.setSegSize(100.0); // Long-term memory (LTM) segment size for grid
	
		
		// EXPERIMENTS FOR PYTHON USING JSON FILES
		
		// Choose whether to save the data
		boolean saveData = true;
		// Choose the filename of saveData = true
		String jsonFilename = "TSjson.json"; 
		
		// Choose which experiment to do
		boolean doExperiment1 = false;
		boolean doExperiment2 = true;
		boolean doExperimetn3 = false;
		
		JSONObject jsonObj = new JSONObject();
		System.out.println("Starting experiment.");
		
		// Store the search path followed and the evolution of the function value for a single run
		if (doExperiment1 == true) {
			Tabu tabuObj = new Tabu();
			tabuObj.doTabuSearch();
			List<String> tabuPath = tabuObj.globSearchHist.stream().map(Point::getStringx).collect(Collectors.toList());
			List<String> fEvolution = tabuObj.globSearchHist.stream().map(Point::getStringFval).collect(Collectors.toList());
			jsonObj.put("tabu_path", tabuPath);
			jsonObj.put("f_evolution", fEvolution);
			jsonObj.put("num_eval_evolution", tabuObj.numEvalEvolution);
		}
		
		// Store the (zero held) evolution of the function value and the minimum value found versus number of function evaluations
		// for a number of different seeds (to be averaged in Python)
		if (doExperiment2 == true) {
			int nTabuRuns = 25; // Number of iterations to average over with different random seeds
			// Nested array of zero-held function evolutions
			ArrayList<LinkedList<Double>> allZeroHeldHistories = new ArrayList<LinkedList<Double>>(nTabuRuns);
			ArrayList<LinkedList<Double>> allZeroHeldMinVals = new ArrayList<LinkedList<Double>>(nTabuRuns);
			Tabu.seed = 1; // Initial seed
			for (int k = 0; k < nTabuRuns; k++) {
				Tabu tabuObj = new Tabu();
				tabuObj.doTabuSearch(); 
				allZeroHeldHistories.add(tabuObj.globfEvolZeroHold);
				allZeroHeldMinVals.add(tabuObj.globMinValZeroHold);
				Tabu.seed+=1;
			}
			jsonObj.put("all_zero_held_evols", allZeroHeldHistories); 
			jsonObj.put("all_zero_held_minvals", allZeroHeldMinVals); 
		}
		
		// Vary a Tabu search parameter and store lists of the best objectives found over many random seeds
		// (to be averaged in Python)
		if (doExperimetn3 == true) {
			int nTabuRuns = 50; // Number of iterations to average over with different random seeds
			ArrayList<ArrayList<Double>> bestSolutions = new ArrayList<ArrayList<Double>>(nTabuRuns);
			ArrayList<Double> parameterValues = new ArrayList<Double>(nTabuRuns);
			ArrayList<Double> currentBestMinVals; // Min vals for the current parameter value
			for (int l = 1; l <50; l++) { // Range of parameter values
				currentBestMinVals = new ArrayList<Double>(nTabuRuns);
				Tabu.seed = 1; // Initial seed
				Tabu.intensifyThresh = l;
				Tabu.diversifyThresh = Tabu.intensifyThresh + 5;
				Tabu.ssrThresh = Tabu.intensifyThresh + 15;

				for (int k = 0; k < nTabuRuns; k++) {
					Tabu tabuObj = new Tabu();
					tabuObj.doTabuSearch(); 
					System.out.println(tabuObj.bestSolution.fval);
					currentBestMinVals.add(tabuObj.bestSolution.fval);
					Tabu.seed+=1;
				}
				parameterValues.add((double) Tabu.intensifyThresh);
				bestSolutions.add(currentBestMinVals);
			}
			jsonObj.put("parameter", parameterValues);
			jsonObj.put("best_solus", bestSolutions);
		}
		
		System.out.println("\nExperiment completed.");
		
		if (saveData == true) {
			// Save the search data to a .json file for analysis with Python
			System.out.println("Saving the data to a .json file.");
			
			String workingdir = System.getProperty("user.dir");
			String parentdir = workingdir.substring(0,workingdir.lastIndexOf('\\'));
			String jsondir = parentdir + "\\json\\";
			
			try (FileWriter file = new FileWriter(jsondir + jsonFilename)) {
				file.write(jsonObj.toJSONString());
			}

			System.out.println("Json file saved.");
		}
	}
}
