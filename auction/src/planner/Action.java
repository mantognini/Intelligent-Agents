package planner;

import logist.task.Task;

/**
 * Define an action as an event-task pair, where the event can be pick or deliver
 */
public class Action {

	public enum Event {
		PICK, DELIVER
	};

	public final Event event;
	public final Task task;

	public Action(Event event, Task task) {
		this.event = event;
		this.task = task;
	}

	public int getDifferentialWeight() {
		if (event == Event.PICK)
			return +task.weight;
		else
			return -task.weight;
	}

	@Override
	public String toString() {
		return event + " task n° " + task.id + " (" + task.weight + ")";
	}

}
