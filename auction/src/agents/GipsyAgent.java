package agents;

import logist.agent.Agent;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import strategy.Strategy;
import strategy.StrategyFactory;

public class GipsyAgent extends MetaAgent {

	@Override
	public void setup(Topology topology, TaskDistribution distribution, Agent agent) {
		// TODO find best parameters
		Strategy gipsy = StrategyFactory.gipsy(agent.vehicles(), distribution);
		init(agent, gipsy);
	}

}
