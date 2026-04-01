package org.cwilt.search.algs.experimental;
import java.io.PrintStream;
import java.util.Queue;

import org.cwilt.search.search.Limit;
import org.cwilt.search.search.SearchAlgorithm;
import org.cwilt.search.search.SearchNode;
import org.cwilt.search.search.SearchProblem;
import org.cwilt.search.search.Solution;
import org.cwilt.search.utils.basic.Heapable;
import org.cwilt.search.utils.basic.MinHeap;
import org.cwilt.search.utils.experimental.FastFloatHeap;
import org.cwilt.search.algs.basic.bestfirst.BestFirstSearch;
public class DynamicOpenAStar extends BestFirstSearch{

	public DynamicOpenAStar(SearchProblem initial, Limit l) {
		super(initial, l);
		open = new MinHeap<SearchNode>(new SearchNode.FGComparator());
	}
	private long switchTime = 0;
	
	private boolean switched = false;
	
	private static final int SPLIT = 5000;
	private static final int SWITCH = 100000;
	protected void setIncumbent() {
		l.startClock();
		Solution goal = null;
		while (!open.isEmpty() && goal == null && l.keepGoing() && !solutionGoodEnough()) {
			if(open.size() > SWITCH && !switched){
				switched = true;
				long switchStart = System.currentTimeMillis();
				Queue<SearchNode> oldOpen = open;
				open = new FastFloatHeap<SearchNode>(new SearchNode.FGComparator(), 0, open.peek().getF() * 1.5, SPLIT);
				SearchNode[] allNodes = oldOpen.toArray(new SearchNode[oldOpen.size()]);
				for(int i = 0; i < allNodes.length; i++){
					open.add(allNodes[i]);
				}
				long switchEnd = System.currentTimeMillis();
				switchTime = switchEnd - switchStart;
			}
			
			SearchNode current = open.poll();
			assert (current != null);
			assert(current.getHeapIndex() == Heapable.NO_POS);
			goal = processNode(current);
//			boolean cc = closedCheck();
//			if(!cc){
//				System.err.println(current);
//				assert(false);
//			}
		}
		l.endClock();
		if (goal != null)
			setIncumbent(goal);
	}

	@Override
	public SearchAlgorithm clone() {
		return new DynamicOpenAStar(prob, l.clone());
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
	@Override
	public void printExtraData(PrintStream ps){
		super.printExtraData(ps);
		SearchAlgorithm.printPair(ps, "switch time", new Long(switchTime));
	}
}
