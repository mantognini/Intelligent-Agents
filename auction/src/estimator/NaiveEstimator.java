package estimator;

import java.util.Random;

import logist.Measures;
import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology.City;
import planner.PlannerTrait;
import utils.Utils;

public class NaiveEstimator extends CostEstimatorTrait {

	private Vehicle vehicle;
	private Random random;
	private City currentCity;

	public NaiveEstimator(Agent agent) {
		random = new Random(123458670);
		vehicle = agent.vehicles().get(0);
		currentCity = vehicle.getCurrentCity();
	}

	@Override
	public double computeMC(PlannerTrait planner, Task task) {
		Utils.ensure(vehicle.capacity() >= task.weight, "Task too heavy for the current vehicle");
		long distanceTask = task.pickupCity.distanceUnitsTo(task.deliveryCity);
		long distanceSum = distanceTask + currentCity.distanceUnitsTo(task.pickupCity);
		double marginalCost = Measures.unitsToKM(distanceSum * vehicle.costPerKm());

		double ratio = 1.0 + (random.nextDouble() * 0.05 * task.id);
		return ratio * marginalCost;
	}
}
