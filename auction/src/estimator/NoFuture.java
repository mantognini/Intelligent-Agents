package estimator;

import logist.task.Task;
import planner.PlannerTrait;

/**
 * Simple cost estimator; no lookahead in the future
 */
public class NoFuture extends CostEstimatorTrait {

	@Override
	public Result computeMC(PlannerTrait planner, Task task) {
		double currentCost = planner.generatePlans().computeCost();

		PlannerTrait extendedPlan = planner.extendPlan(task);
		double costWithExtraTask = extendedPlan.generatePlans().computeCost();

		double mc = Math.abs(costWithExtraTask - currentCost);

		return new Result(mc, extendedPlan);
	}

}
