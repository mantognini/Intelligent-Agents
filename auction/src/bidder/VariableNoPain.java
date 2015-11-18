package bidder;

import java.util.ArrayList;
import java.util.List;

/**
 * Extended no pain estimate the epsilone, such as NoPain, but in accordance to the others bid
 *
 */
public class VariableNoPain extends BidStrategyTrait {

	@Override
	public long bid(double marginalCost) {
		// TODO Auto-generated method stub
		return 0;
	}

	private double averageBid() {
		List<Long> bids = new ArrayList<Long>();
		for (int id : bidHistory.keySet()) {
			bids.addAll(bidHistory.get(id));
		}

	}
}
