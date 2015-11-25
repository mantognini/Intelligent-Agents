package strategy;

import logist.task.Task;
import planner.GeneralPlan;
import planner.PlannerTrait;
import bidder.BidStrategyTrait;
import estimator.CostEstimatorTrait;

public class Strategy {
	private final CostEstimatorTrait estimator;
	private final BidStrategyTrait bidder;

	private PlannerTrait planner;
	private Task currentTask;
	private int bidCount = 0;
	private Long totalReward = 0l;

	public final String name;

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

	public void validateBid(int winner, Long[] lastOffers) {
		if (winner == bidder.agentID) {
			planner = planner.extendPlan(currentTask);
			totalReward += lastOffers[winner];
		}

		bidder.addBids(lastOffers, winner);
		bidder.addWinner(winner);
	}

	public GeneralPlan generatePlans() {
		System.out.println("Generating plan for " + name);

		GeneralPlan plan = planner.generateFinalPlans();
		double totalCost = plan.computeCost();

		System.out.println(name + " total cost   = " + totalCost);
		System.out.println(name + " total reward = " + totalReward);

		if (totalCost > totalReward) {
			System.err.println("\t" + name + " LOST MONEY!!! [" + (totalReward - totalCost) + "]");
		} else {
			System.out.println("\t" + name + " PROFIT is " + (totalReward - totalCost));
		}

		return plan;
	}
}
