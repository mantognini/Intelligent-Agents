package estimator;

import logist.task.Task;
import planner.PlannerTrait;

/**
 * Simple cost estimator; no lookahead in the future
 */
public class NoFuture extends CostEstimatorTrait {

	@Override
	public Result computeMC(PlannerTrait planner, Task task, long timeout) {
		double currentCost = planner.generatePlans(timeout / 2).computeCost();

		PlannerTrait extendedPlan = planner.extendPlan(task);
		double costWithExtraTask = extendedPlan.generatePlans(timeout / 2).computeCost();

		double mc = Math.max(0, costWithExtraTask - currentCost);

		return new Result(mc, extendedPlan);
	}

}
