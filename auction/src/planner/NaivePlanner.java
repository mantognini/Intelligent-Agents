package planner;

import static utils.Utils.ensure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import logist.simulation.Vehicle;
import logist.task.Task;
import planner.Action.Event;
import utils.Utils;

/**
 * Sequential, naive planner; use only one vehicle
 */
public final class NaivePlanner extends PlannerTrait {

	// Caches
	private Map<Vehicle, List<Action>> plans = null;
	private Vehicle biggestVehicle = null;

	public NaivePlanner(List<Vehicle> vehicles) {
		super(vehicles);
	}

	// Internal constructor
	private NaivePlanner(List<Vehicle> vehicles, Set<Task> tasks, Map<Vehicle, List<Action>> plans,
			Vehicle biggestVehicle) {
		super(vehicles, tasks);

		this.plans = plans;
		this.biggestVehicle = biggestVehicle;
	}

	@Override
	public GeneralPlan generatePlans() {
		if (plans == null)
			buildPlans();

		return new GeneralPlan(plans, vehicles);
	}

	private void buildPlans() {
		biggestVehicle = Utils.getBiggestVehicle(vehicles);
		int heaviest = Utils.getHeaviestWeight(tasks);

		ensure(heaviest <= biggestVehicle.capacity(), "no vehicle is big enough");

		List<Action> planForBiggest = new ArrayList<>(tasks.size() * 2);

		for (Task task : tasks) {
			// move & pickup
			planForBiggest.add(new Action(Event.PICK, task));

			// move & deliver
			planForBiggest.add(new Action(Event.DELIVER, task));
		}

		// Build vehicles' actions lists
		plans = new HashMap<>(vehicles.size());
		for (Vehicle v : vehicles) {
			if (v.equals(biggestVehicle))
				plans.put(v, planForBiggest);
			else
				plans.put(v, new ArrayList<>());
		}
	}

	@Override
	public PlannerTrait extendPlan(Task extraTask) {
		if (plans == null)
			buildPlans();

		// We don't rebuild everything from scratch; instead we just extends the current plan
		ensure(extraTask.weight <= biggestVehicle.capacity(), "biggest vehicle is too small for new task");
		List<Action> extendedPlanForBiggest = new ArrayList<>(plans.get(biggestVehicle));
		extendedPlanForBiggest.add(new Action(Event.PICK, extraTask));
		extendedPlanForBiggest.add(new Action(Event.DELIVER, extraTask));

		HashMap<Vehicle, List<Action>> extendedPlans = new HashMap<>(plans);
		extendedPlans.put(biggestVehicle, extendedPlanForBiggest);

		return new NaivePlanner(vehicles, tasks, extendedPlans, biggestVehicle);
	}

}
