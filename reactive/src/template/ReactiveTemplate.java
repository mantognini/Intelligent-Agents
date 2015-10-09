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

	// Potential table; access it through get/setPotential
	private HashMap<State, Double> potentials = new HashMap<State, Double>();

	// Setup transition table for our Reactive Agent
	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the gamma factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double gamma = agent.readProperty("gamma", Double.class, 0.95);

		Vehicle vehicle = agent.vehicles().get(0);

		final int N = topology.size(); // # of cities

		boolean improvement = false;
		do {

			improvement = false;

			// For all states
			for (City city : topology) {
				for (int task = 0; task <= N; ++task) {

					// Skip invalid state
					if (task == city.id)
						continue;

					// Current state
					State state = new State(city, task);

					double bestQ = 0.0;

					// From the current city we can go to any neighbors or, if any, the destination of the task
					// Using `Set` to avoid duplicates with task destination
					Set<City> dests = new HashSet<Topology.City>();
					dests.addAll(state.city.neighbors());
					if (task < N) {
						dests.add(getCity(task, topology));
					}

					// Compute potential for all actions (that is, for all legal moves), and keep only the best
					for (City action : dests) {
						double q = reward(state, action, vehicle, td, topology);

						// For all possible next state' (prime)
						City cityP = action;
						for (int taskP = 0; taskP <= N; ++taskP) {
							State stateP = new State(cityP, taskP);

							q += gamma * transitionProbability(state, action, stateP, td, topology)
									* getPotential(stateP);
						}

						bestQ = Math.max(q, bestQ);
					}

					// Update potential if change
					Double currentPotential = getPotential(state);
					if (!currentPotential.equals(bestQ)) {
						setPotential(state, bestQ);
						improvement = true;
					}
				}
			}

		} while (improvement);
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		return null;
	}

	// Compute the probability to be in `stateP` after taking `action` from `state`
	private double transitionProbability(State state, City action, State stateP, TaskDistribution td, Topology topology) {
		final int N = topology.size();

		if (state.city.id == action.id || state.city.id == state.task || state.city.id == stateP.city.id
				|| stateP.city.id != action.id) {
			return 0.0;
		} else if (stateP.task < N) {
			return td.probability(stateP.city, getCity(stateP.task, topology));
		} else {
			// stateP.task >= N implies there's no task available in the city
			// So we have p = 1 - sum(p(stateP.city, dest))
			// Which is nicely handled by `TaskDistribution.probability`.
			return td.probability(stateP.city, null);
		}
	}

	// Get the city corresponding to the destination of the given task
	private City getCity(int task, Topology topology) {
		assert task < topology.size();
		return topology.cities().get(task);
	}

	// Compute reward for the given state-action pair
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

	private Double getPotential(State state) {
		// By default the table is empty but by default we assume a value of 0.0
		return potentials.getOrDefault(state, 0.0);
	}

	private Double setPotential(State state, double potential) {
		return potentials.put(state, potential);
	}

}
