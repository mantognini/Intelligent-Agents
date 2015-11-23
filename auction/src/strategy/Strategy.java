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

	public void validateBid(Boolean won, Long[] lastOffers) {
		if (won) {
			planner = planner.extendPlan(currentTask);
		}

		bidder.addBids(lastOffers);
	}

	public GeneralPlan generatePlans() {
		System.out.println("Generating plan for " + name);
		return planner.generatePlans();
	}
}
