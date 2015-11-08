package template;

import java.util.List;
import java.util.Random;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;

public final class Utils {

	public final static Random random = new Random();

	static int uniform(int min, int max) {
		ensure(min < max, "uniform requires lowerbound smaller than upperbound");
		ensure(min >= 0, "uniform doesn't work with negative integer"); // it might actually work but we don't care

		int bound = max - min;
		return random.nextInt(bound) + min;
	}

	public static <E> E getRandomElement(List<E> list) {
		ensure(list.size() > 0, "selectRandom needs at least one plan");

		int index = random.nextInt(list.size());
		return list.get(index);
	}

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
	public static GeneralPlan selectBest(GeneralPlan currentBest, List<GeneralPlan> plans) {
		ensure(plans.size() > 0, "selectBest needs at least one plan");

		GeneralPlan bestPlan = currentBest != null ? currentBest : plans.get(0);
		double bestCost = bestPlan.computeOverallCost();
		for (GeneralPlan plan : plans) {
			double cost = plan.computeOverallCost();
			if (cost <= bestCost) {
				bestPlan = plan;
				bestCost = cost;
			}
		}

		return bestPlan;
	}

	public static GeneralPlan selectBest(GeneralPlan currentBest, GeneralPlan[] plans) {
		ensure(plans.length > 0, "selectBest needs at least one plan");

		GeneralPlan bestPlan = currentBest != null ? currentBest : plans[0];
		double bestCost = bestPlan.computeOverallCost();

		for (int i = 0; i < plans.length; i++) {
			double cost = plans[i].computeOverallCost();
			if (cost <= bestCost) {
				bestPlan = plans[i];
				bestCost = cost;
			}
		}

		return bestPlan;
	}

	public static GeneralPlan selectBest(GeneralPlan a, GeneralPlan b) {
		return a.computeOverallCost() < b.computeOverallCost() ? a : b;
	}

	static void ensure(boolean b, String rule) {
		if (!b) {
			throw new RuntimeException("rule <" + rule + "> was violated");
		}
	}
}
