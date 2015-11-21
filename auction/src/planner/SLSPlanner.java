package planner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import logist.simulation.Vehicle;
import logist.task.Task;
import planner.Action.Event;
import utils.Utils;

public class SLSPlanner extends PlannerTrait {

	private Map<Vehicle, List<Action>> plans;
	private Random randomGenerator = new Random();
	private boolean randomInitial = false;
	private int bound = 50000;
	private int stallBound = 5000;
	private double p = 0.5;

	public SLSPlanner(List<Vehicle> vehicles) {
		super(vehicles);
	}

	public SLSPlanner(List<Vehicle> vehicles, Set<Task> tasks) {
		super(vehicles, tasks);

	}

	@Override
	public GeneralPlan generatePlans() {
		if (plans == null) {
			buildPlan(randomInitial);
		}
		return new GeneralPlan(plans, vehicles);
	}

	private void buildPlan(boolean randomInitial) {
		if (randomInitial)
			generateRandomInitial(vehicles, tasks);
		else
			generateInitial(vehicles, tasks);

		GeneralPlan generalPlans = new GeneralPlan(plans, vehicles);

		System.out.println("Generate Neighbours");

		GeneralPlan bestSoFar = generalPlans;

		int iterationCount = 0;
		int stallCount = 0;

		do {
			++iterationCount;
			// Aold ← A
			// no need for that

			// N ← ChooseNeighbours(Aold, X, D, C, f)
			List<GeneralPlan> neighbors = generateNeighbors();

			// A ← LocalChoice(N,f)
			// GeneralPlan bestNeighbour = Utils.selectBest(null, neighbors);
			if (Math.random() > p) {
				generalPlans = Utils.selectBest(generalPlans, neighbors);
			} else {
				generalPlans = Utils.getRandomElement(neighbors);
			}

			plans = generalPlans.getPlans();

			GeneralPlan previousBest = bestSoFar;
			bestSoFar = Utils.selectBest(generalPlans, bestSoFar);

			// Reset generalPlans is stuck in a local minima
			if (previousBest == bestSoFar) { // yes, address comparison.
				++stallCount;
			} else {
				stallCount = 0;
			}

			if (stallCount >= stallBound) {
				// Reset!
				if (randomInitial)
					generateRandomInitial(vehicles, tasks);
				else
					generateInitial(vehicles, tasks);

				generalPlans = new GeneralPlan(plans, vehicles);
				stallCount = 0;
				bestSoFar = Utils.selectBest(generalPlans, bestSoFar);

				System.out.println("plans were reset");
			}

		} while (iterationCount < bound /* && !hasPlanTimedOut(startTime) */);

		plans = bestSoFar.getPlans();
	}

	@Override
	public PlannerTrait extendPlan(Task extraTask) {
		Set<Task> extendedTasks = new HashSet<>(tasks);
		extendedTasks.add(extraTask);
		return new SLSPlanner(vehicles, extendedTasks);
	}

	/**
	 * Generate the first, naive plan: all tasks are assigned to be biggest vehicle in a sequential order.
	 */
	public void generateInitial(List<Vehicle> vehicles, Set<Task> tasks) {
		assert vehicles.size() > 0;
		Vehicle biggest = Utils.getBiggestVehicle(vehicles);
		int heaviest = Utils.getHeaviestWeight(tasks);

		if (biggest.capacity() < heaviest)
			throw new RuntimeException("Impossible to plan: vehicles are not big enough");

		List<Action> planForBiggest = new LinkedList<>();

		for (Task task : tasks) {
			// move & pickup
			planForBiggest.add(new Action(Event.PICK, task));

			// move & deliver
			planForBiggest.add(new Action(Event.DELIVER, task));
		}

		// Build vehicles' actions lists
		plans = new HashMap<>(vehicles.size());
		for (Vehicle v : vehicles) {
			if (v.equals(biggest))
				plans.put(v, planForBiggest);
			else
				plans.put(v, new LinkedList<>());
		}

	}

	/**
	 * Randomly assign the task to different vehicles.
	 */
	public void generateRandomInitial(List<Vehicle> vehicles, Set<Task> tasks) {
		assert vehicles.size() > 0;
		Vehicle biggest = Utils.getBiggestVehicle(vehicles);
		int heaviest = Utils.getHeaviestWeight(tasks);

		if (biggest.capacity() < heaviest)
			throw new RuntimeException("Impossible to plan: vehicles are not big enough");

		// Build vehicles' actions lists
		plans = new HashMap<>(vehicles.size());
		for (Vehicle v : vehicles) {
			plans.put(v, new LinkedList<>());
		}

		// Affect each task to a random vehicle
		for (Task task : tasks) {
			Vehicle vehicle;
			do {
				vehicle = Utils.getRandomElement(vehicles);
			} while (vehicle.capacity() < task.weight);

			// move & pickup
			plans.get(vehicle).add(new Action(Event.PICK, task));

			// move & deliver
			plans.get(vehicle).add(new Action(Event.DELIVER, task));
		}

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

		List<Action> modelPlan = plans.get(modelVehicle);
		for (int i = 0; i < modelPlan.size(); ++i) {
			Action action = modelPlan.get(i);
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
		List<Action> newSourcePlan = getCopyOfVehiclePlan(sourceVehicle);
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
			LinkedList<Action> newDestinationPlan = getCopyOfVehiclePlan(destinationVehicle);
			newDestinationPlan.addFirst(new Action(Event.DELIVER, transferedTask));
			newDestinationPlan.addFirst(new Action(Event.PICK, transferedTask));

			// And combine everything together
			Map<Vehicle, List<Action>> newPlans = getCopyOfPlans();
			newPlans.put(sourceVehicle, newSourcePlan);
			newPlans.put(destinationVehicle, newDestinationPlan);
			GeneralPlan newGeneralPlan = new GeneralPlan(newPlans, vehicles);
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

		final List<Action> originalPlan = plans.get(vehicle);

		// Compute load at pickup time
		int load = computeLoadAtTime(actionIndex, originalPlan);

		/* Try to go back in time and advance the pick up action */

		// First attempt: just before original time
		int t = actionIndex - 1;

		// Continue if beginning of time is not in the future and not overloaded
		while (t >= 0 && load - originalPlan.get(t).getDifferentialWeight() <= vehicle.capacity()) {
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

		final List<Action> originalPlan = plans.get(vehicle);
		final Task movedTask = originalPlan.get(actionIndex).task;

		if (actionIndex + 1 == originalPlan.size())
			return neighbours; // no need to do more work: it cannot be postponed

		/* Try to go forward in time and postpone the pick up action */

		// First attempt: just after original time
		int t = actionIndex + 1;

		// Continue if end of time is not in the past and delivery action is still in the future
		while (t < originalPlan.size() && !originalPlan.get(t).task.equals(movedTask)) {
			// The vehicle has enough room at time t so let's deliver the task later
			GeneralPlan newGeneralPlan = createGeneralPlanByMovingAction(vehicle, actionIndex, t);
			neighbours.add(newGeneralPlan);

			// Go one step further in time
			++t;
		}

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

		final List<Action> originalPlan = plans.get(vehicle);
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

		final List<Action> originalPlan = plans.get(vehicle);

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
		Map<Vehicle, List<Action>> newPlans = getCopyOfPlans();

		// Move the given action carefully: make sure destination index is not invalidated
		List<Action> newVehiclePlan = newPlans.get(vehicle);
		Action action = newVehiclePlan.remove(sourceIndex);
		if (sourceIndex >= destinationIndex)
			newVehiclePlan.add(destinationIndex, action);
		else
			newVehiclePlan.add(destinationIndex - 1, action);

		GeneralPlan newGeneralPlan = new GeneralPlan(newPlans, vehicles);
		return newGeneralPlan;
	}

	private int computeLoadAtTime(int timeIndex, List<Action> plan) {
		int load = 0;
		for (int i = 0; i <= timeIndex; ++i) {
			Action action = plan.get(i);
			load += action.getDifferentialWeight();
		}
		return load;
	}

	// Performs deep copy of the `vehicle`'s plan
	private LinkedList<Action> getCopyOfVehiclePlan(Vehicle vehicle) {
		return new LinkedList<>(plans.get(vehicle));
	}

	// Performs deep copy of all plans
	private Map<Vehicle, List<Action>> getCopyOfPlans() {
		// NOTE: using new HashMap<>(plans) won't work as values are mutable lists.

		Map<Vehicle, List<Action>> copy = new HashMap<>(vehicles.size());
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

}
