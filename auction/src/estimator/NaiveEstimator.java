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
	public Result computeMC(PlannerTrait planner, Task task, long timeout) {
		Result result = super.computeMC(planner, task, timeout);
		double ratio = 1.0 + (random.nextDouble() * 0.05 * task.id);
		double mc = ratio * result.mc;

		return new Result(mc, result.planner);
	}
}
