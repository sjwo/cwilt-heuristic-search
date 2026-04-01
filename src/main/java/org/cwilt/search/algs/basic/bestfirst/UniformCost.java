package org.cwilt.search.algs.basic.bestfirst;
import org.cwilt.search.search.SearchAlgorithm;
import org.cwilt.search.search.SearchNode;
import org.cwilt.search.search.Solution;
import org.cwilt.search.utils.basic.MinHeap;
public class UniformCost extends BestFirstSearch {

	public SearchAlgorithm clone(){
		assert(l.getGenerations() == 0);
		UniformCost a = new UniformCost(prob, l.clone());
		return a;
	}
	
	public UniformCost(org.cwilt.search.search.SearchProblem initial, org.cwilt.search.search.Limit l) {
		super(initial, l);
		open = new MinHeap<SearchNode>(new SearchNode.GHComparator());
	}
	protected double evaluateNode(SearchNode n){
		return n.getG();
	}

	protected boolean solutionGoodEnough(){
		Solution inc = super.getIncumbent();
		if(inc == null)
			return false;
		if(inc.getCost() <= open.peek().getF())
			return true;
		else
			return false;
	}

	
}
