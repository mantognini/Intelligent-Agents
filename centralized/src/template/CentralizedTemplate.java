package template;

//the list of imports
import java.util.ArrayList;
import java.util.List;

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

	private long timeoutPlan;

	@Override
	public void setup(Topology topology, TaskDistribution distribution, Agent agent) {

		// this code is used to get the timeouts
		LogistSettings ls = null;
		try {
			ls = Parsers.parseSettings("config/settings_default.xml");
		} catch (Exception e) {
			throw new RuntimeException("There was a problem loading the configuration file.", e);
		}

		// The setup method cannot last more than timeout_setup milliseconds
		// timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
		// Note: we're not using it

		// The plan method cannot execute more than timeout_plan milliseconds
		timeoutPlan = ls.get(LogistSettings.TimeoutKey.PLAN);
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		long startTime = System.currentTimeMillis();

		// TODO define an option in the agent's XML file to use either the baseline method or our algorithm

		// System.out.println("Agent " + agent.id() + " has tasks " + tasks);
		// List<Plan> plans = naivePlans(vehicles, tasks);
		List<Plan> plans = slsPlans(startTime, vehicles, tasks);

		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		System.out.println("The plan was generated in " + duration + " milliseconds.");

		return plans;
	}

	// Build plans using a SLS-based algorithm
	private List<Plan> slsPlans(long startTime, List<Vehicle> vehicles, TaskSet tasks) {
		/*
		 * Ideas for improvement:
		 * 
		 * - keep a "bestPlanSoFar" variable
		 * 
		 * - start with X random plan (not simply generateInitial plan!) and at each iteration choose to update one of
		 * them randomly
		 */

		// TODO define p as a simulation parameter in the XML file
		double p = 0.1;

		// A ← SelectInitialSolution(X, D, C, f)
		GeneralPlan generalPlans = GeneralPlan.generateInitial(vehicles, tasks);

		do {
			// Aold ← A
			// no need for that

			// N ← ChooseNeighbours(Aold, X, D, C, f)
			List<GeneralPlan> neighbours = generalPlans.generateNeighbors();

			// A ← LocalChoice(N,f)
			GeneralPlan bestNeighbour = Utils.selectBest(neighbours);
			if (Math.random() > p) {
				generalPlans = bestNeighbour;
			}
		} while (!hasPlanTimedOut(startTime));
		// TODO add max number of iterations?

		// Convert solution to logist plans format
		List<Plan> logistPlans = generalPlans.convertToLogistPlans();

		return logistPlans;
	}

	private boolean hasPlanTimedOut(long startTime) {
		long currentTime = System.currentTimeMillis();
		return currentTime - startTime > timeoutPlan;
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
