package template;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;
import template.VehiculeAction.Action;

public class GeneralPlan {

	public final Map<Vehicle, List<VehiculeAction>> plans; // One plan per vehicle

	/**
	 * Private constructor; use generateInitial static factory to build the first plan, then use generateNeighbors to
	 * navigate onto the plan space.
	 */
	private GeneralPlan(Map<Vehicle, List<VehiculeAction>> plans) {
		this.plans = plans;

		validateOrDie();
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

		for (Task task : tasks) {
			// move & pickup
			planForBiggest.add(new VehiculeAction(Action.PICK, task));

			// move & deliver
			planForBiggest.add(new VehiculeAction(Action.DELIVER, task));
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

	public Map<Vehicle, Plan> convertToLogistPlans() {
		Map<Vehicle, Plan> logistPlans = new HashMap<Vehicle, Plan>(plans.size());

		for (Entry<Vehicle, List<VehiculeAction>> p : plans.entrySet()) {
			logistPlans.put(p.getKey(), convertToLogistPlan(p.getKey(), p.getValue()));
		}

		return logistPlans;
	}

	private Plan convertToLogistPlan(Vehicle vehicle, List<VehiculeAction> actions) {
		City currentCity = vehicle.getCurrentCity();
		Plan logistPlan = new Plan(currentCity);

		for (VehiculeAction action : actions) {
			if (action.action == Action.PICK) {
				// move to pickup location & pick it up
				for (City city : currentCity.pathTo(action.task.pickupCity)) {
					logistPlan.appendMove(city);
				}

				logistPlan.appendPickup(action.task);
				currentCity = action.task.pickupCity;
			} else {
				// move to delivery location & deliver
				for (City city : currentCity.pathTo(action.task.deliveryCity)) {
					logistPlan.appendMove(city);
				}

				logistPlan.appendDelivery(action.task);
				currentCity = action.task.deliveryCity;
			}
		}

		return logistPlan;
	}

	/**
	 * Make sure no constraints are violated
	 */
	private void validateOrDie() {
		// TODO
	}

}
