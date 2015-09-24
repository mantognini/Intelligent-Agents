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
	 * Returns true if the agent is still alive
	 */
	public boolean step(int maxEatQuantity, int moveEnergyCost) {
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

		// Remove from space if dead
		if (energy <= 0) {
			space.removeRabbit(x, y);
			return false;
		} else {
			return true;
		}
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

	private int x;
	private int y;
	private int energy;
	private RabbitsGrassSimulationSpace space;
}
