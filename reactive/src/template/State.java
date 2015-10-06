package template;

import logist.topology.Topology.City;

class State {

	private City currentCity;
	private City cityTask;

	public State(City city, City task) {
		this.currentCity = city;
		this.cityTask = task;
	}

	public City getCity() {
		return this.currentCity;
	}

	public City getTask() {
		return this.cityTask;
	}

}
