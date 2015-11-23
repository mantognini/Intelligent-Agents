package bidder;

import utils.Utils;

public class NoPainMinOfBest extends BidStrategyTrait {

	private double marginRatio;
	private int depth;

	public NoPainMinOfBest(double marginRatio, int depth) {
		super();
		this.marginRatio = marginRatio;
		this.depth = depth;
	}

	@Override
	public long bid(double marginalCost) {

		Long minBid = Utils.min(bidHistory.get(Utils.bestAgent(winners, bidHistory, depth)), depth);

		double bid = marginalCost;
		if (minBid > marginalCost)
			bid += (marginalCost - minBid) * marginRatio;

		return Utils.toLong(bid);
	}
}
