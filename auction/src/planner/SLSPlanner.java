package planner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import logist.simulation.Vehicle;
import logist.task.Task;
import planner.Action.Event;
import utils.Utils;

public class SLSPlanner extends PlannerTrait {

	// SLS SETTINGS:
	static class Settings {
		public final int resetBound;
		public final int stallBound;
		public final double p;
		public final int debugLevel; // the higher the more verbose

		// TODO add timeout

		public Settings(int resetBound, int stallBound, double p, int debugLevel) {
			this.resetBound = resetBound;
			this.stallBound = stallBound;
			this.p = p;
			this.debugLevel = debugLevel;
		}
	}

	public static final Settings FAST_SETTIGNS = new Settings(5, 300, 0.5, 0);
	public static final Settings NORMAL_SETTIGNS = new Settings(5, 3000, 0.5, 0);
	public static final Settings OPTIMAL_SETTINGS = new Settings(7, 4000, 0.5, 0);

	private GeneralPlan plansCache = null;
	private Map<Vehicle, List<Action>> plans = null;
	private final Settings regularMode;
	private final Settings optimalMode;

	public SLSPlanner(List<Vehicle> vehicles, Settings regularMode, Settings optimalMode) {
		super(vehicles);

		this.regularMode = regularMode;
		this.optimalMode = optimalMode;

		generateInitial();
	}

	private SLSPlanner(List<Vehicle> vehicles, Set<Task> tasks, Map<Vehicle, List<Action>> plans, Settings regularMode,
			Settings optimalMode) {
		super(vehicles, tasks);

		this.plans = plans; // initial plan
		this.regularMode = regularMode;
		this.optimalMode = optimalMode;
	}

	@Override
	public GeneralPlan generatePlans() {
		if (plansCache == null) {
			buildPlan(regularMode);
		}
		return plansCache;
	}

	@Override
	public GeneralPlan generateFinalPlans() {
		GeneralPlan previous = plansCache; // might be null

		// Reset cache, and use special setting for optimality and rebuild plan
		plans = null;
		plansCache = null;

		generateInitial();
		buildPlan(optimalMode);

		return previous == null ? plansCache : Utils.selectBest(previous, plansCache);
	}

	@Override
	public PlannerTrait extendPlan(Task extraTask) {
		Set<Task> extendedTasks = new HashSet<>(tasks);
		extendedTasks.add(extraTask);

		Vehicle biggest = Utils.getBiggestVehicle(vehicles);
		Map<Vehicle, List<Action>> extendedInitialPlans = getCopyOfPlans();
		extendedInitialPlans.get(biggest).add(new Action(Event.PICK, extraTask));
		extendedInitialPlans.get(biggest).add(new Action(Event.DELIVER, extraTask));

		return new SLSPlanner(vehicles, extendedTasks, extendedInitialPlans, regularMode, optimalMode);
	}

	private void buildPlan(Settings settings) {
		GeneralPlan current = new GeneralPlan(plans, vehicles);

		if (tasks.size() == 0) {
			plansCache = current;
			return;
		}

		debugPrintln(settings, 1, "Generate Neighbours");

		GeneralPlan globalBest = current;
		GeneralPlan localBest = globalBest;
		int bestReset = 0;

		int iterationCount = 0;
		int stallCount = 0;
		int resetCount = 0;

		do {
			++iterationCount;
			// Aold ← A
			// no need for that

			// N ← ChooseNeighbours(Aold, X, D, C, f)
			List<GeneralPlan> neighbors = generateNeighbors();

			// A ← LocalChoice(N,f)
			// GeneralPlan bestNeighbour = Utils.selectBest(null, neighbors);
			if (Math.random() > settings.p) {
				current = Utils.selectBest(current, neighbors);
			} else {
				current = Utils.getRandomElement(neighbors);
			}

			plans = current.getPlans();

			GeneralPlan previousLocalBest = localBest;
			localBest = Utils.selectBest(localBest, current);

			// Reset generalPlans is stuck in a local minimum
			if (previousLocalBest == localBest) { // yes, address comparison.
				++stallCount;
			} else {
				stallCount = 0;
				debugPrintln(settings, 3, "LOCAL best was improved at iteration " + iterationCount);
				debugPrintln(settings, 3, "Previous cost was " + previousLocalBest.computeCost());
				debugPrintln(settings, 3, "New      cost is  " + localBest.computeCost());
			}

			if (stallCount >= settings.stallBound) {
				debugPrintln(settings, 2, "### plans were RESET at iteration " + iterationCount + "###");

				// Save local best if better than global best
				GeneralPlan previousGlobalBest = globalBest;
				globalBest = Utils.selectBest(globalBest, localBest);
				if (previousGlobalBest != globalBest) {
					debugPrintln(settings, 2, "\t>>> GLOBAL best was improved at reset " + resetCount + "<<<");
					debugPrintln(settings, 2, "\t>>> Previous cost was " + previousGlobalBest.computeCost());
					debugPrintln(settings, 2, "\t>>> New      cost is  " + globalBest.computeCost());
					bestReset = resetCount;
				}

				// Reset!
				stallCount = 0;
				iterationCount = 0;
				++resetCount;

				if (resetCount < settings.resetBound) {
					generateInitial();
					current = new GeneralPlan(plans, vehicles);
					localBest = current;
				} // else: no need to do it
			}

		} while (resetCount < settings.resetBound /* && !hasPlanTimedOut(startTime) */);
		// TODO add timeout

		debugPrintln(settings, 1, "Best plan cost is " + globalBest.computeCost() + " and was found at reset = "
				+ bestReset);
		if (bestReset >= resetCount - 1) {
			debugPrintln(settings, 0, "The best plan was found during the last reset iteration!");
		}

		plans = globalBest.getPlans();
		plansCache = globalBest;
	}

	private void debugPrintln(Settings settings, int level, String msg) {
		if (level <= settings.debugLevel) {
			System.err.println(msg);
		}
	}

	/**
	 * Generate the first, naive plan: all tasks are assigned to be biggest vehicle in a sequential order.
	 */
	private void generateInitial() {
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
	private List<GeneralPlan> generateNeighbors() {

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
		Vehicle modelVehicle;

		do {
			modelVehicle = Utils.getRandomElement(vehicles);
		} while (plans.get(modelVehicle).size() == 0);

		return modelVehicle;
	}

}
