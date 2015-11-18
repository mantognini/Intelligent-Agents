package bidder;

import java.util.ArrayList;
import java.util.List;

import utils.Utils;

/**
 * No pain average estimate ε, such as NoPain, but in accordance to the others bid.
 *
 */
public class NoPainAverage extends BidStrategyTrait {

	@Override
	public long bid(double marginalCost) {
		double averageBid = averageBid();

		if (averageBid == 0 || marginalCost > averageBid) {
			return Utils.toLong(marginalCost);
		}

		return Utils.toLong(averageBid);
	}

	private double averageBid() {
		List<Long> bids = new ArrayList<Long>();
		for (int id : bidHistory.keySet()) {
			bids.addAll(bidHistory.get(id));
		}
		return Utils.averageListLong(bids);
	}

}
