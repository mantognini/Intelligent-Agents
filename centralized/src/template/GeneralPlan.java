package template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;
import template.VehiculeAction.Action;

public class GeneralPlan {

	private final Map<Vehicle, List<VehiculeAction>> plans; // One plan per vehicle

	// Keep track of those for validation purposes
	private final List<Vehicle> vehicles;
	private final TaskSet tasks;

	private final Random randomGenerator = new Random();

	/**
	 * Private constructor; use generateInitial static factory to build the first plan, then use generateNeighbors to
	 * navigate onto the plan space.
	 */
	private GeneralPlan(Map<Vehicle, List<VehiculeAction>> plans, List<Vehicle> vehicles, TaskSet tasks) {
		this.plans = plans;
		this.vehicles = vehicles;
		this.tasks = tasks;

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

		return new GeneralPlan(plans, vehicles, tasks);
	}

	public List<GeneralPlan> generateNeighbors() {
		List<GeneralPlan> neighbours = new ArrayList<GeneralPlan>();
		Vehicle modelVehicule;
		// Randomly choose a vehicle that has at least one task
		do {
			int index = randomGenerator.nextInt(vehicles.size());
			modelVehicule = vehicles.get(index);
		} while (plans.get(modelVehicule).size() == 0);

		// Distribute the same task to the other vehicle for each new plan
		List<VehiculeAction> newTaskList = new LinkedList<VehiculeAction>(plans.get(modelVehicule));
		VehiculeAction distributedAction = newTaskList.remove(0);

		// TODO : Verify that the added plan doesn't violate the constraints
		for (Vehicle vehicle : vehicles) {
			Map<Vehicle, List<VehiculeAction>> newPlan = new HashMap<Vehicle, List<VehiculeAction>>(plans);
			if (!vehicle.equals(modelVehicule)) {
				List<VehiculeAction> updated = newPlan.get(vehicle);
				updated.add(0, distributedAction);
				newPlan.put(vehicle, updated);
				newPlan.put(modelVehicule, newTaskList);
				// TODO : Convert to LogistPlan
				neighbours.add(new GeneralPlan(newPlan, vehicles, tasks));
			}
		}

		List<VehiculeAction> originalActions = plans.get(modelVehicule);
		Map<Vehicle, List<VehiculeAction>> newPlan = new HashMap<Vehicle, List<VehiculeAction>>(plans);

		if (originalActions.size() > 1) {

			for (int i = 0; i < originalActions.size(); i++) {
				for (int j = i + 1; j < originalActions.size(); j++) {
					List<VehiculeAction> newActions = new LinkedList<VehiculeAction>(originalActions);
					Collections.swap(newTaskList, i, j);
					// TODO Check if doesn't violate constraints
					newPlan.put(modelVehicule, newActions);
					neighbours.add(new GeneralPlan(newPlan, vehicles, tasks));
				}
			}
		}
		return neighbours;
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
		// TODO implement validation for plans
	}

}
