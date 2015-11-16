package bidder;

/**
 * Base class for different bid strategies
 */
public abstract class BidStrategyTrait {
	/**
	 * Given the number of tasks already owned with the corresponding current cost, and also the estimated cost if we
	 * win the currently auctioned task, compute the desired bid value.
	 * 
	 * NB: the marginal cost is estimatedCost - currentCost.
	 */
	public abstract long bid(int nbTasks, double currentCost, double estimatedCost);
}
