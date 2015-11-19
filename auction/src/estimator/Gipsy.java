package estimator;

import logist.task.DefaultTaskDistribution;
import logist.task.Task;
import logist.task.TaskDistribution;
import planner.PlannerTrait;

public class Gipsy extends NoFuture {

	private final int minTasks;
	private final int nbPredictions;
	private final DefaultTaskDistribution distribution;

	public Gipsy(int minTasks, int nbPredictions, TaskDistribution distribution) {
		this.minTasks = minTasks;
		this.nbPredictions = nbPredictions;
		this.distribution = (DefaultTaskDistribution) distribution;
	}

	@Override
	public double computeMC(PlannerTrait planner, Task task) {
		if (planner.tasks.size() >= minTasks)
			return super.computeMC(planner, task); // use NoFuture

		// Compute a few estimation
		double worsePrediction = Double.NEGATIVE_INFINITY;
		double sum = 0;
		for (int i = 0; i < nbPredictions; ++i) {
			PlannerTrait vision = planner;

			// Extend the current planner with random tasks
			while (vision.tasks.size() < minTasks) {
				vision = vision.extendPlan(createTask());
			}

			double prediction = super.computeMC(vision, task);
			worsePrediction = Math.max(worsePrediction, prediction);

			sum += prediction;
		}

		// TODO remove me after plotting data
		double avg = sum / nbPredictions;
		System.out.println("Gipsy " + planner.tasks.size() + " " + minTasks + " " + nbPredictions + " " + avg);
		// import output in Excel

		return worsePrediction;
	}

	/**
	 * Generate a random task
	 */
	private Task createTask() {
		return distribution.createTask();
	}

}
