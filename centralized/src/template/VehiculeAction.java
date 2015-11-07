package template;

import logist.task.Task;

public class VehiculeAction {

	public enum Event {
		PICK, DELIVER
	};

	public final Event event;
	public final Task task;

	public VehiculeAction(Event event, Task task) {
		this.event = event;
		this.task = task;
	}

	public int getDifferentialWeight() {
		if (event == Event.PICK)
			return +task.weight;
		else
			return -task.weight;
	}

}
