package utils;

import java.util.List;
import java.util.Random;
import java.util.Set;

import logist.simulation.Vehicle;
import logist.task.Task;

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
		for (int i = list.size() - 1; i > (list.size() - 1 - depth); i--) {
			sum += (double) list.get(i);
		}
		return sum / ((double) depth);
	}

	private Utils() {
		// Disallow instantiation
	}

}
