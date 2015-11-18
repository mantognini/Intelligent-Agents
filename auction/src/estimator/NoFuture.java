package estimator;

import logist.task.Task;
import planner.PlannerTrait;

/**
 * Simple cost estimator; no lookahead in the future
 */
public final class NoFuture extends CostEstimatorTrait {

	@Override
	public double computeMC(PlannerTrait planner, Task task) {
		PlannerTrait extendPlan = planner.extendPlan(task);
		return extendPlan.generatePlans().computeCost();
	}

}
