package template;

import logist.task.Task;

public class VehiculeAction {

	public enum Action {
		PICK, DELIVER
	};

	public final Action action;
	public final Task task;

	public VehiculeAction(Action action, Task task) {
		this.action = action;
		this.task = task;
	}

}
