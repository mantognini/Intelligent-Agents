package estimator;

import planner.PlannerTrait;

/**
 * Simple cost estimator; no lookahead in the future
 */
public final class NoFuture extends CostEstimatorTrait {

	@Override
	public double estimateCost(PlannerTrait planner) {
		return 0; // TODO
	}

}
