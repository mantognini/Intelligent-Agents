package strategy;

import java.util.List;

import logist.simulation.Vehicle;
import logist.task.TaskDistribution;
import planner.NaivePlanner;
import planner.SLSPlanner;
import bidder.NoGain;
import bidder.NoPainMinOfBest;
import estimator.Gipsy;
import estimator.NoFuture;

public class StrategyFactory {

	public static Strategy naive(List<Vehicle> vehicles) {
		return new Strategy("Naive", new NaivePlanner(vehicles), new NoFuture(), new NoGain());
	}

	public static Strategy simple(List<Vehicle> vehicles) {
		return new Strategy("Simple", new SLSPlanner(vehicles), new NoFuture(), new NoGain());
	}

	public static Strategy gipsy(List<Vehicle> vehicles, TaskDistribution distribution) {
		int minTasks = 6;
		int nbPredictions = 10;
		return new Strategy("Gipsy", new SLSPlanner(vehicles), new Gipsy(minTasks, nbPredictions, distribution),
				new NoPainMinOfBest(0.5, 5));
	}

	private StrategyFactory() {
		// Disallow instantiation
	}
}
