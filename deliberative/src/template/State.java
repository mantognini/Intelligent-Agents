package template;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

public class State {

	/**
	 * Create the initial state for the given vehicle and the task list
	 */
	public static State createInitialState(Vehicle vehicle, TaskSet tasks) {
		TaskSet currentTasks = vehicle.getCurrentTasks();
		int remainingCapacity = vehicle.capacity() - currentTasks.weightSum();
		return new State(vehicle.getCurrentCity(), currentTasks, tasks, remainingCapacity, vehicle.costPerKm());
	}

	/**
	 * City currently in
	 */
	public final City currentCity;
	/**
	 * Tasks on board
	 */
	public final TaskSet deliveries;
	/**
	 * Task available on the topology
	 */
	public final TaskSet availableTasks;
	/**
	 * Remaining available capacity on the vehicle
	 */
	public final int remainingCapacity;

	// We need to keep track of that in order to compute the cost of an action.
	private final int costPerKm;

	public State(City currentCity, TaskSet deliveries, TaskSet available, int remainingCapacity, int costPerKm) {
		this.currentCity = currentCity;
		this.deliveries = deliveries;
		this.availableTasks = available;
		this.remainingCapacity = remainingCapacity;
		this.costPerKm = costPerKm;
	}

	public boolean isFinal() {
		return deliveries.isEmpty() && availableTasks.isEmpty();
	}

	public List<Action> getLegalActions() {

		List<Action> actions = new ArrayList<Action>(); // The set of legal actions
		Set<City> destinations = new HashSet<City>(); // The set of interesting destination (pickup + delivery)

		// Drop case
		for (Task t : deliveries) {
			if (t.deliveryCity.equals(currentCity)) {
				// We are in the same city so we can deliver it now.
				actions.add(new Delivery(t));
			} else {
				// We need to go to the task's delivery site, hence we compute the path to this city and
				// add the next city on this path to our set of interesting destinations.
				List<City> path = currentCity.pathTo(t.deliveryCity);
				City nextStep = path.get(0);
				destinations.add(nextStep);
			}
		}

		// Pickup case
		for (Task t : availableTasks) {
			// We don't go to pickup location of task, nor pickup, that don't fit in our vehicle.
			if (t.weight > remainingCapacity)
				continue;

			if (t.pickupCity.equals(currentCity)) {
				// We are in the same city so we can pick it up now.
				actions.add(new Pickup(t));
			} else {
				// We need to go to this task's pickup site and similarly to the drop case
				// we add the next city toward it.
				List<City> path = currentCity.pathTo(t.pickupCity);
				City nextStep = path.get(0);
				destinations.add(nextStep);
			}

		}

		// Move actions
		for (City c : destinations) {
			assert currentCity.neighbors().contains(c); // just in case...
			actions.add(new Move(c));
		}

		return actions;
	}

	public State nextState(Action a) {
		return a.apply(); // See note about `Action` class.
	}

	/**
	 * We need access to logist.plan.Action's private member for the `nextState` method... So we apply a Facade pattern.
	 * And in order to avoid using instanceof in `nextState`, we use dynamic dispatch with `apply` on the action itself.
	 * 
	 * Note that those actions are bounded to the State object that created them so we have access to the state's
	 * internal fields.
	 */
	protected abstract class Action {
		public abstract logist.plan.Action getLogistAction();

		public abstract State apply(); // transform the current state

		public abstract double cost(); // cost (or reward) of applying the action

		// Get the state back from the action
		public final State getState() {
			return State.this;
		}
	}

	private final class Move extends Action {
		public final City destination;

		public Move(City destination) {
			this.destination = destination;
		}

		@Override
		public logist.plan.Action getLogistAction() {
			return new logist.plan.Action.Move(destination);
		}

		@Override
		public State apply() {
			// Simply move to the destination
			return new State(destination, deliveries, availableTasks, remainingCapacity, costPerKm);
		}

		@Override
		public double cost() {
			// Consume energy...
			double distance = currentCity.distanceTo(destination);
			return distance * costPerKm;
		}
	}

	private final class Pickup extends Action {
		public final Task task;

		public Pickup(Task task) {
			this.task = task;
		}

		@Override
		public logist.plan.Action getLogistAction() {
			return new logist.plan.Action.Pickup(task);
		}

		@Override
		public State apply() {
			assert remainingCapacity >= task.weight;

			// Transfer the task from one set to the other
			TaskSet newAvailableTasks = availableTasks.clone();
			newAvailableTasks.remove(task);

			TaskSet newDeliveries = deliveries.clone();
			newDeliveries.add(task);

			int newRemainingCapacity = remainingCapacity - task.weight;

			return new State(currentCity, newDeliveries, newAvailableTasks, newRemainingCapacity, costPerKm);
		}

		@Override
		public double cost() {
			// No reward yet
			return 0;
		}
	}

	private final class Delivery extends Action {
		public final Task task;

		public Delivery(Task task) {
			this.task = task;
		}

		@Override
		public logist.plan.Action getLogistAction() {
			return new logist.plan.Action.Delivery(task);
		}

		@Override
		public State apply() {
			// Drop the task and free the truck
			TaskSet newDeliveries = deliveries.clone();
			newDeliveries.remove(task);

			int newRemainingCapacity = remainingCapacity + task.weight;

			return new State(currentCity, newDeliveries, availableTasks, newRemainingCapacity, costPerKm);
		}

		@Override
		public double cost() {
			// Reward are a good things!
			return -task.reward;
		}
	}

	// * ECLIPSE GENERATE THIS: *//

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((availableTasks == null) ? 0 : availableTasks.hashCode());
		result = prime * result + ((currentCity == null) ? 0 : currentCity.hashCode());
		result = prime * result + ((deliveries == null) ? 0 : deliveries.hashCode());
		result = prime * result + remainingCapacity;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		State other = (State) obj;
		if (availableTasks == null) {
			if (other.availableTasks != null)
				return false;
		} else if (!availableTasks.equals(other.availableTasks))
			return false;
		if (currentCity == null) {
			if (other.currentCity != null)
				return false;
		} else if (!currentCity.equals(other.currentCity))
			return false;
		if (deliveries == null) {
			if (other.deliveries != null)
				return false;
		} else if (!deliveries.equals(other.deliveries))
			return false;
		if (remainingCapacity != other.remainingCapacity)
			return false;
		return true;
	}
}
