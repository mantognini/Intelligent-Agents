package strategy;

import java.util.List;

import logist.simulation.Vehicle;
import logist.task.TaskDistribution;
import planner.NaivePlanner;
import bidder.NoGain;
import estimator.Gipsy;
import estimator.NoFuture;

public class StrategyFactory {

	public static Strategy naive(List<Vehicle> vehicles) {
		return new Strategy(new NaivePlanner(vehicles), new NoFuture(), new NoGain());
	}

	public static Strategy gipsy(List<Vehicle> vehicles, int minTasks, int nbPredictions, TaskDistribution distribution) {
		// TODO change NaivePlanner to SLSPlanner or something more intelligent!
		return new Strategy(new NaivePlanner(vehicles), new Gipsy(minTasks, nbPredictions, distribution), new NoGain());
	}

	private StrategyFactory() {
		// Disallow instantiation
	}
}
