package strategy;

import logist.agent.Agent;
import logist.task.TaskDistribution;
import planner.NaivePlanner;
import planner.SLSPlanner;
import bidder.NoGain;
import bidder.NoPainMinOfBest;
import estimator.Gipsy;
import estimator.NaiveEstimator;
import estimator.NoFuture;

public class StrategyFactory {

	public static Strategy naive(Agent agent) {
		return new Strategy("Naive", new NaivePlanner(agent.vehicles()), new NaiveEstimator(), new NoGain(agent.id()));
	}

	public static Strategy simple(Agent agent) {
		return new Strategy("Simple", new SLSPlanner(agent.vehicles(), SLSPlanner.NORMAL_SETTIGNS,
				SLSPlanner.OPTIMAL_SETTINGS), new NoFuture(), new NoGain(agent.id()));
	}

	public static Strategy safeGambler(Agent agent) {
		double marginRatio = 0.5;
		int depth = 5;

		return new Strategy("Safe Gambler", new SLSPlanner(agent.vehicles(), SLSPlanner.NORMAL_SETTIGNS,
				SLSPlanner.OPTIMAL_SETTINGS), new NoFuture(), new NoPainMinOfBest(agent.id(), marginRatio, depth));
	}

	public static Strategy gipsy(Agent agent, TaskDistribution distribution) {
		int minTasks = 5;
		int nbPredictions = 10;
		double riskTolerance = 0.7;

		double marginRatio = 0.5;
		int depth = 5;

		return new Strategy("Gipsy", new SLSPlanner(agent.vehicles(), SLSPlanner.FAST_SETTIGNS,

		SLSPlanner.OPTIMAL_SETTINGS), new Gipsy(minTasks, nbPredictions, riskTolerance, distribution),
				new NoPainMinOfBest(agent.id(), marginRatio, depth));
	}

	private StrategyFactory() {
		// Disallow instantiation
	}
}
