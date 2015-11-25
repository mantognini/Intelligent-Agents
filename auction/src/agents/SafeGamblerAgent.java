package agents;

import logist.agent.Agent;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import strategy.Strategy;
import strategy.StrategyFactory;

public class SafeGamblerAgent extends MetaAgent {

	@Override
	public void setup(Topology topology, TaskDistribution distribution, Agent agent) {
		Strategy safe = StrategyFactory.safeGambler(agent);
		init(agent, safe);
	}
}
