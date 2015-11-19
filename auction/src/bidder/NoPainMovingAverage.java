package bidder;

import utils.Utils;

/**
 * No pain average estimate ε, such as NoPain, but in accordance to the others bid.
 *
 */
public class NoPainMovingAverage extends BidStrategyTrait {

	private int currentId;
	private double marginRatio;

	public NoPainMovingAverage(int id, double marginRatio) {
		super();
		this.currentId = id;
	}

	@Override
	public long bid(double marginalCost) {
		double averageBid = minAverageBid();

		if (averageBid == 0 || marginalCost > averageBid) {
			return Utils.toLong(marginalCost);
		}

		double margin = averageBid - marginalCost;

		return Utils.toLong(margin + marginRatio * margin);
	}

	private double minAverageBid() {
		double min = Double.MAX_VALUE;
		for (int id : bidHistory.keySet()) {
			if (id != currentId) {
				min = Math.min(min, Utils.movingAverage(bidHistory.get(id), 5));
			}
		}
		return min;
	}

}
