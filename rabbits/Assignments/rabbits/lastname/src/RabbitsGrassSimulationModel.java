import uchicago.src.reflector.RangePropertyDescriptor;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.DisplaySurface;

/**
 * Class that implements the simulation model for the rabbits grass simulation.
 * This is the first class which needs to be setup in order to run Repast
 * simulation. It manages the entire RePast environment and the simulation.
 *
 * @author
 */

public class RabbitsGrassSimulationModel extends SimModelImpl {

	/**
	 * Prepares the model for a new run
	 */
	@Override
	public void setup() {
		// clean everything in fact
		space = null;
		surface = null;
		schedule = null;

		// register the growth of grass as a slider
		RangePropertyDescriptor grassSlider = new RangePropertyDescriptor(
				"GrassGrowthRate", 0, 255, 50);
		descriptors.put("GrassGrowthRate", grassSlider);
	}

	/**
	 * Gets the names of the initial model parameters to set.
	 */
	@Override
	public String[] getInitParam() {
		String[] initParams = { "GridSize", "GrassGrowthRate",
				"InitialRabbits", "BirthThreshold", "MaxEatQuantity" };
		return initParams;
	}

	/**
	 * Begins a simulation run.
	 */
	@Override
	public void begin() {
		buildModel();
		buildSchedule();
		buildDisplay();

		// Open the display window
		surface.display();
	}

	@Override
	public String getName() {
		return "Killer Rabbit of Caerbannog";
	}

	@Override
	public Schedule getSchedule() {
		return schedule;
	}

	public int getGridSize() {
		return gridSize;
	}

	public void setGridSize(int gridSize) {
		this.gridSize = gridSize;
	}

	public int getGrassGrowthRate() {
		return grassGrowthRate;
	}

	public void setGrassGrowthRate(int grassGrowthRate) {
		this.grassGrowthRate = grassGrowthRate;
	}

	public int getInitialRabbits() {
		return initialRabbits;
	}

	public void setInitialRabbits(int initialRabbits) {
		this.initialRabbits = initialRabbits;
	}

	public int getBirthThreshold() {
		return birthThreshold;
	}

	public void setBirthThreshold(int birthThreshold) {
		this.birthThreshold = birthThreshold;
	}

	public int getMaxEatQuantity() {
		return maxEatQuantity;
	}

	public void setMaxEatQuantity(int maxEatQuantity) {
		this.maxEatQuantity = maxEatQuantity;
	}

	private void buildModel() {
		space = new RabbitsGrassSimulationSpace(getGridSize(),
				getInitialRabbits());
	}

	private void buildSchedule() {
		schedule = new Schedule();

		// Perform every action
		schedule.scheduleActionAtInterval(1, new BasicAction() {
			@Override
			public void execute() {
				// Grow the grass at every clock tick
				space.growGrass(grassGrowthRate);

				// Move rabbits around and let them eat
				// space.updateRabbits();

				// Repaint the surface frequently
				surface.updateDisplay();
			}
		});
	}

	private void buildDisplay() {
		surface = new DisplaySurface(space.getDimension(), this, "Display");
		surface.addDisplayable(space.getGrassDisplayable(), "Grass");
		surface.addDisplayable(space.getRabbitsDisplayable(), "Rabbits");

		registerDisplaySurface("World", surface);
	}

	// Our even scheduler
	private Schedule schedule;

	// Our space representation
	private RabbitsGrassSimulationSpace space;

	// 2D surface for rendering
	private DisplaySurface surface;

	// Simulation parameters
	private int gridSize = DEFAULT_GRID_SIZE;
	private int grassGrowthRate = DEFAULT_GRASS_GROWTH_RATE;
	private int initialRabbits = DEFAULT_INIITIAL_RABBITS;
	private int birthThreshold = DEFAULT_BRITH_THRESHOLD;
	private int maxEatQuantity = DEFAULT_MAX_EAT_QUANTITY;

	// Default values for parameters
	static private final int DEFAULT_GRID_SIZE = 20;
	static private final int DEFAULT_GRASS_GROWTH_RATE = 1;
	static private final int DEFAULT_INIITIAL_RABBITS = 1;
	static private final int DEFAULT_BRITH_THRESHOLD = 50;
	static private final int DEFAULT_MAX_EAT_QUANTITY = 50;
}
