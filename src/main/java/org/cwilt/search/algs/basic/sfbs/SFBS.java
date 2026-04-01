package org.cwilt.search.algs.basic.sfbs;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import org.cwilt.search.algs.basic.bestfirst.AStarNoDD;
import org.cwilt.search.algs.basic.bestfirst.MultistartAStar;
import org.cwilt.search.algs.basic.bestfirst.MultistartAStar.MultistartSearchProblem;
import org.cwilt.search.search.Limit;
import org.cwilt.search.search.SearchAlgorithm;
import org.cwilt.search.search.SearchProblem;
import org.cwilt.search.search.SearchState;
import org.cwilt.search.search.SearchState.Child;
/**
 * Single Frontier Bidirectional Search
 * 
 * @author Christopher Wilt
 * 
 */

public abstract class SFBS extends org.cwilt.search.search.SearchAlgorithm {
	private static final class BestG {
		public double bestG;
		@SuppressWarnings("unused")
		public final SearchState state;
		@SuppressWarnings("unused")
		public SearchState parent;
		public ArrayList<Child> children;
		
		public BestG(SearchState state, SearchState parent, double bestG){
			this.parent = parent;
			this.bestG = bestG;
			this.state = state;
		}
	}

	protected final boolean lite;
	private final HashMap<Object, BestG> fExpCache;
	private final HashMap<Object, BestG> bExpCache;

	public ArrayList<Child> reverseExpand(double currentG, SearchState n) {
		BestG bg = bExpCache.get(n.getKey());
		assert (bg != null);

		if (currentG > bg.bestG) {
			// this expansion of the node is junk
			return null;
		}

		if (bg.children == null) {
			bg.children = n.reverseExpand();
			l.incrExp();
			l.incrGen(bg.children.size());
			ListIterator<Child> childIter = bg.children.listIterator();
			while (childIter.hasNext()) {
				Child c = childIter.next();
				double childG = currentG + c.transitionCost;
				BestG childBestG = bExpCache.get(c.child.getKey());
				if (childBestG == null) {
					childBestG = new BestG(c.child, n, childG);
					bExpCache.put(c.child.getKey(), childBestG);
				}
				//this version of the child is better than the previous one
				if(childG < childBestG.bestG){
					childBestG.bestG = childG;
					childBestG.children = null;
					childBestG.parent = n;
				}
				//this version of the child is worse, so it is just junk
				if(childG > childBestG.bestG)
					childIter.remove();
			}
		}

		return bg.children;
	}

	public ArrayList<Child> expand(double currentG, SearchState n) {

		BestG bg = fExpCache.get(n.getKey());
		assert (bg != null);

		if (currentG > bg.bestG) {
			// this expansion of the node is junk
			return null;
		}

		if (bg.children == null) {
			bg.children = n.expand();
			l.incrExp();
			l.incrGen(bg.children.size());
			ListIterator<Child> childIter = bg.children.listIterator();
			while (childIter.hasNext()) {
				Child c = childIter.next();
				double childG = currentG + c.transitionCost;
				BestG childBestG = fExpCache.get(c.child.getKey());
				if (childBestG == null) {
					childBestG = new BestG(c.child, n, childG);
					fExpCache.put(c.child.getKey(), childBestG);

				}
				//this version of the child is better than the previous one
				if(childG < childBestG.bestG){
					childBestG.bestG = childG;
					childBestG.children = null;
					childBestG.parent = n;
				}
				//this version of the child is worse, so it is just junk
				if(childG > childBestG.bestG)
					childIter.remove();
			}
		}

		return bg.children;
	}

	protected abstract class SFBSState extends org.cwilt.search.search.SearchState {
		
		protected final double fGValue;
		protected final double bGValue;
		
		public SFBSState(SearchState start, SearchState goal, double fg, double bg) {
			this.start = start;
			this.goal = goal;
			this.fGValue = fg;
			this.bGValue = bg;
		}

		public String toString() {
			return "start: " + start + "\ngoal: " + goal;
		}

		public final SearchState start, goal;

		@Override
		public ArrayList<Child> reverseExpand() {
			throw new UnsupportedOperationException();
		}

		@Override
		public double h() {
			return start.distTo(goal);
		}

		@Override
		public int d() {
			return 0;
		}

		@Override
		public boolean isGoal() {
			return start.equals(goal);
		}

		@Override
		public Object getKey() {
			return this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((goal == null) ? 0 : goal.hashCode());
			result = prime * result + ((start == null) ? 0 : start.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SFBSState other = (SFBSState) obj;
			if (goal == null) {
				if (other.goal != null)
					return false;
			} else if (!goal.equals(other.goal))
				return false;
			if (start == null) {
				if (other.start != null)
					return false;
			} else if (!start.equals(other.start))
				return false;
			return true;
		}

		@Override
		public int lexOrder(SearchState s) {
			return 0;
		}

	}

	protected final SearchState start;
	protected final ArrayList<SearchState> goals;

	protected abstract SearchProblem getProblem();
	
	public SFBS(SearchProblem prob, Limit l, boolean lite) {
		super(prob, l);
		this.lite = lite;
		this.fExpCache = new HashMap<Object, BestG>();
		this.bExpCache = new HashMap<Object, BestG>();

		this.start = prob.getInitial();
		this.goals = prob.getGoals();
		
		if(lite)
			this.a = new AStarNoDD(this.getProblem(), l.clone());
		else
			this.a = new MultistartAStar((MultistartSearchProblem) this.getProblem(), l.clone());
	}

	private final SearchAlgorithm a;

	@Override
	public void reset() {
		a.reset();
	}

	@Override
	protected void cleanup() {
		this.a.reset();
	}

	@Override
	public List<SearchState> solve() {
		l.startClock();
		fExpCache.put(start.getKey(), new BestG(start, null, 0));
		for(SearchState s : goals)
			bExpCache.put(s.getKey(), new BestG(s,  null, 0));
		
		List<SearchState> solution = a.solve();

		l.endClock();
		// have to copy a's solution information into this guy's solution
		// information

		// a.printSearchData(System.err);
		System.err.flush();

		this.setIncumbent(a.getIncumbent());

		return solution;
	}
	
	@Override
	public void printExtraData(PrintStream ps){
		SearchAlgorithm.printPair(ps, "metagen", a.getLimit().getGenerations());
		SearchAlgorithm.printPair(ps, "metaexp", a.getLimit().getExpansions());
	}
}
