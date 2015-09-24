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

	public RabbitsGrassSimulationSpace(int worldSize) {
		size = worldSize;
		grass = new Object2DGrid(worldSize, worldSize);
		rabbits = new Object2DGrid(worldSize, worldSize);

		// Fill the space with no grass at all initially
		for (int i = 0; i < worldSize; i++) {
			for (int j = 0; j < worldSize; j++) {
				grass.putObjectAt(i, j, new Integer(0));
			}
		}
	}

	// Increase the grass height in a few places
	public void growGrass(int amount) {
		while (amount > 0) {
			int x = Utils.uniform(0, size - 1);
			int y = Utils.uniform(0, size - 1);

			Integer value = (Integer) grass.getObjectAt(x, y);

			// Don't allow more than the maximum:
			value = Math.min(value + 1, MAX_GRASS);

			grass.putObjectAt(x, y, value);

			amount--;
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

	boolean isFreeForRabbit(int x, int y) {
		return x >= 0 && y >= 0 && x < rabbits.getSizeX()
				&& y < rabbits.getSizeY() && rabbits.getObjectAt(x, y) == null;
	}

	public void putRabbit(int x, int y, RabbitsGrassSimulationAgent rabbit) {
		assert isFreeForRabbit(x, y);
		rabbits.putObjectAt(x, y, rabbit);
		rabbit.setX(x);
		rabbit.setY(y);
	}

	public void removeRabbit(int x, int y) {
		assert !isFreeForRabbit(x, y);
		rabbits.putObjectAt(x, y, null);
	}

	/**
	 * Allow rabbits to eat grass
	 */
	public int getEnergy(int x, int y, int maxEatQuantity) {
		Integer value = (Integer) grass.getObjectAt(x, y);
		int taken = Math.min(maxEatQuantity, value);
		value -= taken;
		grass.putObjectAt(x, y, value);
		return taken;
	}

	// Grids: representing objects on the discrete space
	private Object2DGrid grass;
	private Object2DGrid rabbits;
	private int size; // of the world

	static private final int MAX_GRASS = 255; // TODO Do we need that?

	// TODO maybe we'll have to deal with more than "255 grass"...
	// Map integer in [0, 255] to a specific green
	static private final ColorMap GREENS = new ColorMap();
	static {
		for (int i = 0; i <= MAX_GRASS; i++) {
			GREENS.mapColor(i, new Color(0, i, 0));
		}
	}
}
