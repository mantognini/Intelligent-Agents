package template;

/* import table */
import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * An optimal planner for one vehicle.
 */
public class DeliberativeTemplate implements DeliberativeBehavior {

	enum Algorithm {
		BFS, ASTAR, NAIVE
	}

	/* the properties of the agent */
	Agent agent;

	/* the planning class */
	Algorithm algorithm;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		this.agent = agent;

		// initialize the planner
		String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");

		// Throws IllegalArgumentException if algorithm is unknown
		algorithm = Algorithm.valueOf(algorithmName.toUpperCase());
	}

	@Override
	public Plan plan(Vehicle vehicle, TaskSet tasks) {
		// Compute the plan with the selected algorithm.
		Plan plan = null;
		long startTime = System.currentTimeMillis();
		switch (algorithm) {
		case ASTAR:
			plan = aStarPlan(vehicle, tasks);
			break;

		case BFS:
			plan = bfs(vehicle, tasks);
			break;

		case NAIVE:
			plan = naivePlan(vehicle, tasks);
			break;

		default:
			throw new AssertionError("Should not happen.");
		}
		long endTime = System.currentTimeMillis();

		System.out.println("Algorithm: " + algorithm);

		double distance = vehicle.getDistance() + plan.totalDistance();
		System.out.println("Distance to be travelled: " + distance);

		System.out.println("Plan computed in " + (endTime - startTime) / 1000.0 + "s");

		return plan;
	}

	private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);

		for (Task task : tasks) {
			// move: current city => pickup location
			for (City city : current.pathTo(task.pickupCity))
				plan.appendMove(city);

			plan.appendPickup(task);

			// move: pickup location => delivery location
			for (City city : task.path())
				plan.appendMove(city);

			plan.appendDelivery(task);

			// set current city
			current = task.deliveryCity;
		}
		return plan;
	}

	private Plan bfs(Vehicle vehicle, TaskSet tasks) {
		return new BFS(vehicle, tasks).build();
	}

	private Plan aStarPlan(Vehicle vehicle, TaskSet tasks) {
		return new AStarPlanner(vehicle, tasks).build();
	}

	@Override
	public void planCancelled(TaskSet carriedTasks) {
		// Intentionally empty
	}
}
