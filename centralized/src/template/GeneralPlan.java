package template;

import java.util.ArrayList;
import java.util.List;

import logist.simulation.Vehicle;

public class GeneralPlan {

	public List<List<VehiculeAction>> plans = new ArrayList<List<VehiculeAction>>();
	List<Vehicle> vehicules;

	public static GeneralPlan generateInitial() {
		// TODO : Generate inital plan to generare and compare with neighbors.
		return null;
	}

	public List<GeneralPlan> generateNeighbors() {
		// Generate neighbors for a current plan
		return null;
	}

}
