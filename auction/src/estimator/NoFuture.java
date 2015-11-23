package estimator;

import logist.task.Task;
import planner.PlannerTrait;

/**
 * Simple cost estimator; no lookahead in the future
 */
public class NoFuture extends CostEstimatorTrait {

	@Override
	public double computeMC(PlannerTrait planner, Task task) {
		double currentCost = planner.generatePlans().computeCost();

		PlannerTrait extendedPlan = planner.extendPlan(task);
		double costWithExtraTask = extendedPlan.generatePlans().computeCost();

		return Math.abs(costWithExtraTask - currentCost);
	}

}
