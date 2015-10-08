package template;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class ReactiveTemplate implements ReactiveBehavior {

	private HashMap<State, Double> potentials = new HashMap<State, Double>();

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the gamma factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double gamma = agent.readProperty("gamma", Double.class, 0.95);

		Vehicle vehicle = agent.vehicles().get(0);

		final int N = topology.size();

		// TODO : Handle the case where there isn't any task
		boolean improvement = false;
		do {
			improvement = false;
			Double sumOfDiffs = 0.0;
			// System.out.println("------------------------------------");

			// For all states
			for (City city : topology) {
				for (int task = 0; task <= N; ++task) {

					// Skip invalid state
					if (task == city.id)
						continue;

					State state = new State(city, task);

					double bestQ = 0.0;

					// From the current city we can go to any neighbors or, if any, the destination of the task
					// Using set to avoid duplicates with task destination
					Set<City> dests = new HashSet<Topology.City>();
					dests.addAll(state.city.neighbors());
					if (task < N) {
						dests.add(topology.cities().get(task));
					}

					// For all actions
					for (City action : dests) {
						double q = reward(state, action, vehicle, td, topology);

						// For all state' (prime)
						City cityP = action;
						for (int taskP = 0; taskP <= N; ++taskP) {
							State stateP = new State(cityP, taskP);

							q += gamma * transitionProbability(state, action, stateP, td, topology)
									* potentials.getOrDefault(stateP, 0.0);
						}

						bestQ = Math.max(q, bestQ);
					}

					// Update potential if change
					Double currentPotential = potentials.getOrDefault(state, 0.0);
					// System.out.println("current: " + currentPotential + "; bestQ: " + bestQ);
					sumOfDiffs += Math.abs(currentPotential - bestQ);
					if (!currentPotential.equals(bestQ)) {
						potentials.put(state, bestQ);
						improvement = true;
					}
				}
			}

			System.out.println("Sum of diffs: " + sumOfDiffs);

		} while (improvement);
	}

	private double transitionProbability(State state, City action, State stateP, TaskDistribution td, Topology topology) {
		final int N = topology.size();

		if (state.city.id == action.id || state.city.id == state.task || state.city.id == stateP.city.id
				|| stateP.city.id != action.id) {
			return 0.0;
		} else if (stateP.task < N) {
			return td.probability(stateP.city, getCity(stateP.task, topology));
		} else {
			// stateP.task >= N ---> π[1 - p(stateP.city, dest)]
			double p = 1.0;
			for (City dest : topology) {
				p *= 1.0 - td.probability(stateP.city, dest);
			}
			return p;
		}
	}

	private City getCity(int task, Topology topology) {
		assert task < topology.size();
		return topology.cities().get(task);
	}

	private double reward(State state, City action, Vehicle vehicle, TaskDistribution td, Topology topology) {
		final int N = topology.size();

		double win = 0;

		// If we have a task with us, we can get some candy!
		if (state.task < N && action.id == state.task) {
			win = td.reward(state.city, action);
		}

		double lost = state.city.distanceTo(action) * vehicle.costPerKm();

		return win - lost;
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		return null;
	}

}
