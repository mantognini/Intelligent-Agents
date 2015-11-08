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

	enum Algorithm {
		NAIVE, SLS
	}

	Algorithm algorithm;

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

		String algorithmName = agent.readProperty("algorithm", String.class, "NAIVE");

		algorithm = Algorithm.valueOf(algorithmName.toUpperCase());

	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		long startTime = System.currentTimeMillis();

		// TODO define an option in the agent's XML file to use either the baseline method or our algorithm

		// System.out.println("Agent " + agent.id() + " has tasks " + tasks);
		// List<Plan> plans = naivePlans(vehicles, tasks);
		List<Plan> plans;
		switch (algorithm) {
		case NAIVE:
			plans = naivePlans(vehicles, tasks);
			break;
		case SLS:
			plans = slsPlans(startTime, vehicles, tasks);
			break;
		default:
			throw new AssertionError("Should not happen.");
		}

		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		System.out.println("Algorithm : " + algorithm);
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
		double p = 0.7;

		// A ← SelectInitialSolution(X, D, C, f)
		// TODO Is it normal that generateInitial produce solutions with only one active vehicle?
		GeneralPlan generalPlans = GeneralPlan.generateInitial(vehicles, tasks);
		// While this works "better"
		// GeneralPlan generalPlans = GeneralPlan.generateRandomInitial(vehicles, tasks);
		// TODO we need a better metric to judge how the company is doing maybe?

		int i = 0;
		do {
			++i;
			// Aold ← A
			// no need for that

			// N ← ChooseNeighbours(Aold, X, D, C, f)
			List<GeneralPlan> neighbors = generalPlans.generateNeighbors();

			// A ← LocalChoice(N,f)
			// GeneralPlan bestNeighbour = Utils.selectBest(null, neighbors);
			if (Math.random() > p) {
				generalPlans = Utils.selectBest(generalPlans, neighbors);
			} else {
				generalPlans = Utils.getRandomElement(neighbors);
			}

			System.out.println("New general plans #" + i + ":\n" + generalPlans);
		} while (i < 100 && !hasPlanTimedOut(startTime));
		// TODO add max number of iterations?

		// Convert solution to logist plans format
		List<Plan> logistPlans = generalPlans.convertToLogistPlans();

		return logistPlans;
	}

	private boolean hasPlanTimedOut(long startTime) {
		long currentTime = System.currentTimeMillis();
		long duration = currentTime - startTime;

		// Increase duration by 10% to account for next iteration + plan convertion
		duration *= 1.1;

		return duration > timeoutPlan;
	}

	private List<Plan> naivePlans(List<Vehicle> vehicles, TaskSet tasks) {
		Plan planVehicle1 = naivePlan(vehicles.get(0), tasks);

		List<Plan> plans = new ArrayList<>();
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
