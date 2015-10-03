package template;

import java.util.Random;

import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class ReactiveTemplate implements ReactiveBehavior {

	private Random random;
	private double pPickup;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// TODO is this the gamma factor???
		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class, 0.95);

		this.random = new Random();
		this.pPickup = discount;

		// Number of cities & moves:
		Integer N = topology.size();
		// Number of task's state: N+1 (when task == N it means no task is available)

		// Potential V for each state s = (city, task)
		Double potential[][] = new Double[N][N + 1];

		Boolean progress = true;
		do {
			// Progress is made if `potential` is updated
			progress = false;

			// For all states (city x task)
			for (Integer city = 0; city < N; ++city) {
				for (Integer task = 0; task <= N; ++task) {
					Double bestQForAction = -1.0;

					// For all actions (move): compute best Q
					for (Integer move = 0; move < N; ++move) {
						Double q = reward(city, task, move);

						// for all s'∈ S: γ * T(s,a,s') * V(s')
						Integer cityP = move; // only one possibility for the next state's city
						for (Integer taskP = 0; taskP <= N; ++taskP) {
							q += gamma * probabilityTransition(city, task, move, cityP, taskP)
									* potential[cityP][taskP];
						}

						// NOTE: probabilityTransition will probably depends only on the last two parameters

						bestQForAction = Math.max(bestQForAction, q);
					}

					// Update potential
					if (potential[city][task] != bestQForAction) {
						progress = true;
						potential[city][task] = bestQForAction;
					}
				}
			}
		} while (progress);

		// TODO same `potential` as a field or something
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;

		if (availableTask == null || random.nextDouble() > pPickup) {
			City currentCity = vehicle.getCurrentCity();
			action = new Move(currentCity.randomNeighbor(random));
		} else {
			action = new Pickup(availableTask);
		}

		return action;
	}
}
