package bidder;

import utils.Utils;

/**
 * Very simple bid strategy: take no risk but ask for no profit
 */
public class NoPain extends BidStrategyTrait {

	@Override
	public long bid(int nbTasks, double currentCost, double estimatedCost) {
		// compute marginal cost
		return Utils.toLong(estimatedCost - currentCost);
	}

}
