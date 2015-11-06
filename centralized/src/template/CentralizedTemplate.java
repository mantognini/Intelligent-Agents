package template;

//the list of imports
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import logist.LogistSettings;
import logist.agent.Agent;
import logist.behavior.CentralizedBehavior;
import logist.config.Parsers;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * A very simple auction agent that assigns all tasks to its first vehicle and handles them sequentially.
 *
 */
public class CentralizedTemplate implements CentralizedBehavior {

	private long timeout_setup;
	private long timeout_plan;

	@Override
	public void setup(Topology topology, TaskDistribution distribution, Agent agent) {

		// this code is used to get the timeouts
		LogistSettings ls = null;
		try {
			ls = Parsers.parseSettings("config/settings_default.xml");
		} catch (Exception e) {
			throw new RuntimeException("There was a problem loading the configuration file.", e);
		}

		// the setup method cannot last more than timeout_setup milliseconds
		timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
		// the plan method cannot execute more than timeout_plan milliseconds
		timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		long time_start = System.currentTimeMillis();

		// TODO define an option in the agent's XML file to use either the baseline method or our algorithm

		// TODO implement our algorithm by visiting "neighbor plans"

		// System.out.println("Agent " + agent.id() + " has tasks " + tasks);
		// List<Plan> plans = naivePlans(vehicles, tasks);
		List<Plan> plans = slsPlans(vehicles, tasks);

		long time_end = System.currentTimeMillis();
		long duration = time_end - time_start;
		System.out.println("The plan was generated in " + duration + " milliseconds.");

		return plans;
	}

	// Build plans using a SLS-based algorithm
	private List<Plan> slsPlans(List<Vehicle> vehicles, TaskSet tasks) {
		GeneralPlan generalPlans = GeneralPlan.generateInitial(vehicles, tasks);

		// TODO implement me; don't forget about timeout!

		// Convert solution to logist plans format
		Map<Vehicle, Plan> logistPlans = generalPlans.convertToLogistPlans();

		// Keep the correct order for plan
		List<Plan> plans = new ArrayList<Plan>(vehicles.size());
		for (Vehicle vehicle : vehicles) {
			plans.add(logistPlans.get(vehicle));
		}

		return plans;
	}

	private List<Plan> naivePlans(List<Vehicle> vehicles, TaskSet tasks) {
		Plan planVehicle1 = naivePlan(vehicles.get(0), tasks);

		List<Plan> plans = new ArrayList<Plan>();
		plans.add(planVehicle1);
		while (plans.size() < vehicles.size()) {
			plans.add(Plan.EMPTY);
		}

		return plans;
	}

	// Baseline: plan for one vehicle only
	private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);

		for (Task task : tasks) {
			// move: current city => pickup location
			for (City city : current.pathTo(task.pickupCity)) {
				plan.appendMove(city);
			}

			plan.appendPickup(task);

			// move: pickup location => delivery location
			for (City city : task.path()) {
				plan.appendMove(city);
			}

			plan.appendDelivery(task);

			// set current city
			current = task.deliveryCity;
		}
		return plan;
	}
}
