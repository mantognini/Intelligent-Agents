package template;

import logist.topology.Topology.City;

/*
 * Represents the state of a Reactive Agent.
 * 
 * Assuming there are N cities in the topology of the agent's world, a state is represented as follow:
 * 
 * - city: the city in which the agent is
 * - task: if less than N it means that a task for the `task`-th city of the topology is available,
 *         otherwise it means no task is available in the current `city`.
 */
class State {

	public final City city;
	public final int task;

	public State(City city, int task) {
		this.city = city;
		this.task = task;
	}

	// Eclipse generate this; no human should read it!
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result + task;
		return result;
	}

	// Eclipse generate this; no human should read it!
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		State other = (State) obj;
		if (city == null) {
			if (other.city != null)
				return false;
		} else if (!city.equals(other.city))
			return false;
		if (task != other.task)
			return false;
		return true;
	}

}
