package estimator;

import planner.PlannerTrait;

/**
 * Base class for multiple plan estimator
 */
public abstract class CostEstimatorTrait {
	public abstract double estimateCost(PlannerTrait planner);
}
