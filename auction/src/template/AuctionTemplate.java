package template;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import logist.Measures;
import logist.agent.Agent;
import logist.behavior.AuctionBehavior;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;
import planner.NaivePlanner;
import strategy.Strategy;
import bidder.BidStrategyTrait;
import bidder.NoGain;
import bidder.NoPain;
import bidder.NoPainAverage;
import estimator.CostEstimatorTrait;
import estimator.Gipsy;
import estimator.NoFuture;

/**
 * A very simple auction agent that assigns all tasks to its first vehicle and handles them sequentially.
 */
@SuppressWarnings("unused")
public class AuctionTemplate implements AuctionBehavior {

	enum Bidder {
		NoGain, NoPain, NoPainAverage
	}

	enum Estimator {
		Gipsy, NoFuture
	}

	enum Planner {
		General, Naive
	}

	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private Random random;
	private Vehicle vehicle;
	private City currentCity;

	// Strategies
	private Bidder bidder;
	private Estimator estimator;
	private Planner planner;
	private Strategy strategy;

	// Temporary ε value
	private static final double EPSILON = 4;

	@Override
	public void setup(Topology topology, TaskDistribution distribution, Agent agent) {

		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
		this.vehicle = agent.vehicles().get(0);
		this.currentCity = vehicle.homeCity();

		long seed = -9019554669489983951L * currentCity.hashCode() * agent.id();
		this.random = new Random(seed);

		bidder = Bidder.valueOf(agent.readProperty("bidder", String.class, "NoGain"));
		estimator = Estimator.valueOf(agent.readProperty("estimator", String.class, "NoFuture"));

		BidStrategyTrait bidStrategy;
		CostEstimatorTrait estimatorStrategy;

		switch (bidder) {
		case NoGain:
			bidStrategy = new NoGain();
			break;
		case NoPain:
			bidStrategy = new NoPain(EPSILON);
			break;
		case NoPainAverage:
			bidStrategy = new NoPainAverage();
			break;
		default:
			throw new IllegalArgumentException("Should not happend");
		}

		switch (estimator) {
		case Gipsy:
			estimatorStrategy = new Gipsy();
			break;
		case NoFuture:
			estimatorStrategy = new NoFuture();
			break;
		default:
			throw new IllegalArgumentException("Should not happend");
		}

		strategy = new Strategy(new NaivePlanner(agent.vehicles()), estimatorStrategy, bidStrategy);

		System.out.println("Estimator :" + estimator + ", BidStrategy : " + bidder);

	}

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
		if (winner == agent.id()) {
			currentCity = previous.deliveryCity;
		}
	}

	@Override
	public Long askPrice(Task task) {

		return strategy.bid(task);
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {

		// System.out.println("Agent " + agent.id() + " has tasks " + tasks);

		Plan planVehicle1 = naivePlan(vehicle, tasks);

		List<Plan> plans = new ArrayList<Plan>();
		plans.add(planVehicle1);
		while (plans.size() < vehicles.size())
			plans.add(Plan.EMPTY);

		return plans;
	}

	private Long naiveBid(Task task) {
		if (vehicle.capacity() < task.weight)
			return null;

		long distanceTask = task.pickupCity.distanceUnitsTo(task.deliveryCity);
		long distanceSum = distanceTask + currentCity.distanceUnitsTo(task.pickupCity);
		double marginalCost = Measures.unitsToKM(distanceSum * vehicle.costPerKm());

		double ratio = 1.0 + (random.nextDouble() * 0.05 * task.id);
		double bid = ratio * marginalCost;

		return (long) Math.round(bid);
	}

	private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);

		for (Task task : tasks) {
			// move: current city => pickup location
			for (City city : current.pathTo(task.pickupCity))
				plan.appendMove(city);

			plan.appendPickup(task);

			// move: pickup location => delivery location
			for (City city : task.path())
				plan.appendMove(city);

			plan.appendDelivery(task);

			// set current city
			current = task.deliveryCity;
		}
		return plan;
	}
}
