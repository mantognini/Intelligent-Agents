package bidder;

import java.util.HashMap;
import java.util.List;

/**
 * Base class for different bid strategies
 */
public abstract class BidStrategyTrait {

	/**
	 * Store the bid history for all the current agents. The agent are identify by their index on bidList
	 */
	public HashMap<Integer, List<Long>> bidHistory = new HashMap<>();

	/**
	 * Given the number of tasks already owned with the corresponding current cost, and also the estimated cost if we
	 * win the currently auctioned task, compute the desired bid value.
	 * 
	 * NB: the marginal cost is estimatedCost - currentCost.
	 */
	public abstract long bid(double marginalCost);

	public void addBids(Long[] bids) {
		for (int i = 0; i < bids.length; ++i) {
			List<Long> currenbids = bidHistory.get(i);
			currenbids.add(bids[i]);
			bidHistory.put(i, currenbids);
		}
	}

}
