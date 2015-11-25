package agents;

import logist.agent.Agent;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import strategy.Strategy;
import strategy.StrategyFactory;

public class SimpleAgent extends MetaAgent {

	@Override
	public void setup(Topology topology, TaskDistribution distribution, Agent agent) {
		Strategy simple = StrategyFactory.simple(agent);
		init(agent, simple);
	}
}
