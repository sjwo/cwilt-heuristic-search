/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.algs.basic.bestfirst;


import org.cwilt.search.search.SearchAlgorithm;
import org.cwilt.search.search.SearchNode;
import org.cwilt.search.search.Solution;
import org.cwilt.search.utils.basic.MinHeap;

public class WAStar extends BestFirstSearch {

	@Override
	protected String paramString(){
		return "(" + weight + ")";
	}
	
	@Override
	protected double evaluateNode(SearchNode n){
		return n.getG() + n.getH() * this.weight;
	}
	
	protected final double weight;
	public SearchAlgorithm clone(){
		assert(l.getGenerations() == 0);
		return new WAStar(prob, l.clone(), weight);
	}
	
	public WAStar(org.cwilt.search.search.SearchProblem initial, org.cwilt.search.search.Limit l, double weight) {
		super(initial, l);
		this.weight = weight;
		open = new MinHeap<SearchNode>(new SearchNode.WFGComparator(weight));
	}

	
	protected boolean solutionGoodEnough(){
		Solution inc = super.getIncumbent();
		if(inc == null)
			return false;
		if(inc.getCost() <= open.peek().getG() + open.peek().getH() * weight)
			return true;
		else
			return false;
	}

}
