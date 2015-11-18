package estimator;

import logist.task.Task;
import planner.PlannerTrait;

/**
 * Base class for multiple plan estimator
 */
public abstract class CostEstimatorTrait {
	public abstract double computeMC(PlannerTrait planner, Task task);
}
