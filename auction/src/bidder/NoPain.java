package bidder;

import utils.Utils;

public class NoPain extends BidStrategyTrait {

	private double epsilon;

	public NoPain(double epsilon) {
		this.epsilon = epsilon;
	}

	@Override
	public long bid(double marginalCost) {
		return Utils.toLong(marginalCost + epsilon);
	}

}
