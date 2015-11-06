package template;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import template.VehiculeAction.Action;

public class GeneralPlan {

	public final Map<Vehicle, List<VehiculeAction>> plans; // One plan per vehicle

	/**
	 * Private constructor; use generateInitial static factory to build the first plan, then use generateNeighbors to
	 * navigate onto the plan space.
	 */
	private GeneralPlan(Map<Vehicle, List<VehiculeAction>> plans) {
		this.plans = plans;
	}

	/**
	 * Generate the first, naive plan: all tasks are assigned to be biggest vehicle in a sequential order.
	 */
	public static GeneralPlan generateInitial(List<Vehicle> vehicles, TaskSet tasks) {
		assert vehicles.size() > 0;
		Vehicle biggest = Utils.getBiggestVehicle(vehicles);
		int heaviest = Utils.getHeaviestWeight(tasks);

		if (biggest.capacity() < heaviest)
			throw new RuntimeException("Impossible to plan: vehicles are not big enough");

		List<VehiculeAction> planForBiggest = new LinkedList<VehiculeAction>();

		for (Task t : tasks) {
			// move & pickup
			planForBiggest.add(new VehiculeAction(Action.PICK, t.id));

			// move & deliver
			planForBiggest.add(new VehiculeAction(Action.DELIVER, t.id));
		}

		// Build vehicles' actions lists
		Map<Vehicle, List<VehiculeAction>> plans = new HashMap<Vehicle, List<VehiculeAction>>(vehicles.size());
		for (Vehicle v : vehicles) {
			if (v.equals(biggest))
				plans.put(v, planForBiggest);
			else
				plans.put(v, new LinkedList<VehiculeAction>());
		}

		return new GeneralPlan(plans);
	}

	public List<GeneralPlan> generateNeighbors() {
		// TODO Generate neighbors for a current plan
		return null;
	}

}
