package strategy;

import logist.agent.Agent;
import logist.task.TaskDistribution;
import planner.NaivePlanner;
import planner.SLSPlanner;
import bidder.NoGain;
import bidder.NoPainMinOfBest;
import estimator.Gipsy;
import estimator.NoFuture;

public class StrategyFactory {

	public static Strategy naive(Agent agent) {
		return new Strategy("Naive", new NaivePlanner(agent.vehicles()), new NoFuture(), new NoGain(agent.id()));
	}

	public static Strategy simple(Agent agent) {
		return new Strategy("Simple", new SLSPlanner(agent.vehicles(), SLSPlanner.NORMAL_SETTIGNS,
				SLSPlanner.OPTIMAL_SETTINGS), new NoFuture(), new NoGain(agent.id()));
	}

	public static Strategy gipsy(Agent agent, TaskDistribution distribution) {
		int minTasks = 10;
		int nbPredictions = 10;

		return new Strategy("Gipsy", new SLSPlanner(agent.vehicles(), SLSPlanner.FAST_SETTIGNS,
				SLSPlanner.OPTIMAL_SETTINGS), new Gipsy(minTasks, nbPredictions, distribution), new NoPainMinOfBest(
				agent.id(), 0.5, 5));

	}

	private StrategyFactory() {
		// Disallow instantiation
	}
}
