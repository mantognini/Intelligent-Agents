import java.awt.Color;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;

/**
 * Class that implements the simulation agent for the rabbits grass simulation.
 * 
 * @author
 */

class RabbitsGrassSimulationAgent implements Drawable {

	public RabbitsGrassSimulationAgent(int x, int y, int energy,
			RabbitsGrassSimulationSpace space) {
		super();
		this.x = x;
		this.y = y;
		this.energy = energy;
		this.space = space;

		space.putRabbit(x, y, this);
	}

	/**
	 * Returns an offspring if the agent reproduces with itself
	 */
	public RabbitsGrassSimulationAgent step(int maxEatQuantity,
			int moveEnergyCost, int initialAmountOfEnergy, int birthThreshold,
			int energyConsumptionRate) {
		// Try to move to an adjacent cell only if it's free
		switch (Utils.uniform(0, 3)) {
		case 0: // North
			move(moveEnergyCost, x, y - 1);
			break;

		case 1: // South
			move(moveEnergyCost, x, y + 1);
			break;

		case 2: // East
			move(moveEnergyCost, x + 1, y);
			break;

		case 3: // West
			move(moveEnergyCost, x - 1, y);
			break;
		}

		// Try to eat
		eat(maxEatQuantity);

		// Default energy consumption
		energy -= energyConsumptionRate;

		// If possible, "reproduce"
		RabbitsGrassSimulationAgent offspring = reproduce(birthThreshold,
				initialAmountOfEnergy);

		// Remove from space if dead
		if (isDead()) {
			space.removeRabbit(x, y);
		}

		return offspring;
	}

	public boolean isDead() {
		return energy <= 0;
	}

	@Override
	public void draw(SimGraphics graphics) {
		// Let's draw our beautiful rabbit here...
		graphics.drawFastRoundRect(Color.WHITE);
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	private void move(int moveEnergyCost, int destX, int destY) {
		if (space.isFreeForRabbit(destX, destY)) {
			space.removeRabbit(x, y);
			space.putRabbit(destX, destY, this); // this will call setX/Y
			energy -= moveEnergyCost;
		}
	}

	private void eat(int maxEatQuantity) {
		energy += space.getEnergy(x, y, maxEatQuantity);
	}

	private RabbitsGrassSimulationAgent reproduce(int birthThreshold,
			int initialAmountOfEnergy) {
		if (energy < birthThreshold || initialAmountOfEnergy >= energy)
			return null;

		// Find a room for newborn
		int freeX = x;
		int freeY = y;
		if (space.isFreeForRabbit(x, y - 1)) {
			freeY = y - 1;
		} else if (space.isFreeForRabbit(x + 1, y)) {
			freeX = x + 1;
		} else if (space.isFreeForRabbit(x, y + 1)) {
			freeY = y + 1;
		} else if (space.isFreeForRabbit(x - 1, y)) {
			freeX = x - 1;
		} else {
			return null; // No spot available...
		}

		// Now we're sure we can reproduce
		energy -= initialAmountOfEnergy;
		return new RabbitsGrassSimulationAgent(freeX, freeY,
				initialAmountOfEnergy, space);
	}

	private int x;
	private int y;
	private int energy;
	private RabbitsGrassSimulationSpace space;
}
