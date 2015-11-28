package strategy;

import logist.task.Task;
import planner.GeneralPlan;
import planner.PlannerTrait;
import bidder.BidStrategyTrait;
import estimator.CostEstimatorTrait;
import estimator.CostEstimatorTrait.Result;

public class Strategy {
	private final CostEstimatorTrait estimator;
	private final BidStrategyTrait bidder;

	private PlannerTrait planner;
	private PlannerTrait nextPlanner = null;
	private Task currentTask;
	private int bidCount = 0;
	private Long totalReward = 0l;
	private int winCount = 0;

	public final String name;

	public Strategy(String name, PlannerTrait planner, CostEstimatorTrait estimator, BidStrategyTrait bidder) {
		this.name = name;
		this.planner = planner;
		this.estimator = estimator;
		this.bidder = bidder;
	}

	public Long bid(Task task) {
		currentTask = task;
		System.out.println(name + " is bidding...");

		Result result = estimator.computeMC(planner, currentTask);
		nextPlanner = result.planner;
		Long bid = bidder.bid(result.mc);

		System.out.println(name + " has bid " + bid);
		if (result.planner != null) {
			// everything should be cached so this won't harm performance
			System.out.println("next planner has cost: " + result.planner.generatePlans().computeCost());
		}
		return bid;
	}

	public void validateBid(int winner, Long[] lastOffers) {
		final boolean won = winner == bidder.agentID;
		if (won) {
			planner = nextPlanner != null ? nextPlanner : planner.extendPlan(currentTask);
			nextPlanner = null;

			totalReward += lastOffers[winner];
			++winCount;
		}

		++bidCount;

		bidder.addBids(lastOffers, winner);

		System.out.print(name + " bid " + lastOffers[bidder.agentID] + " for n° " + bidCount + " and ");
		System.out.println((won ? "won" : "lost") + " [total = " + winCount + "]");
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
