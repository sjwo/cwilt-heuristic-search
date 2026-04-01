/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.algs.basic.bestfirst;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import org.cwilt.search.search.SearchAlgorithm;
import org.cwilt.search.search.SearchNode;
import org.cwilt.search.search.SearchState;
import org.cwilt.search.search.Solution;
import org.cwilt.search.utils.basic.Heapable;
public abstract class BestFirstSearch extends org.cwilt.search.search.SearchAlgorithm {
	private int vascilation = 0;
	private double lastBest = Double.MAX_VALUE;
	
	protected double evaluateNode(SearchNode n){
		return n.getF();
	}
	
	public void reset() {
		closed.clear();
		open.clear();
	}
	
	protected final HashMap<Object, SearchNode> closed;
	protected Queue<SearchNode> open;

	protected BestFirstSearch(org.cwilt.search.search.SearchProblem initial, org.cwilt.search.search.Limit l) {
		super(initial, l);
		closed = new HashMap<Object, SearchNode>();
	}

	@Override
	public ArrayList<SearchState> solve() {
		SearchNode i = SearchNode.makeInitial(initial);
		open.add(i);
		closed.put(i.getState().getKey(), i);

		try {
			setIncumbent();
		} catch (OutOfMemoryError e) {
			l.setOutOfMemory();
			l.endClock();
			open.clear();
			closed.clear();
		}
		if (getIncumbent() == null)
			return null;
		else {
			// System.err.println(incumbent.getGoal().printParents());
			ArrayList<SearchState> finalPath = getIncumbent().reconstructPath();
			return finalPath;
		}
	}

//	private java.util.HashSet<Object> expanded = new java.util.HashSet<Object>();
	
	protected Solution processNode(SearchNode current) {
		double nextValue = evaluateNode(current);
		if(nextValue <= lastBest)
			lastBest = nextValue;
		else if (nextValue - lastBest > 0.00000001){
			vascilation ++;
			lastBest = nextValue;
		}
		
		if (current.getState().isGoal()) {
			if (getIncumbent() == null
					|| getIncumbent().getCost() > current.getG())
				return new Solution(current, current.getG(), l.getDuration(),
						current.pathLength(), l.getExpansions(),
						l.getGenerations(), l.getDuplicates());
		} else {
			ArrayList<? extends SearchNode> children = current.expand();
			l.incrExp();
			int max = children.size();
			
//			if(expanded.contains(current.getState().getKey())){
//				l.incrReExp();
//			} else {
//				expanded.add(current.getState().getKey());
//			}
			
			l.incrGen(max);
			for(int i = 0; i < max; i++){
				considerChild(children.get(i));
			}
//			Iterator<? extends SearchNode> childIter = children.iterator();
//			while (childIter.hasNext()) {
//				l.incrGen();
//				considerChild(childIter.next());
//			}
		}
		return null;
	}
	
	protected abstract boolean solutionGoodEnough();
	
	protected void setIncumbent() {
		l.startClock();
		Solution goal = null;
		while (!open.isEmpty() && goal == null && l.keepGoing() && !solutionGoodEnough()) {
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

	
	@SuppressWarnings("unused")
	private boolean closedCheck() {
		Iterator<Entry<Object, SearchNode>> i = closed.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<Object, SearchNode> n = i.next();
			if (n.getValue().getHeapIndex() != Heapable.NO_POS) {
				if (!open.contains(n.getValue())) {
					System.err.println("failed to find this:");
					System.err.println(n.getValue());
					return false;
				}
			}
		}
		return true;
	}

	public void considerChild(SearchNode child) {
		// look for the child in the hash table
		SearchNode incumbentNode = closed.get(child.getState().getKey());
		if (incumbentNode == null) {
			open.add(child);
			closed.put(child.getState().getKey(), child);
		} else if (child.getG() < incumbentNode.getG()) {
			int ix = incumbentNode.getHeapIndex();
			if (ix != Heapable.NO_POS) {
				assert (open.contains(incumbentNode));

				boolean r = open.remove(incumbentNode);
				assert (incumbentNode.getHeapIndex() == Heapable.NO_POS);

				if (!r) {
					r = open.remove(incumbentNode);
					assert (r);
				}
			}
			open.add(child);
			SearchNode r = closed.remove(incumbentNode.getState().getKey());
			//have to put this back on the closed list
			closed.put(child.getState().getKey(), child);
			assert (r.getHeapIndex() == Heapable.NO_POS);
			assert (r != null);
		}
		if (incumbentNode != null)
			l.incrDup();
	}

	public SearchState findFirstGoal() {
		// if the open list is empty, have to do some stuff, otherwise just
		// continue
		if (open.isEmpty()) {
			SearchNode i = SearchNode.makeInitial(initial);
			open.add(i);
			closed.put(i.getState().getKey(), i);
		}
		this.setIncumbent();
		if (getIncumbent() != null)
			return getIncumbent().getGoal().getState();
		else
			return null;
	}

	@Override
	public void printExtraData(PrintStream ps){
		super.printExtraData(ps);
		SearchAlgorithm.printPair(ps, "vascilations", this.vascilation);
	}
	
	protected void cleanup() {
		open.clear();
		closed.clear();
	}

}
