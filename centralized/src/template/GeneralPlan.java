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
import template.VehiculeAction.Event;

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

		// TODO This should be probably be disabled after we are sure everything is working well in order to ensure
		// decent performance.
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
			planForBiggest.add(new VehiculeAction(Event.PICK, task));

			// move & deliver
			planForBiggest.add(new VehiculeAction(Event.DELIVER, task));
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

	public double computeOverallCost() {
		// Converting to logist plan format will probably significantly slow down things.
		// Instead we could compute the cost ourselves.
		// TODO determine if computeOverallCost is a bottleneck and if so optimize it.

		double cost = 0;
		for (Vehicle vehicle : vehicles) {
			List<VehiculeAction> plan = plans.get(vehicle);
			Plan logistPlan = convertToLogistPlan(vehicle, plan);
			cost += logistPlan.totalDistance() * vehicle.costPerKm();
		}

		return cost;
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
			if (action.event == Event.PICK) {
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
		// The set of rules that should hold:
		final String rule1 = "All vehicles should have a (maybe empty) plan";
		final String rule2 = "All tasks should be picked up by exactly once, and therefore by one vehicle";
		final String rule3 = "All tasks should be delivered exactly once, and therefore by one vehicle";
		final String rule4 = "All tasks should be delivered by the same vehicle that picked it up";
		final String rule5 = "All tasks should be delivered after being picked up";

		// Ensure the first rule holds
		for (Vehicle vehicle : vehicles) {
			Utils.ensure(plans.get(vehicle) != null, rule1);
		}

		// Build up knowledge about our plans:
		// -> how many vehicles pick up/deliver a task
		Map<Task, Integer> pickupCount = new HashMap<Task, Integer>(tasks.size());
		Map<Task, Integer> deliveryCount = new HashMap<Task, Integer>(tasks.size());
		// -> who pick up/deliver a task
		Map<Task, Vehicle> pickupVehicle = new HashMap<Task, Vehicle>(tasks.size());
		Map<Task, Vehicle> deliveryVehicle = new HashMap<Task, Vehicle>(tasks.size());
		// -> and when a task was picked up/delivered
		Map<Task, Integer> pickupVehicleTime = new HashMap<Task, Integer>(tasks.size());
		Map<Task, Integer> deliveryVehicleTime = new HashMap<Task, Integer>(tasks.size());
		// Those last two variables keep track of relative time for the pickup/delivery vehicle;
		// i.e. the index of the corresponding action

		// Iterate on all plans to build up knowledge
		for (Vehicle vehicle : vehicles) {
			List<VehiculeAction> actions = plans.get(vehicle);
			for (int t = 0; t < actions.size(); ++t) {
				VehiculeAction action = actions.get(t);

				if (action.event == Event.PICK) {
					int newCount = pickupCount.getOrDefault(action.task, 0) + 1;
					pickupCount.put(action.task, newCount);

					pickupVehicle.put(action.task, vehicle);

					pickupVehicleTime.put(action.task, t);
				} else {
					int newCount = deliveryCount.getOrDefault(action.task, 0) + 1;
					deliveryCount.put(action.task, newCount);

					deliveryVehicle.put(action.task, vehicle);

					deliveryVehicleTime.put(action.task, t);
				}
			}
		}

		// Ensure rule 2 to 5 hold
		for (Task task : tasks) {
			Utils.ensure(pickupCount.getOrDefault(task, 0) == 1, rule2);
			Utils.ensure(pickupVehicle.get(task) != null, rule2);
			Utils.ensure(pickupVehicleTime.get(task) != null, rule2);

			Utils.ensure(deliveryCount.getOrDefault(task, 0) == 1, rule3);
			Utils.ensure(deliveryVehicle.get(task) != null, rule3);
			Utils.ensure(deliveryVehicleTime.get(task) != null, rule3);

			Utils.ensure(pickupVehicle.get(task).equals(deliveryVehicle.get(task)), rule4);

			Utils.ensure(pickupVehicleTime.get(task) < deliveryVehicleTime.get(task), rule5);
		}
	}
}
