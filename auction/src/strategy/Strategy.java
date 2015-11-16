package strategy;

import static utils.Utils.ensure;
import logist.task.Task;
import planner.GeneralPlan;
import planner.PlannerTrait;
import bidder.BidStrategyTrait;
import estimator.CostEstimatorTrait;

public class Strategy {
	private PlannerTrait planner;
	private PlannerTrait nextPlanner = null;
	private final CostEstimatorTrait estimator;
	private final BidStrategyTrait bidder;

	public Strategy(PlannerTrait planner, CostEstimatorTrait estimator, BidStrategyTrait bidder) {
		this.planner = planner;
		this.estimator = estimator;
		this.bidder = bidder;
	}

	public Long bid(Task task) {
		ensure(nextPlanner == null, "validate bid should be called between two bids");

		nextPlanner = planner.extendPlan(task);
		double currentCost = estimator.estimateCost(planner);
		double estimatedCost = estimator.estimateCost(nextPlanner);
		long bid = bidder.bid(planner.tasks.size(), currentCost, estimatedCost);

		return bid;
	}

	public void validateBid(Boolean won) {
		ensure(nextPlanner != null, "no bid was made");

		if (won) {
			planner = nextPlanner;
		}

		nextPlanner = null;
	}

	public GeneralPlan generatePlans() {
		return planner.generatePlans();
	}
}
