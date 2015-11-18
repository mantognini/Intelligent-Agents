package bidder;

import utils.Utils;

/**
 * Very simple bid strategy: take no risk but ask for no profit
 */
public class NoGain extends BidStrategyTrait {

	@Override
	public long bid(double marginalCost) {
		return Utils.toLong(marginalCost);
	}

}
