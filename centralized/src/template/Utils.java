package template;

import java.util.List;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;

public final class Utils {

	/**
	 * Find the heaviest task
	 */
	static int getHeaviestWeight(TaskSet tasks) {
		int heaviest = 0;
		for (Task t : tasks) {
			if (t.weight > heaviest)
				heaviest = t.weight;
		}
		return heaviest;
	}

	/**
	 * Find the vehicle with the biggest capacity
	 */
	static Vehicle getBiggestVehicle(List<Vehicle> vehicles) {
		Vehicle biggest = vehicles.get(0);
		for (Vehicle v : vehicles) {
			if (v.capacity() > biggest.capacity())
				biggest = v;
		}
		return biggest;
	}

	/**
	 * Find and return (one of) the best given plans
	 */
	public static GeneralPlan selectBest(List<GeneralPlan> plans) {
		ensure(plans.size() > 0, "selectBest needs at least one plan");

		GeneralPlan bestPlan = plans.get(0);
		double bestCost = bestPlan.computeOverallCost();
		for (GeneralPlan plan : plans) {
			double cost = plan.computeOverallCost();
			if (cost < bestCost) {
				bestPlan = plan;
				bestCost = cost;
			}
		}

		return bestPlan;
	}

	static void ensure(boolean b, String rule) {
		if (!b) {
			throw new RuntimeException("rule <" + rule + "> was violated");
		}
	}
}
