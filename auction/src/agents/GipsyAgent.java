package agents;

import java.util.List;

import logist.agent.Agent;
import logist.behavior.AuctionBehavior;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import strategy.Strategy;
import strategy.StrategyFactory;

public class GipsyAgent implements AuctionBehavior {

	private Agent agent = null;
	private Strategy gipsy = null;

	@Override
	public void setup(Topology topology, TaskDistribution distribution, Agent agent) {
		this.agent = agent;
		this.gipsy = StrategyFactory.gipsy(agent.vehicles(), 8, 10, distribution);
	}

	@Override
	public Long askPrice(Task task) {
		return gipsy.bid(task);
	}

	@Override
	public void auctionResult(Task lastTask, int lastWinner, Long[] lastOffers) {
		gipsy.validateBid(lastWinner == agent.id());
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		return gipsy.generatePlans().convertToLogistPlans(tasks);
	}

}
