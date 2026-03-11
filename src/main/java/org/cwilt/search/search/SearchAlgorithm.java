/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.search;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.IOException;

public abstract class SearchAlgorithm implements Runnable, Cloneable {

	public static void printPair(PrintStream ps, String name, Object value) {
		ps.println("#pair  \"" + name + "\"\t\"" + value + "\"");
	}

	protected final int id;

	protected String getName() {
		return this.getClass().getName() + id;
	}

	protected String paramString() {
		return "()";
	}

	public String toString() {
		return getName() + paramString();
	}

	protected final SearchState initial;

	public abstract void reset();

	public void stop() {
		System.err.println("memory warning");
		l.setOutOfMemory();
	}

	protected abstract void cleanup();

	private class InvalidClone extends RuntimeException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8164336788073310599L;

	}

	protected final void checkClone(String localPath) {
		if (localPath.compareTo(this.getClass().getCanonicalName()) != 0) {
			throw new InvalidClone();
		}
	}

	public abstract SearchAlgorithm clone();

	public void run() {
		solve();
		if (m != null && incumbent != null) {
			synchronized (m) {
				m.acceptSolution(incumbent);
			}
		}
	}

	protected SearchMaster m;

	public final void setSearchMaster(SearchMaster m) {
		this.m = m;
	}

	protected final org.cwilt.search.search.Limit l;

	public final org.cwilt.search.search.Limit getLimit() {
		return l;
	}

	protected static int idCounter = 0;
	protected SearchProblem prob;

	public SearchAlgorithm(SearchProblem prob, org.cwilt.search.search.Limit l) {
		this.id = idCounter;
		idCounter++;
		this.initial = prob.getInitial();
		this.prob = prob;
		this.goals = new ArrayList<Solution>(1);
		this.l = l;
	}

	private final List<Solution> goals;
	private Solution incumbent;

	protected void setIncumbent(Solution incumbent) {
		if (this.incumbent == null
				|| this.incumbent.getCost() > incumbent.getCost()) {
			goals.add(incumbent);
			this.incumbent = incumbent;
		}
	}

	protected final void setIncumbent(SearchNode current) {
		if (incumbent == null || incumbent.getCost() > current.getG())
			setIncumbent(new Solution(current, current.getG(), l.getDuration(),
					current.pathLength(), l.getExpansions(),
					l.getGenerations(), l.getDuplicates()));
	}

	public final Solution getIncumbent() {
		return incumbent;
	}

	public abstract List<SearchState> solve();

	public SearchState findFirstGoal() {
		throw new RuntimeException(this.getName()
				+ ": This Algorithm can't find first goal");
	}

	public final double getFinalCost() {
		if (incumbent == null)
			return Double.MAX_VALUE;
		else
			return incumbent.getCost();
	}

	public static String printPath(List<SearchState> path) {
		String toReturn = new String();
		Iterator<SearchState> i = path.iterator();
		while (i.hasNext()) {
			SearchState s = i.next();
			// toReturn = toReturn + s.toString() + "\n";
			toReturn = toReturn + s.toString() + " " + s.h() + " " + "\n";
		}
		return toReturn;
	}

	public void printExtraData(PrintStream ps) {

	}

	public void printSearchData(PrintStream ps) {
		ps.println("#cols  \"sol cost\"\t\"sol length\"\t\"nodes expanded\"\t\"nodes generated\"\t\"duplicates encountered\"\t\"raw cpu time\"");
		ps.println("inf\t0\t0\t0\t0\t0.000000");

		if (incumbent != null) {

			for (Solution s : goals) {
				ps.printf("%f\t%d\t%d\t%d\t%d\t%f\n", s.getCost(),
						s.getLength(), s.getExp(), l.getGenerations(),
						l.getDuplicates(),
						(((double) s.getFindTime()) / 1000.0));
			}
			printPair(ps, "found solution", "yes");
			printPair(ps, "final sol cost",
					new Double(incumbent.getCost()).toString());
			printPair(ps, "final sol length",
					new Integer(incumbent.getLength()));
			printSolution(ps, incumbent.reconstructPath());
		} else {
			printPair(ps, "found solution", "no");
			printPair(ps, "final sol cost", "inf");
			printPair(ps, "final sol length", "0");
		}
		double solTime = ((double) l.getDuration()) / (1000.0);
		printPair(ps, "total raw cpu time", new Double(solTime));
		printPair(ps, "peak virtual mem usage kb", new Long(Runtime
				.getRuntime().totalMemory()));
		printPair(ps, "total nodes reexpanded", new Long(l.getReExpansion()));
		printPair(ps, "number of duplicates found", new Long(l.getDuplicates()));
		printPair(ps, "total nodes expanded", new Long(l.getExpansions()));
		printPair(ps, "total nodes generated", new Long(l.getGenerations()));
		printPair(ps, "initial h", new Double(initial.h()));
		if (l.getOutOfMemory())
			printPair(ps, "out of memory", "true");
		printExtraData(ps);
		prob.printProblemData(ps);

		// SearchNode n = incumbent.getGoal();
		// while(n != null){
		// System.out.println("original h: " + n.getState().h());
		// System.out.println(n);
		// n = n.getParent();
		// }
	}

	private void printSolution(PrintStream ps, ArrayList<SearchState> soln) {
		ps.println(" ==== printing solution ====");
		ps.printf("%d states:\n", soln.size());
		for (SearchState s : soln) {
			ps.println(s);
		}
		ps.println(" ==== end solution ====");
	}

}
