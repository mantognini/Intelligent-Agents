package template;

public class VehiculeAction {

	public enum Action {
		PICK, DELIVER
	};

	public final Action action;
	public final int taksID;

	public VehiculeAction(Action action, int taksID) {
		super();
		this.action = action;
		this.taksID = taksID;
	}

}
