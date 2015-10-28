package template;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import logist.plan.Action;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.TaskSet;
import logist.topology.Topology.City;

public class BFS {

	private City initialCity;
	private State intialState;

	public BFS(Vehicle vehicle, TaskSet tasks) {
		super();
		this.initialCity = vehicle.getCurrentCity();
		this.intialState = State.createInitialState(vehicle, tasks);

	}

	/**
	 * 
	 * @return Best Plan according to BFS algorithm
	 */

	public Plan build() {
		LinkedList<PartialPlan> queue = new LinkedList<PartialPlan>();
		ArrayList<State> alreadyVisitedStates = new ArrayList<State>();
		PartialPlan initialNode = new PartialPlan(new ArrayList<State.Action>(), intialState);
		queue.addLast(initialNode);
		while (!queue.isEmpty()) {
			PartialPlan node = queue.poll();
			State state = node.lastState;
			if (state.isFinal()) {
				System.out.println("# of node visited: " + alreadyVisitedStates.size());
				return new Plan(initialCity, convertToAction(node.actions));
			}
			if (!alreadyVisitedStates.contains(state)) {
				alreadyVisitedStates.add(state);
				for (State.Action action : state.getLegalActions()) {
					State successorState = state.nextState(action);
					ArrayList<State.Action> futureActions = new ArrayList<State.Action>(node.actions);
					futureActions.add(action);
					queue.add(new PartialPlan(futureActions, successorState));

				}
			}
		}

		throw new AssertionError("Should not append");

	}

	/**
	 * Convert a list from State.Action to Action
	 * 
	 * @param actions
	 *            : List of State.Action
	 */
	private List<Action> convertToAction(List<State.Action> actions) {
		ArrayList<Action> converted = new ArrayList<Action>();
		for (State.Action action : actions) {
			converted.add(action.getLogistAction());
		}
		return converted;
	}

}
