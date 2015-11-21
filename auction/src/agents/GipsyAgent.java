package agents;

import logist.agent.Agent;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import strategy.Strategy;
import strategy.StrategyFactory;

public class GipsyAgent extends MetaAgent {

	@Override
	public void setup(Topology topology, TaskDistribution distribution, Agent agent) {
		Strategy gipsy = StrategyFactory.gipsy(agent.vehicles(), 8, 10, distribution);
		init("Gipsy", agent, gipsy);
	}

}
