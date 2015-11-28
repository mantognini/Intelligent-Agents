package estimator;

import logist.task.Task;
import planner.PlannerTrait;

/**
 * Base class for multiple plan estimator
 */
public abstract class CostEstimatorTrait {

	public static class Result {
		public final double mc;
		public final PlannerTrait planner; // might be null

		public Result(double mc, PlannerTrait planner) {
			this.mc = mc;
			this.planner = planner;
		}
	}

	/**
	 * Return the marginal cost and the corresponding planner, if it exists
	 */
	public abstract Result computeMC(PlannerTrait planner, Task task);
}
