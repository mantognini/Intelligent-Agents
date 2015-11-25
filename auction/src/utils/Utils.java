package utils;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import logist.simulation.Vehicle;
import logist.task.Task;
import planner.GeneralPlan;

public final class Utils {

	public final static Random random = new Random();

	public static int uniform(int min, int max) {
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

	public static long toLong(double d) {
		return (long) Math.ceil(d);
	}

	/**
	 * Find the heaviest task
	 */
	public static int getHeaviestWeight(Set<Task> tasks) {
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
	public static Vehicle getBiggestVehicle(List<Vehicle> vehicles) {
		Vehicle biggest = vehicles.get(0);
		for (Vehicle v : vehicles) {
			if (v.capacity() > biggest.capacity())
				biggest = v;
		}
		return biggest;
	}

	/**
	 * Assert that `b` holds
	 */
	public static void ensure(boolean b, String rule) {
		if (!b) {
			throw new RuntimeException("rule <" + rule + "> was violated");
		}
	}

	public static double movingAverage(List<Long> list, int depth) {
		if (list.size() == 0) {
			return 0;
		}
		double sum = 0.0;
		for (int i = list.size() - 1; i > (list.size() - 1 - Math.min(depth, list.size() - 1)); i--) {
			sum += (double) list.get(i);
		}
		return sum / ((double) depth);
	}

	/**
	 * Find and return (one of) the best given plans
	 */
	public static GeneralPlan selectBest(GeneralPlan currentBest, List<GeneralPlan> plans) {
		ensure(plans.size() > 0, "selectBest needs at least one plan");

		GeneralPlan bestPlan = currentBest != null ? currentBest : plans.get(0);
		double bestCost = bestPlan.computeCost();
		for (GeneralPlan plan : plans) {
			double cost = plan.computeCost();
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
		double bestCost = bestPlan.computeCost();

		for (int i = 0; i < plans.length; i++) {
			double cost = plans[i].computeCost();
			if (cost <= bestCost) {
				bestPlan = plans[i];
				bestCost = cost;
			}
		}

		return bestPlan;
	}

	public static GeneralPlan selectBest(GeneralPlan a, GeneralPlan b) {
		return a.computeCost() <= b.computeCost() ? a : b;
	}

	public static Long min(List<Long> longs, int depth) {
		ensure(longs.size() > 0, "Minimum of a empty list souldn't be called");
		Long min = Long.MAX_VALUE;
		for (int i = longs.size() - 1; i >= (longs.size() - 1 - Math.min(depth, longs.size() - 1)); i--) {
			min = Math.min(min, longs.get(i));
		}

		return min;
	}

	public static int bestAgent(List<Integer> winners, HashMap<Integer, List<Long>> bids, int depth, int agentID) {
		int best = 0;
		int nbWinsBest = Integer.MIN_VALUE;

		for (int id : bids.keySet()) {
			if (id == agentID) {
				continue;
			}
			int nbWins = 0;
			for (int winner : winners) {
				if (id == winner) {
					nbWins++;
				}
			}

			if (nbWins > nbWinsBest) {
				best = id;
				nbWinsBest = nbWins;
			} else if (nbWins == nbWinsBest) {
				Long minBest = min(bids.get(best), depth);
				Long minCurrent = min(bids.get(id), depth);
				if (minBest > minCurrent) {
					best = id;
				}
			}
		}

		return best;
	}

	private Utils() {
		// Disallow instantiation
	}

}
