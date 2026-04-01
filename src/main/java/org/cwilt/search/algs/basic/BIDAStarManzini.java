package org.cwilt.search.algs.basic;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

import org.cwilt.search.search.Limit;
import org.cwilt.search.search.SearchAlgorithm;
import org.cwilt.search.search.SearchNode;
import org.cwilt.search.search.SearchProblem;
import org.cwilt.search.search.SearchState;
import org.cwilt.search.search.Solution;
import org.cwilt.search.utils.basic.Heapable;
import org.cwilt.search.utils.basic.MinHeap;
public class BIDAStarManzini extends org.cwilt.search.search.SearchAlgorithm {
	public final int perimeterSize;

	public BIDAStarManzini(SearchProblem prob, Limit l, int perimeterSize) {
		super(prob, l);
		this.perimeterSize = perimeterSize;
		this.perimeter = new ArrayList<SearchNode>(perimeterSize);
	}

	public void reset() {
		bestRejectedF = 0;
	}

	public SearchAlgorithm clone() {
		BIDAStarManzini i = new BIDAStarManzini(prob, l.clone(), perimeterSize);
		i.bestRejectedF = bestRejectedF;
		return i;
	}

	private double bestRejectedF;

	@Override
	public SearchState findFirstGoal() {
		assert (false);
		System.err.println("IDA* only runs once.");
		System.exit(1);
		return null;
	}

	private int heuristicEvaluations = 0;

	protected double initialF(SearchState n, double bound) {
		// use the perimeter
		double minValue = Double.MAX_VALUE;
		for (int i = 0; i < perimeter.size(); i++) {
			heuristicEvaluations++;
			double fHere = n.distTo(perimeter.get(i).getState())
					+ perimeter.get(i).getG();
			if (fHere < minValue)
				minValue = fHere;
		}
		return minValue;
	}

	private static final class PerimeterCheck{
		private final double value;
		private final boolean passes;
		private final SearchNode goal;
		
		public PerimeterCheck(){
			this.passes = true;
			this.value = Double.NaN;
			this.goal = null;
		}
		public PerimeterCheck(double value, SearchNode g, boolean passes){
			this.value = value;
			this.goal = g;
			this.passes = passes;
		}
	}
	
	protected PerimeterCheck perimeterF(SearchNode n, double bound, BitSet checks) {

		// use the perimeter
		double minValue = Double.MAX_VALUE;
		for (int i = 0; i < perimeter.size(); i++) {
			//skip over the ones that are not set
			if(checks.get(i))
				continue;
			
			heuristicEvaluations++;
			double fHere = n.getG()
					+ n.getState().distTo(perimeter.get(i).getState())
					+ perimeter.get(i).getG();
			if(n.getState().equals(perimeter.get(i).getState())){
				return new PerimeterCheck(fHere, perimeter.get(i), fHere <= bound);
			}
			if (fHere <= bound)
				return new PerimeterCheck();
			if (fHere < minValue)
				minValue = fHere;
			if(fHere > minValue)
				checks.set(i);
		}
		// if we get to here, the node has its real f value, but whatever that
		// value is it is larger than bound.
		return new PerimeterCheck(minValue, null, minValue <= bound);
		// return n.getF() <= bound;
	}

	@Override
	public void printExtraData(PrintStream s) {
		SearchAlgorithm.printPair(s, "h_eval",
				new Integer(heuristicEvaluations));
	}

	private void doIteration(final double bound, SearchNode n, BitSet checks) {
		if(this.getIncumbent() != null && this.getIncumbent().getCost() <= bound)
			return;
		
		BitSet toCheck = (BitSet) checks.clone();
		PerimeterCheck c = perimeterF(n, bound, toCheck);
		if (!c.passes) {
			if (c.value < bestRejectedF)
				bestRejectedF = c.value;
		} else {
			if (n.getState().isGoal()) {
				setIncumbent(new Solution(n, n.getG(), l.getDuration(),
						n.pathLength(), l.getExpansions(), l.getGenerations(),
						l.getDuplicates()));
				return;
			}
			if (c.goal != null){
				
				Solution s = new Solution(n, n.getG() + c.goal.getG(),
						l.getDuration(), n.pathLength() + c.goal.pathLength()
								- 1, l.getExpansions(), l.getGenerations(),
						l.getDuplicates());
				this.setIncumbent(s);
				return;
			}
			ArrayList<? extends SearchNode> children = n.expand();
			l.incrExp();
			l.incrGen(children.size());
			for(int i = 0; i < children.size() && getIncumbent() == null; i++) {
				doIteration(bound, children.get(i), toCheck);
			}
		}
	}

	private final ArrayList<SearchNode> perimeter;

	private void createPerimeter() {
		MinHeap<SearchNode> gb = new MinHeap<SearchNode>(
				new SearchNode.GComparator());
		HashMap<Object, SearchNode> gbClosed = new HashMap<Object, SearchNode>();
		ArrayList<ArrayList<? extends SearchNode>> storedChildren = new ArrayList<ArrayList<? extends SearchNode>>();
		ArrayList<SearchNode> newPerimeter = new ArrayList<SearchNode>();

		for (SearchState s : prob.getGoals()) {
			SearchNode newNode = SearchNode.makeInitial(s);
			gb.add(newNode);
			gbClosed.put(s.getKey(), newNode);
			l.incrGen();
		}

		while (newPerimeter.size() < perimeterSize) {
			SearchNode next = gb.poll();
			assert (next != null);
			if (next.getState().getKey().equals(prob.getInitial().getKey())) {
				// found the start going backwards, so can stop searching.
				this.setIncumbent(next);
				break;
			}
			newPerimeter.add(next);
			l.incrExp();
			ArrayList<? extends SearchNode> children = next.reverseExpand();
			storedChildren.add(children);
			l.incrGen(children.size());
			for (SearchNode c : children) {
				SearchNode incumbent = gbClosed.get(c.getState().getKey());
				if (incumbent == null) {
					gb.add(c);
					gbClosed.put(c.getState().getKey(), c);
				} else if (incumbent.getG() > c.getG()) {
					assert (incumbent.getHeapIndex() != Heapable.NO_POS);
					boolean removed = gb.remove(incumbent);
					assert (removed);
					gb.add(c);
					gbClosed.put(c.getState().getKey(), c);
				}
			}
		}

		for (int i = 0; i < newPerimeter.size(); i++) {
			ArrayList<? extends SearchNode> children = storedChildren.get(i);
			boolean allExpanded = true;
			for (SearchNode n : children) {
				SearchNode incumbent = gbClosed.get(n.getState().getKey());
				assert (incumbent != null);
				if (incumbent.getHeapIndex() != Heapable.NO_POS) {
					allExpanded = false;
					break;
				}
			}
			if (!allExpanded) {
				perimeter.add(newPerimeter.get(i));
			}
		}
	}

	private void setIncumbent() {
		l.startClock();
		// have to create the perimeter first
		createPerimeter();
		double bound = initialF(initial, Double.MAX_VALUE);
		while (getIncumbent() == null && l.keepGoing()) {
			if (bestRejectedF < Double.MAX_VALUE)
				bound = bestRejectedF;
			bestRejectedF = Double.MAX_VALUE;
			BitSet toCheck = new BitSet(perimeter.size());
			doIteration(bound, SearchNode.makeInitial(initial), toCheck);
			if (bestRejectedF == Double.MAX_VALUE) {
				l.endClock();
				return;
			}
		}
		l.endClock();
	}

	@Override
	public ArrayList<SearchState> solve() {
		try {
			setIncumbent();
		} catch (OutOfMemoryError e) {
			l.endClock();
			l.setOutOfMemory();
		}
		if (getIncumbent() == null)
			return null;
		else {
			ArrayList<SearchState> finalPath = getIncumbent().reconstructPath();
			return finalPath;
		}
	}

	public void cleanup() {
		return;
	}

}
