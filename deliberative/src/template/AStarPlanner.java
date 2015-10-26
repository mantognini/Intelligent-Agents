package template;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.TaskSet;
import template.State.Action;

/**
 * Implement the A* algorithm with the following heuristic:
 * 
 * 
 */
public class AStarPlanner {

	// Set of nodes already evaluated (with their estimated cost)
	private final Map<State, Double> closedSet = new HashMap<State, Double>();

	// Set of nodes to be visited, with their estimated cost, sorted by priority (cost).
	private final PriorityQueue<PartialPlan> queue = new PriorityQueue<PartialPlan>();

	// Initial state.
	private final State start;

	private final Heuristic algorithm;

	/* see computeHeuristic for details */
	public enum Heuristic {
		DELIVERY, OPTIMISTIC, CONSTANT
	};

	public AStarPlanner(Vehicle vehicle, TaskSet tasks, Heuristic algorithm) {
		this.algorithm = algorithm;

		// Initialize algorithm from the start node
		start = State.createInitialState(vehicle, tasks);
		List<Action> noAction = new LinkedList<Action>();
		double knownCost = 0.0;
		double heuristic = computeHeuristic(start);
		PartialPlan initialPlan = new PartialPlan(noAction, start, heuristic, knownCost);

		queue.add(initialPlan);
	}

	public Plan build() {

		do {
			PartialPlan node = dequeue();

			if (node.lastState.isFinal())
				return buildPlan(node);

			// TODO do we need to check if a higher cost was previously found for this state???
			// if (!closedSet.contains(node.lastState)) {
			Double previousCost = closedSet.getOrDefault(node.lastState, Double.POSITIVE_INFINITY);
			Double currentCost = node.getCost();
			if (currentCost < previousCost) {
				closedSet.put(node.lastState, currentCost);

				// Augment the queue of partial plans of interest
				for (Action action : node.lastState.getLegalActions()) {
					enqueue(node, action);
				}
			}

		} while (true);
	}

	private Plan buildPlan(PartialPlan node) {
		// Convert the actions and build the optimal plan
		List<logist.plan.Action> actions = new LinkedList<logist.plan.Action>();
		for (Action action : node.actions) {
			actions.add(action.getLogistAction());
		}

		Plan plan = new Plan(start.currentCity, actions);

		// Debug: print the plan
		// System.out.println("Plan for A*: " + plan);

		return plan;
	}

	private double computeHeuristic(State state) {
		switch (algorithm) {
		case CONSTANT:
			// Simplest heuristic
			// -> VERY fast, but sub-optimal
			return 0;

		case OPTIMISTIC:
			// Naive heuristic
			// -> MUCH slower but much more cost-efficient
			return -state.availableTasks.rewardSum() - state.deliveries.rewardSum();

		case DELIVERY:
			// Slightly less naive heuristic: use only what's on the lorry to predict cost
			// -> RATHER fast, a bit sub-optimal
			return -state.deliveries.rewardSum();

		default:
			throw new AssertionError("Should not happen.");
		}
	}

	private void enqueue(PartialPlan node, Action action) {
		// Augment the partial plan with the given action and compute the new cost
		List<Action> actions = new LinkedList<Action>(node.actions);
		actions.add(action);

		State lastState = action.apply();

		double knownCost = node.knownCost + action.cost();
		double heuristic = computeHeuristic(lastState);

		PartialPlan nextNode = new PartialPlan(actions, lastState, heuristic, knownCost);

		queue.add(nextNode);
	}

	private PartialPlan dequeue() {
		return queue.remove();
	}

}
