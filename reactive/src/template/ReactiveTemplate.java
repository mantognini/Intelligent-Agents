package template;

import java.util.HashMap;
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
	private HashMap<State, Double> values = new HashMap<State, Double>();

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class,
				0.95);

		this.random = new Random();
		this.pPickup = discount;

		Vehicle vehicle = agent.vehicles().get(0);

		// TODO : Handle the case where there isn't any task
		// TODO : Handle the fact that an agent can't mover further thant its
		// neighbors
		boolean improvement = false;
		do {
			improvement = false;
			for (City city : topology) {
				for (City task : topology) {
					State state = new State(city, task);
					// TODO : Define Q
					double bestQ = 0.0;
					for (City move : topology) {
						double q = reward(city, task, move, vehicle, td);
	
						for (City cityP : topology) {
							for (City taskP : topology) {
								q += futureValue(city, task, cityP, taskP, td);
							}
						}
						bestQ = Math.max(q, bestQ);
					}
					if(values.get(state) < bestQ) {
						values.put(state, bestQ);
						improvement = true;
					}
				}
			}
		}while(improvement)
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

	private double reward(City city, City task, City move, Vehicle vehicle,
			TaskDistribution td) {
		if (city.id == task.id || city.id == move.id) {
			return 0.0;
		} else if (task.id != move.id) {
			return city.distanceTo(move) * vehicle.costPerKm();
		} else {
			return td.reward(city, task) - city.distanceTo(task)
					* vehicle.costPerKm();
		}

	}

	private double futureValue(City currentC, City currentT, City futureC,
			City futureT, TaskDistribution td) {
		if ((currentC.id == futureC.id) || (currentT.id == currentC.id)
				|| (currentT.id == futureT.id)) {
			return 0.0;
		} else {
			return td.probability(futureC, futureT);
		}
	}
}
