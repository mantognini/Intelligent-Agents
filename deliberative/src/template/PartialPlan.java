package template;

import java.util.List;

import template.State.Action;

class PartialPlan implements Comparable<PartialPlan> {
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
	 * Constructor that invalidate the comparable. Use with BFS.
	 * 
	 * @param actions
	 * @param lastState
	 */
	public PartialPlan(List<Action> actions, State lastState) {
		super();
		this.actions = actions;
		this.lastState = lastState;
		this.heuristicCost = 0;
		this.knownCost = 0;
	}

	/**
	 * Returns a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
	 * the specified object.
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

	public double getCost() {
		return heuristicCost + knownCost;
	}
}