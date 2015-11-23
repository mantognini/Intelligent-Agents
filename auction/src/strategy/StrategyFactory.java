package strategy;

import java.util.List;

import logist.simulation.Vehicle;
import logist.task.TaskDistribution;
import planner.NaivePlanner;
import planner.SLSPlanner;
import bidder.NoGain;
import estimator.Gipsy;
import estimator.NoFuture;

public class StrategyFactory {

	public static Strategy naive(List<Vehicle> vehicles) {
		return new Strategy(new NaivePlanner(vehicles), new NoFuture(), new NoGain());
	}

	public static Strategy gipsy(List<Vehicle> vehicles, int minTasks, int nbPredictions, TaskDistribution distribution) {
		return new Strategy(new SLSPlanner(vehicles), new Gipsy(minTasks, nbPredictions, distribution), new NoGain());
	}

	private StrategyFactory() {
		// Disallow instantiation
	}
}
