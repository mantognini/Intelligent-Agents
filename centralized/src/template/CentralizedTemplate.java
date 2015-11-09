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

	/*
	 * Type of possible algorithm
	 */
	enum Algorithm {
		NAIVE, SLS, SLS_RANDOM_INITIAL, SLS_GENETIC
	}

	/**
	 * Type of choosen algorithm
	 */
	Algorithm algorithm;

	/**
	 * Probability to take the new plan
	 */
	double p;

	/**
	 * Population size for SLS-genetic algorithm
	 */
	int geneticPopulationSize;

	int bound;
	int stallBound;

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
		bound = agent.readProperty("bound", Integer.class, 10000);
		stallBound = agent.readProperty("stall", Integer.class, 100);

		p = agent.readProperty("probability", Double.class, 0.5);

		geneticPopulationSize = agent.readProperty("populationSize", Integer.class, 10);
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		long startTime = System.currentTimeMillis();

		// System.out.println("Agent " + agent.id() + " has tasks " + tasks);
		// List<Plan> plans = naivePlans(vehicles, tasks);
		List<Plan> plans;
		switch (algorithm) {
		case NAIVE:
			plans = naivePlans(vehicles, tasks);
			break;

		case SLS:
			plans = slsPlans(false, startTime, vehicles, tasks);
			break;

		case SLS_RANDOM_INITIAL:
			plans = slsPlans(true, startTime, vehicles, tasks);
			break;

		case SLS_GENETIC:
			plans = slsGeneticPlans(startTime, vehicles, tasks);
			break;

		default:
			throw new AssertionError("Should not happen.");
		}

		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		System.out.println("Algorithm : " + algorithm);
		System.out.println("The plan was generated in " + duration + " milliseconds.");

		int cost = 0;
		for (int i = 0; i < plans.size(); ++i) {
			Plan p = plans.get(i);
			Vehicle v = vehicles.get(i);
			cost += p.totalDistance() * v.costPerKm();
		}
		System.out.println("Overall cost of plans: " + cost);

		return plans;
	}

	// Build plans using a SLS-based algorithm
	private List<Plan> slsPlans(boolean randomInitial, long startTime, List<Vehicle> vehicles, TaskSet tasks) {
		// A ← SelectInitialSolution(X, D, C, f)

		GeneralPlan generalPlans;
		if (randomInitial)
			generalPlans = GeneralPlan.generateRandomInitial(vehicles, tasks);
		else
			generalPlans = GeneralPlan.generateInitial(vehicles, tasks);

		System.out.println("Generate Neighbours");

		GeneralPlan bestSoFar = generalPlans;

		int iterationCount = 0;
		int stallCount = 0;

		do {
			++iterationCount;
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
					generalPlans = GeneralPlan.generateRandomInitial(vehicles, tasks);
				else
					generalPlans = GeneralPlan.generateInitial(vehicles, tasks);

				stallCount = 0;
				bestSoFar = Utils.selectBest(generalPlans, bestSoFar);

				System.out.println("plans were reset");
			}
		} while (iterationCount < bound && !hasPlanTimedOut(startTime));

		// Convert solution to logist plans format
		List<Plan> logistPlans = bestSoFar.convertToLogistPlans();

		return logistPlans;
	}

	private List<Plan> slsGeneticPlans(long startTime, List<Vehicle> vehicles, TaskSet tasks) {
		// Generate an initial random population of general plans
		GeneralPlan[] population = new GeneralPlan[geneticPopulationSize];
		for (int i = 0; i < geneticPopulationSize; ++i) {
			population[i] = GeneralPlan.generateRandomInitial(vehicles, tasks);
		}

		// Keep track of the best so far
		GeneralPlan bestSoFar = Utils.selectBest(null, population);

		System.out.println("Population size is " + geneticPopulationSize);

		do {
			// Select a random individual (i.e. a general plan) & mutate it
			int rank = Utils.uniform(0, geneticPopulationSize - 1);
			population[rank] = population[rank].mutate();

			bestSoFar = Utils.selectBest(bestSoFar, population[rank]);

		} while (!hasPlanTimedOut(startTime));

		return bestSoFar.convertToLogistPlans();
	}

	private boolean hasPlanTimedOut(long startTime) {
		long currentTime = System.currentTimeMillis();
		long duration = currentTime - startTime;

		// Increase duration by 10% to account for next iteration + plan conversion
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
