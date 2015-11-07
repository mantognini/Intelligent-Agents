package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;
import template.VehicleAction.Event;

public class GeneralPlan {

	private final Map<Vehicle, List<VehicleAction>> plans; // One plan per vehicle

	// Keep track of those for validation purposes
	private final List<Vehicle> vehicles;
	private final TaskSet tasks;

	private final Random randomGenerator = new Random();

	/**
	 * Private constructor; use generateInitial static factory to build the first plan, then use generateNeighbors to
	 * navigate onto the plan space.
	 */
	private GeneralPlan(Map<Vehicle, List<VehicleAction>> plans, List<Vehicle> vehicles, TaskSet tasks) {
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

		List<VehicleAction> planForBiggest = new LinkedList<>();

		for (Task task : tasks) {
			// move & pickup
			planForBiggest.add(new VehicleAction(Event.PICK, task));

			// move & deliver
			planForBiggest.add(new VehicleAction(Event.DELIVER, task));
		}

		// Build vehicles' actions lists
		Map<Vehicle, List<VehicleAction>> plans = new HashMap<>(vehicles.size());
		for (Vehicle v : vehicles) {
			if (v.equals(biggest))
				plans.put(v, planForBiggest);
			else
				plans.put(v, new LinkedList<>());
		}

		return new GeneralPlan(plans, vehicles, tasks);
	}

	/**
	 * Neighbor plans are computed using five strategies:
	 * 
	 * - the first task of a vehicle is transfered to another vehicle as long as this other vehicle has enough capacity;
	 * 
	 * - the pick time for a given task can be advanced as long as, at no point in time, the vehicle is overloaded;
	 * 
	 * - the pick time for a given task can be postponed as long as the delivery time is still after the pick up time;
	 * 
	 * - the delivery time for a given task can be advanced as long as the delivery time is still after the pick up
	 * time;
	 * 
	 * - the delivery time for a given task can be postponed as long as, at no point in time, the vehicle is overloaded.
	 * 
	 * Note that this method doesn't build the full set of neighbors as it would get too big. Instead the neighbor plans
	 * are stochastically selected. This means that running this methods twice might result in two different solution
	 * sets.
	 */
	public List<GeneralPlan> generateNeighbors() {

		List<GeneralPlan> neighbours = new LinkedList<>();

		// Apply strategies for a randomly selected, non-empty vehicle
		Vehicle modelVehicle = selectRandomVehicle();

		neighbours.addAll(swapFirstTask(modelVehicle));

		List<VehicleAction> modelPlan = plans.get(modelVehicle);
		for (int i = 0; i < modelPlan.size(); ++i) {
			VehicleAction action = modelPlan.get(i);
			if (action.event == Event.PICK) {
				neighbours.addAll(advancePickUp(modelVehicle, i));
				neighbours.addAll(postponePickUp(modelVehicle, i));
			} else {
				neighbours.addAll(advanceDelivery(modelVehicle, i));
				neighbours.addAll(postponeDelivery(modelVehicle, i));
			}
		}

		return neighbours;
	}

	private List<GeneralPlan> swapFirstTask(Vehicle sourceVehicle) {
		Utils.ensure(plans.get(sourceVehicle).size() > 0, "swapFirstTask needs a vehicle with at least one task");

		List<GeneralPlan> neighbours = new LinkedList<>();

		// Transfer the first task from the source vehicle to the other vehicles
		List<VehicleAction> newSourcePlan = getCopyOfVehiclePlan(sourceVehicle);
		Task transferedTask = newSourcePlan.get(0).task;

		// Remove pickup & deliver actions from the source vehicle
		newSourcePlan.remove(0);
		for (int i = 0; i < newSourcePlan.size(); ++i) {
			if (newSourcePlan.get(i).task.equals(transferedTask)) {
				newSourcePlan.remove(i);
				break;
			}
		}

		// Attempt to transfer the task to other vehicles
		for (Vehicle destinationVehicle : vehicles) {
			// Skip the source vehicle
			if (destinationVehicle.equals(sourceVehicle))
				continue;

			// Skip small vehicle
			if (destinationVehicle.capacity() < transferedTask.weight)
				continue;

			// Build new plan for destination vehicle
			LinkedList<VehicleAction> newDestinationPlan = getCopyOfVehiclePlan(destinationVehicle);
			newDestinationPlan.addFirst(new VehicleAction(Event.DELIVER, transferedTask));
			newDestinationPlan.addFirst(new VehicleAction(Event.PICK, transferedTask));

			// And combine everything together
			Map<Vehicle, List<VehicleAction>> newPlans = getCopyOfPlans();
			newPlans.put(sourceVehicle, newSourcePlan);
			newPlans.put(destinationVehicle, newDestinationPlan);
			GeneralPlan newGeneralPlan = new GeneralPlan(newPlans, vehicles, tasks);
			neighbours.add(newGeneralPlan);
		}

		return neighbours;
	}

	private List<GeneralPlan> advancePickUp(Vehicle vehicle, int actionIndex) {
		Utils.ensure(plans.get(vehicle).size() > actionIndex, "advancePickUp needs a vehicle with at least "
				+ actionIndex + " events");
		Utils.ensure(plans.get(vehicle).get(actionIndex).event == Event.PICK,
				"advancePickUp needs an index corresponding to a pick up event");

		List<GeneralPlan> neighbours = new LinkedList<>();

		if (actionIndex == 0)
			return neighbours; // no need to do more work: it cannot be advanced

		final List<VehicleAction> originalPlan = plans.get(vehicle);
		final Task movedTask = originalPlan.get(actionIndex).task;

		// Compute load right before pickup
		int load = computeLoadAtTime(actionIndex - 1, originalPlan);

		/* Try to go back in time and advance the pick up action */

		// First attempt: just before original time
		int t = actionIndex - 1;

		// Continue if beginning of time is not in the future and not overloaded
		while (t >= 0 && load + movedTask.weight <= vehicle.capacity()) {
			GeneralPlan newGeneralPlan = createGeneralPlanByMovingAction(vehicle, actionIndex, t);
			neighbours.add(newGeneralPlan);

			// Go one step back in time and update weight
			load -= originalPlan.get(t).getDifferentialWeight();
			--t;
		}

		return neighbours;
	}

	private List<GeneralPlan> postponePickUp(Vehicle vehicle, int actionIndex) {
		Utils.ensure(plans.get(vehicle).size() > actionIndex, "postponePickUp needs a vehicle with at least "
				+ actionIndex + " events");
		Utils.ensure(plans.get(vehicle).get(actionIndex).event == Event.PICK,
				"postponePickUp needs an index corresponding to a pick up event");

		List<GeneralPlan> neighbours = new LinkedList<>();
		// TODO Auto-generated method stub
		return neighbours;
	}

	private List<GeneralPlan> advanceDelivery(Vehicle vehicle, int actionIndex) {
		Utils.ensure(plans.get(vehicle).size() > actionIndex, "advanceDelivery needs a vehicle with at least "
				+ actionIndex + " events");
		Utils.ensure(plans.get(vehicle).get(actionIndex).event == Event.DELIVER,
				"advanceDelivery needs an index corresponding to a pick up event");

		List<GeneralPlan> neighbours = new LinkedList<>();

		if (actionIndex == 0)
			return neighbours; // no need to do more work: it cannot be advanced

		/* Try to go back in time and advance the delivery action */

		final List<VehicleAction> originalPlan = plans.get(vehicle);
		final Task movedTask = originalPlan.get(actionIndex).task;

		// First attempt: just before original time
		int t = actionIndex - 1;

		// Continue if beginning of time is not in the future and pick up action is still in the past
		while (t >= 0 && !originalPlan.get(t).task.equals(movedTask)) {
			GeneralPlan newGeneralPlan = createGeneralPlanByMovingAction(vehicle, actionIndex, t);
			neighbours.add(newGeneralPlan);

			// Go one step back in time
			--t;
		}

		return neighbours;
	}

	private List<GeneralPlan> postponeDelivery(Vehicle vehicle, int actionIndex) {
		Utils.ensure(plans.get(vehicle).size() > actionIndex, "postponeDelivery needs a vehicle with at least "
				+ actionIndex + " events");
		Utils.ensure(plans.get(vehicle).get(actionIndex).event == Event.DELIVER,
				"postponeDelivery needs an index corresponding to a pick up event");

		List<GeneralPlan> neighbours = new LinkedList<>();

		final List<VehicleAction> originalPlan = plans.get(vehicle);

		if (actionIndex + 1 == originalPlan.size())
			return neighbours; // no need to do more work: it cannot be postponed

		// Compute load right before delivery
		int load = computeLoadAtTime(actionIndex - 1, originalPlan);

		/* Try to go forward in time and postpone the delivery action */

		// First attempt: just after original time
		int t = actionIndex + 1;

		// Continue if end of time is not in the past and not overloaded
		while (t < originalPlan.size() && load + originalPlan.get(t).getDifferentialWeight() <= vehicle.capacity()) {
			// The vehicle has enough room at time t so let's deliver the task later
			GeneralPlan newGeneralPlan = createGeneralPlanByMovingAction(vehicle, actionIndex, t);
			neighbours.add(newGeneralPlan);

			// Go one step further in time and update weight
			load += originalPlan.get(t).getDifferentialWeight();
			++t;
		}

		return neighbours;
	}

	/**
	 * Move the action at index `sourceIndex` of the given `vehicle` to index `destinationIndex` correctly
	 */
	private GeneralPlan createGeneralPlanByMovingAction(Vehicle vehicle, int sourceIndex, int destinationIndex) {
		Map<Vehicle, List<VehicleAction>> newPlans = getCopyOfPlans();

		// Move the given action carefully: make sure destination index is not invalidated
		List<VehicleAction> newVehiclePlan = newPlans.get(vehicle);
		VehicleAction action = newVehiclePlan.remove(sourceIndex);
		if (sourceIndex >= destinationIndex)
			newVehiclePlan.add(destinationIndex, action);
		else
			newVehiclePlan.add(destinationIndex - 1, action);

		GeneralPlan newGeneralPlan = new GeneralPlan(newPlans, vehicles, tasks);
		return newGeneralPlan;
	}

	private int computeLoadAtTime(int timeIndex, List<VehicleAction> plan) {
		int load = 0;
		for (int i = 0; i <= timeIndex; ++i) {
			VehicleAction action = plan.get(i);
			load += action.getDifferentialWeight();
		}
		return load;
	}

	private LinkedList<VehicleAction> getCopyOfVehiclePlan(Vehicle vehicle) {
		return new LinkedList<>(plans.get(vehicle));
	}

	private Map<Vehicle, List<VehicleAction>> getCopyOfPlans() {
		// NOTE: using new HashMap<>(plans) won't work as value are mutable lists.

		Map<Vehicle, List<VehicleAction>> copy = new HashMap<>(vehicles.size());
		for (Vehicle vehicle : vehicles) {
			copy.put(vehicle, getCopyOfVehiclePlan(vehicle));
		}
		return copy;
	}

	/**
	 * The returned vehicle has at least one task on its agenda
	 */
	private Vehicle selectRandomVehicle() {
		Vehicle modelVehicule;
		do {
			int index = randomGenerator.nextInt(vehicles.size());
			modelVehicule = vehicles.get(index);
		} while (plans.get(modelVehicule).size() == 0);

		return modelVehicule;
	}

	public double computeOverallCost() {
		// Converting to logist plan format will probably significantly slow down things.
		// Instead we could compute the cost ourselves.
		// TODO determine if computeOverallCost is a bottleneck and if so optimize it.

		double cost = 0;
		for (Vehicle vehicle : vehicles) {
			List<VehicleAction> plan = plans.get(vehicle);
			Plan logistPlan = convertToLogistPlan(vehicle, plan);
			cost += logistPlan.totalDistance() * vehicle.costPerKm();
		}

		return cost;
	}

	public List<Plan> convertToLogistPlans() {
		// Keep the correct order for plan
		List<Plan> logistPlans = new ArrayList<>(vehicles.size());
		for (Vehicle vehicle : vehicles) {
			List<VehicleAction> plan = plans.get(vehicle);
			Plan logistPlan = convertToLogistPlan(vehicle, plan);
			logistPlans.add(logistPlan);
		}

		return logistPlans;
	}

	private Plan convertToLogistPlan(Vehicle vehicle, List<VehicleAction> actions) {
		City currentCity = vehicle.getCurrentCity();
		Plan logistPlan = new Plan(currentCity);

		for (VehicleAction action : actions) {
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
		final String rule6 = "No vehicle should be overloaded";

		// Ensure the first rule holds
		for (Vehicle vehicle : vehicles) {
			Utils.ensure(plans.get(vehicle) != null, rule1);
		}

		// Build up knowledge about our plans:
		// -> how many vehicles pick up/deliver a task
		Map<Task, Integer> pickupCount = new HashMap<>(tasks.size());
		Map<Task, Integer> deliveryCount = new HashMap<>(tasks.size());
		// -> who pick up/deliver a task
		Map<Task, Vehicle> pickupVehicle = new HashMap<>(tasks.size());
		Map<Task, Vehicle> deliveryVehicle = new HashMap<>(tasks.size());
		// -> and when a task was picked up/delivered
		Map<Task, Integer> pickupVehicleTime = new HashMap<>(tasks.size());
		Map<Task, Integer> deliveryVehicleTime = new HashMap<>(tasks.size());
		// Those last two variables keep track of relative time for the pickup/delivery vehicle;
		// i.e. the index of the corresponding action

		// Iterate on all plans to build up knowledge
		for (Vehicle vehicle : vehicles) {
			List<VehicleAction> actions = plans.get(vehicle);

			// TODO check that rule6 holds here

			for (int t = 0; t < actions.size(); ++t) {
				VehicleAction action = actions.get(t);

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

	@Override
	public String toString() {
		String rep = "";

		for (Vehicle vehicle : vehicles) {
			rep += "Plan for vehicle n° " + vehicle.id() + ": " + plans.get(vehicle) + "\n";
		}

		return rep;
	}

}
