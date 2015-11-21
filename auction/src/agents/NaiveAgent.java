package agents;

import logist.agent.Agent;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import strategy.Strategy;
import strategy.StrategyFactory;

public class NaiveAgent extends MetaAgent {

	@Override
	public void setup(Topology topology, TaskDistribution distribution, Agent agent) {
		Strategy naive = StrategyFactory.naive(agent.vehicles());
		init("Naive", agent, naive);
	}

}
