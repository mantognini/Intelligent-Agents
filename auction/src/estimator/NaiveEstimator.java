package estimator;

import java.util.Random;

import logist.task.Task;
import planner.PlannerTrait;

public class NaiveEstimator extends NoFuture {

	private Random random;

	public NaiveEstimator() {
		random = new Random(123458670);
	}

	@Override
	public double computeMC(PlannerTrait planner, Task task) {
		double marginalCost = super.computeMC(planner, task);
		double ratio = 1.0 + (random.nextDouble() * 0.05 * task.id);

		return ratio * marginalCost;
	}
}
