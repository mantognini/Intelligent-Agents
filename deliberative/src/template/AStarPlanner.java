package template;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.TaskSet;
import template.State.Action;

/**
 * Implement the A* algorithm with the following heuristic:
 * 
 * TODO define heuristic
 */
public class AStarPlanner {

	// Set of nodes already evaluated.
	private final Set<State> closedSet = new HashSet<State>();

	// Set of nodes to be visited, with their estimated cost, sorted by priority (cost).
	private final PriorityQueue<PartialPlan> queue = new PriorityQueue<PartialPlan>();

	// Initial state.
	private final State start;

	public AStarPlanner(Vehicle vehicle, TaskSet tasks) {
		// Initialize algorithm from the start node
		start = State.createInitialState(vehicle, tasks);
		List<Action> noAction = new LinkedList<Action>();
		double knownCost = 0.0;
		double heuristic = computeHeuristic(start);
		PartialPlan initialPlan = new PartialPlan(noAction, start, knownCost, heuristic);

		queue.add(initialPlan);
	}

	public Plan build() {

		do {
			PartialPlan node = dequeue();

			if (node.lastState.isFinal())
				return buildPlan(node);

			// TODO do we need to check if a higher cost was previously found for this state???
			if (!closedSet.contains(node.lastState)) {
				closedSet.add(node.lastState);

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
		System.out.println(plan);

		return plan;
	}

	private double computeHeuristic(State state) {
		// TODO
		double h = 0.0;
		return h;
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

	private static class PartialPlan implements Comparable<PartialPlan> {
		public final List<Action> actions; // Partial plan
		public final State lastState; // Resulting state from applying the plan
		public final double heuristicCost; // Estimated cost of applying the plan until goal is satisfied
		public final double knownCost; // Cost of applying the plan from the start to now

		public PartialPlan(List<Action> actions, State lastState, double heuristicCost, double knownCost) {
			this.actions = actions;
			this.lastState = lastState;
			this.heuristicCost = heuristicCost;
			this.knownCost = knownCost;
		}

		/**
		 * Returns a negative integer, zero, or a positive integer as this object is less than, equal to, or greater
		 * than the specified object.
		 */
		@Override
		public int compareTo(PartialPlan o) {
			if (getCost() < o.getCost())
				return -1;
			else if (getCost() > o.getCost())
				return 1;
			else
				return 0;
		}

		private double getCost() {
			return heuristicCost + knownCost;
		}
	}

}
