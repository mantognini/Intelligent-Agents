package agents;

import java.util.List;

import logist.agent.Agent;
import logist.behavior.AuctionBehavior;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import strategy.Strategy;

/**
 * When generating a tournament, remove this *abstract* agent from the list in <tournament>/agents.xml as logist is not
 * capable of detecting abstract classes that implement AuctionBehavior.
 */
public abstract class MetaAgent implements AuctionBehavior {

	private Strategy strategy = null;

	@Override
	public Long askPrice(Task task) {
		return strategy.bid(task);
	}

	@Override
	public void auctionResult(Task lastTask, int lastWinner, Long[] lastOffers) {
		strategy.validateBid(lastWinner, lastOffers);
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		return strategy.generatePlans().convertToLogistPlans(tasks);
	}

	// To be called from the setup method in subclasses
	protected void init(Agent agent, Strategy strategy) {
		this.strategy = strategy;

		System.out.print(strategy.name + " agent has vehicles ");
		for (Vehicle v : agent.vehicles())
			System.out.print(v.name() + " ");
		System.out.println();
	}

}
