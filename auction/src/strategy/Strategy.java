package strategy;

import logist.task.Task;
import planner.GeneralPlan;
import planner.PlannerTrait;
import bidder.BidStrategyTrait;
import estimator.CostEstimatorTrait;

public class Strategy {
	private PlannerTrait planner;
	private final CostEstimatorTrait estimator;
	private final BidStrategyTrait bidder;
	private Task currentTask;
	private int bidCount = 0;
	public final String name;
	private Long totalReward = 0l;

	public Strategy(String name, PlannerTrait planner, CostEstimatorTrait estimator, BidStrategyTrait bidder) {
		this.name = name;
		this.planner = planner;
		this.estimator = estimator;
		this.bidder = bidder;
	}

	public Long bid(Task task) {
		++bidCount;
		currentTask = task;
		Long bid = bidder.bid(estimator.computeMC(planner, currentTask));
		System.out.println(name + " " + bidCount + " " + bid);
		return bid;
	}

	public void validateBid(int winner, int agentID, Long[] lastOffers) {
		if (winner == agentID) {
			planner = planner.extendPlan(currentTask);
			totalReward += lastOffers[winner];
		}

		bidder.addBids(lastOffers);
		bidder.addWinnder(winner);
	}

	public GeneralPlan generatePlans() {
		System.out.println("Generating plan for " + name);

		GeneralPlan plan = planner.generatePlans();
		double totalCost = plan.computeCost();

		System.out.println(name + " total cost   = " + totalCost);
		System.out.println(name + " total reward = " + totalReward);

		if (totalCost > totalReward) {
			System.err.println(name + " LOST MONEY!!!");
		}

		return plan;
	}
}
