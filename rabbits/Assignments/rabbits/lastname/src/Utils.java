import uchicago.src.sim.util.Random;


class Utils {
	static int uniform(int min, int max) {
		Random.createUniform();
		return Random.uniform.nextIntFromTo(min, max);
	}
}
