package planner;

import static utils.Utils.ensure;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import logist.simulation.Vehicle;
import logist.task.Task;

/**
 * Base class for planners
 */
public abstract class PlannerTrait {
	public final List<Vehicle> vehicles;
	public final Set<Task> tasks; // Note: we apparently cannot create empty TaskSet so we use regular Set

	public PlannerTrait(List<Vehicle> vehicles) {
		this.vehicles = vehicles;
		this.tasks = new HashSet<>();

		ensure(vehicles.size() > 0, "At least one vehicle is required");
	}

	public PlannerTrait(List<Vehicle> vehicles, Set<Task> tasks) {
		this.vehicles = vehicles;
		this.tasks = tasks;

		ensure(vehicles.size() > 0, "At least one vehicle is required");
	}

	/**
	 * Build a general plan for the agent's vehicles and set of tasks to deliver
	 */
	public abstract GeneralPlan generatePlans(long timeout);

	/**
	 * Similar to generatePlans but this one need to do its best to find *the* optimal plan
	 */
	public abstract GeneralPlan generateFinalPlans(long timeout);

	/**
	 * Create a new planner with one more task
	 */
	public abstract PlannerTrait extendPlan(Task extraTask);
}
