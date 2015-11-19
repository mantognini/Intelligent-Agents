package template;

import java.util.List;

import logist.agent.Agent;
import logist.behavior.AuctionBehavior;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
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

	private Agent agent;

	// Parameters
	private Bidder bidder;
	private Estimator estimator;
	private Planner planner;

	// Strategies
	private BidStrategyTrait bidStrategy;
	private CostEstimatorTrait estimatorStrategy;
	private Strategy strategy;

	// Temporary ε value
	private static final double EPSILON = 4;

	@Override
	public void setup(Topology topology, TaskDistribution distribution, Agent agent) {
		this.agent = agent;

		bidder = Bidder.valueOf(agent.readProperty("bidder", String.class, "NoGain"));
		estimator = Estimator.valueOf(agent.readProperty("estimator", String.class, "NoFuture"));

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
		strategy.validateBid(agent.id() == winner);
		bidStrategy.addBids(bids);
	}

	@Override
	public Long askPrice(Task task) {
		return strategy.bid(task);
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		return strategy.generatePlans().convertToLogistPlans(tasks);
	}
}
