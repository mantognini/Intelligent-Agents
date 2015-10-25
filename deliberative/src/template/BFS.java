package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import logist.plan.Action;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.TaskSet;
import logist.topology.Topology.City;

public class BFS {

	private Vehicle vehicle;
	private City initialCity;
	private TaskSet tasks;
	private HashMap<Plan, Double> plans = new HashMap<Plan, Double>();

	public BFS(Vehicle vehicle, TaskSet tasks) {
		super();
		this.vehicle = vehicle;
		this.initialCity = vehicle.getCurrentCity();
		this.tasks = tasks;

	}

	// TODO : Consider the case when bfs have to perform BFS from a state different than the root (actually defined
	// initial state without carried tasks).
	/**
	 * 
	 * @return Best Plan according to BFS algorithm
	 */

	public Plan build() {
		State initial = new State(initialCity, null, tasks, vehicle.capacity(), vehicle.costPerKm());
		perform(initial, new ArrayList<State.Action>());
		return computeBestPlan();
	}

	/**
	 * Performing BFS by recursion, according to all possible legal actions that an agent can perform in a current
	 * state.
	 * 
	 * @param current
	 *            : Current state the algorithm is
	 * @param actions
	 *            : List of performed actions.
	 */
	private void perform(State current, List<State.Action> actions) {

		if (!current.isFinal()) {
			for (State.Action action : current.getLegalActions()) {
				ArrayList<State.Action> nextAction = new ArrayList<State.Action>(actions);
				nextAction.add(action);
				perform(current.nextState(action), nextAction);
			}
		} else {

			Plan plan = new Plan(initialCity, convertToAction(actions));
			plans.put(plan, computeCost(actions));
		}

	}

	// TODO : Verify cost is well computed

	/**
	 * Compute the total cost given a list of performed action.
	 * 
	 * @param actions
	 *            : Set of performed actions
	 * @return Total cost of a Plan
	 */
	private double computeCost(List<State.Action> actions) {
		double totalCost = 0.0;
		for (State.Action action : actions) {
			totalCost += action.cost();
		}
		return totalCost;
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

	/**
	 * 
	 * @return Plan minimizing the cost
	 */
	public Plan computeBestPlan() {
		Plan bestPlan = null;
		double bestReward = Double.MAX_VALUE;
		for (Entry<Plan, Double> entry : plans.entrySet()) {
			if (entry.getValue() < bestReward) {
				bestReward = entry.getValue();
				bestPlan = entry.getKey();
			}
		}
		return bestPlan;
	}
}
