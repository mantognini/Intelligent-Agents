package bidder;

import utils.Utils;

public class NoPainMinOfBest extends BidStrategyTrait {

	private double marginRatio;
	private int depth;

	public NoPainMinOfBest(int agentID, double marginRatio, int depth) {
		super(agentID);
		this.marginRatio = marginRatio;
		this.depth = depth;
	}

	@Override
	public long bid(double marginalCost) {

		double bid = marginalCost;

		if (winners.size() > 0) {
			int bestAgentIndex = Utils.bestAgent(winners, bidHistory, depth); // TODO don't include ourselves
			Long minBid = Utils.min(bidHistory.get(bestAgentIndex), depth);

			if (minBid > marginalCost) {
				bid += (minBid - marginalCost) * marginRatio;
			}

			System.out.println("Gispsy : ");
			System.out.println("	Marginal Cost : " + marginalCost);
			System.out.println("	minBid : " + minBid);
			System.out.println("	final bid : " + bid);
		}

		return Utils.toLong(bid);
	}
}
