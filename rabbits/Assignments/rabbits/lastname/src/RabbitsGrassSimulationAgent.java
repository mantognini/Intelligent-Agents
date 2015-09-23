import java.awt.Color;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;

/**
 * Class that implements the simulation agent for the rabbits grass simulation.
 * 
 * @author
 */

class RabbitsGrassSimulationAgent implements Drawable {

	public RabbitsGrassSimulationAgent(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public void draw(SimGraphics graphics) {
		// Let's draw our beautiful rabbit here...
		graphics.drawFastRoundRect(Color.GRAY);
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}
	
	private int x;
	private int y;

}
