package strategy;

import java.util.List;

import logist.simulation.Vehicle;
import planner.NaivePlanner;
import bidder.NoGain;
import estimator.NoFuture;

public class StrategyFactory {

	public static Strategy naive(List<Vehicle> vehicles) {
		return new Strategy(new NaivePlanner(vehicles), new NoFuture(), new NoGain());
	}

	private StrategyFactory() {
		// Disallow instantiation
	}
}
