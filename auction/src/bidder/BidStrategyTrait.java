package bidder;

import java.util.ArrayList;
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
	 * Store the number of win
	 */
	public List<Integer> winners = new ArrayList<>();

	/**
	 * ID of the agent for which the bidder should estimate the bid
	 */
	public final int agentID;

	public BidStrategyTrait(int agentID) {
		this.agentID = agentID;
	}

	/**
	 * Given the number of tasks already owned with the corresponding current cost, and also the estimated cost if we
	 * win the currently auctioned task, compute the desired bid value.
	 * 
	 * NB: the marginal cost is estimatedCost - currentCost.
	 */
	public abstract long bid(double marginalCost);

	public void addBids(Long[] bids, int winnerID) {
		for (int i = 0; i < bids.length; ++i) {
			List<Long> currenbids = bidHistory.get(i);
			if (currenbids == null) {
				currenbids = new ArrayList<Long>();
			}
			currenbids.add(bids[i]);
			bidHistory.put(i, currenbids);
		}

		winners.add(winnerID);
	}

}
