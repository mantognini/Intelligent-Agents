import java.awt.Color;
import java.awt.Dimension;

import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.Displayable;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * 
 * @author
 */

class RabbitsGrassSimulationSpace {

	public RabbitsGrassSimulationSpace(int worldSize, int rabbitCount) {
		grass = new Object2DGrid(worldSize, worldSize);
		rabbits = new Object2DGrid(worldSize, worldSize);

		// Fill the space with no grass at all initially
		for (int i = 0; i < worldSize; i++) {
			for (int j = 0; j < worldSize; j++) {
				grass.putObjectAt(i, j, new Integer(0));
			}
		}

		// Insert at most worldSize x worldSize rabbits on the plane
		rabbitCount = Math.min(rabbitCount, worldSize * worldSize);
		while (rabbitCount > 0) {
			int x = Utils.uniform(0, worldSize - 1);
			int y = Utils.uniform(0, worldSize - 1);
			if (isFree(x, y)) {
				rabbits.putObjectAt(x, y, new RabbitsGrassSimulationAgent(x, y));
				rabbitCount--;
			}
		}

		// TODO remove me
		grass.putObjectAt(0, 0, new Integer(255));
		grass.putObjectAt(worldSize - 1, 0, new Integer(125));
		grass.putObjectAt(0, worldSize - 1, new Integer(125));
	}

	// Increase the grass height everywhere
	public void growGrass(int amount) {
		for (int i = 0; i < grass.getSizeX(); i++) {
			for (int j = 0; j < grass.getSizeY(); j++) {
				Integer value = (Integer) grass.getObjectAt(i, j);

				// Don't allow more than the maximum:
				value = Math.min(value + amount, MAX_GRASS);

				grass.putObjectAt(i, j, value);
			}
		}
	}

	public Displayable getGrassDisplayable() {
		return new Value2DDisplay(grass, GREENS);
	}

	public Displayable getRabbitsDisplayable() {
		return new Object2DDisplay(rabbits);
	}

	public Dimension getDimension() {
		return grass.getSize();
	}

	private boolean isFree(int x, int y) {
		return rabbits.getObjectAt(x, y) == null;
	}

	// Grids: representing objects on the discrete space
	private Object2DGrid grass;
	private Object2DGrid rabbits;

	static private final int MAX_GRASS = 255; // TODO Do we need that?

	// TODO maybe we'll have to deal with more than "255 grass"...
	// Map integer in [0, 255] to a specific green
	static private final ColorMap GREENS = new ColorMap();
	static {
		for (int i = 1; i <= MAX_GRASS; i++) {
			GREENS.mapColor(i, new Color(0, i, 0));
		}
		GREENS.mapColor(0, Color.WHITE); // we don't want 0 to be black
	}
}
