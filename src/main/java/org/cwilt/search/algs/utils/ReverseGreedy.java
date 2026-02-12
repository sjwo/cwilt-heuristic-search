package org.cwilt.search.algs.utils;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Iterator;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;import org.cwilt.search.utils.basic.MinHeap;
public class ReverseGreedy extends SearchAlgorithm {
	private final HashMap<Object, SearchNode> closed;
	private final MinHeap<SearchNode> open;

	public HashMap<Object, SearchNode> getClosed() {
		return closed;
	}

	// for each LM, record its hwm, and its size (num nodes)
	private static final class NodeReservoir {
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + count;
			long temp;
			temp = Double.doubleToLongBits(highwater);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		// if two LMs have same count (size) and hwm, then consider them to be equal
		//
		// is equality used for deduplication? not during recording, because each is added to an ArrayList, not a hashtable.
		//
		// what about in post-processing? yes, deduped in post-processing based on hashCode and equals: same hwm and same count.
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NodeReservoir other = (NodeReservoir) obj;
			if (count != other.count)
				return false;
			if (Double.doubleToLongBits(highwater) != Double
					.doubleToLongBits(other.highwater))
				return false;
			return true;
		}

		private int count;
		public final double highwater;

		public NodeReservoir(double hw) {
			this.highwater = hw;
			this.count = 0;
		}

		public void incrementNodes() {
			count++;
		}

		@Override
		public String toString() {
			StringBuffer b = new StringBuffer();
			b.append(highwater);
			b.append(" ");
			b.append(count);
			return b.toString();
		}
	}

	private NodeReservoir current;
	private final ArrayList<NodeReservoir> nodeReservoirs;

	public ReverseGreedy(SearchProblem prob, Limit l) {
		super(prob, l);
		this.closed = new HashMap<Object, SearchNode>();
		this.open = new MinHeap<SearchNode>(new SearchNode.HComparator());
		this.errorPairs = new HashMap<ErrorPair, AtomicInteger>();
		this.nodeReservoirs = new ArrayList<NodeReservoir>();
	}

	@Override
	public void reset() {
		open.clear();
		closed.clear();
	}

	@Override
	protected void cleanup() {
		this.reset();
	}

	@Override
	public SearchAlgorithm clone() {
		return new ReverseGreedy(prob, l.clone());
	}

	Stack<HighwaterMark> highwaterStack = new Stack<HighwaterMark>();

	private static final class HighwaterMark {
		final double mark;
		final long exp;

		public HighwaterMark(double m, long e) {
			this.mark = m;
			this.exp = e;
		}
	}

	private void evaluateNode(SearchNode n) {
		// hwStack empty: initialization
		// stack < n: not in LM
		if (highwaterStack.isEmpty() || highwaterStack.peek().mark < n.getH()) {
			highwaterStack.push(new HighwaterMark(n.getH(), this.l
					.getExpansions()));
			hOutside.put(n.getH(), new AtomicInteger(0));
			current = null;
			// stack > n: in LM
		} else if (highwaterStack.peek().mark > n.getH()) {
			if (current == null) {
				current = new NodeReservoir(highwaterStack.peek().mark);
				nodeReservoirs.add(current);
			}
			current.incrementNodes();
			// plateau
		} else if (highwaterStack.peek().mark == n.getH()) {
			// want to ignore the nodes that are on the same level as the
			// current highwater mark.
			current = null;
		}
		{
			// only record relative difference between h and hwm [0, inf)
 			double hw = highwaterStack.peek().mark;
			Double hd = hw - n.getH();
			AtomicInteger i = hDiffCounts.get(hd);
			if (i == null) {
				hDiffCounts.put(hd, new AtomicInteger(1));
			} else {
				i.incrementAndGet();
			}
		}
		{
			Double h = n.getH();
			AtomicInteger i = hOutside.get(h);
			// if have not yet recorded this non-LM h value, record it
			if (i == null) {
				i = new AtomicInteger(0);
				hOutside.put(h, i);
			}
			double hw = highwaterStack.peek().mark;
			// ???????
			if (h < hw) {
				i.incrementAndGet();
			}
		}

		{
			Double h = n.getH();
			// if have not yet recorded this h value (among all h values, LM and non-LM), record it
			AtomicInteger i = hCounts.get(h);
			if (i == null) {
				hCounts.put(h, new AtomicInteger(1));
			} else {
				i.incrementAndGet();
			}
		}
		{
			// probably for computing heuristic error
			ErrorPair epNew = new ErrorPair(n.getH(),
					highwaterStack.peek().mark);
			AtomicInteger ct = errorPairs.get(epNew);
			if (ct == null) {
				errorPairs.put(epNew, new AtomicInteger(1));
			} else {
				ct.incrementAndGet();
			}
		}
	}

	private final HashMap<ErrorPair, AtomicInteger> errorPairs;

	private static final class ErrorPair {
		private final double h, highwater;

		public ErrorPair(double h, double hw) {
			this.h = h;
			this.highwater = hw;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(h);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(highwater);
			result = prime * result + (int) (temp ^ (temp >>> 32));
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
			ErrorPair other = (ErrorPair) obj;
			if (Double.doubleToLongBits(h) != Double.doubleToLongBits(other.h))
				return false;
			if (Double.doubleToLongBits(highwater) != Double
					.doubleToLongBits(other.highwater))
				return false;
			return true;
		}
	}

	HashMap<Double, AtomicInteger> hDiffCounts = new HashMap<Double, AtomicInteger>();
	HashMap<Double, AtomicInteger> hCounts = new HashMap<Double, AtomicInteger>();
	HashMap<Double, AtomicInteger> hOutside = new HashMap<Double, AtomicInteger>();

	@Override
	public ArrayList<SearchState> solve() {

		// search backwards from ALL goals
		ArrayList<SearchState> goals = prob.getGoals();
		for (SearchState goal : goals) {
			SearchNode first = SearchNode.makeInitial(goal);
			open.add(first);
			closed.put(first.getState().getKey(), first);
		}

		try {
			while (!open.isEmpty()) {
				SearchNode next = open.poll();
				evaluateNode(next);
				ArrayList<? extends SearchNode> children = next.reverseExpand();
				l.incrExp();
				for (SearchNode n : children) {
					// check closed at generation; if not in closed, add to closed and to open
					SearchNode incumbent = closed.get(n.getState().getKey());
					if (incumbent == null) {
						open.add(n);
						closed.put(n.getState().getKey(), n);
					}
				}
			}
		} catch (OutOfMemoryError ex) {
		}

		this.reset();


		return null;
	}

	private static final class HighwaterError implements
			Comparable<HighwaterError> {
		private final double error;
		private final int count;

		public HighwaterError(double e, int c) {
			this.error = e;
			this.count = c;
		}

		@Override
		public int compareTo(HighwaterError arg0) {
			if (this.error < arg0.error)
				return -1;
			else if (this.error > arg0.error)
				return 1;
			else
				return 0;
		}

	}

	@Override
	public void printExtraData(PrintStream ps) {
		for (HighwaterMark m : highwaterStack) {
			StringBuffer b = new StringBuffer();
			b.append(m.mark);
			b.append(" ");
			b.append(m.exp);
			SearchAlgorithm.printPair(ps, "highwater_pair", b.toString());
		}
		Iterator<Map.Entry<Double, AtomicInteger>> iter = hDiffCounts
				.entrySet().iterator();
		ArrayList<HighwaterError> errors = new ArrayList<HighwaterError>(
				hDiffCounts.size());
		while (iter.hasNext()) {
			Map.Entry<Double, AtomicInteger> n = iter.next();
			errors.add(new HighwaterError(n.getKey(), n.getValue().get()));
		}
		Collections.sort(errors);
		int cumulative = 0;
		for (HighwaterError e : errors) {
			StringBuffer b = new StringBuffer();
			b.append(e.error);
			b.append(" ");
			cumulative += e.count;
			b.append(cumulative);
			SearchAlgorithm.printPair(ps, "highwater_error", b.toString());
		}

		ArrayList<HighwaterError> hs = new ArrayList<HighwaterError>(
				hCounts.size());
		Iterator<Map.Entry<Double, AtomicInteger>> hiter = hCounts.entrySet()
				.iterator();
		while (hiter.hasNext()) {
			Map.Entry<Double, AtomicInteger> n = hiter.next();
			hs.add(new HighwaterError(n.getKey(), n.getValue().get()));
		}
		Collections.sort(hs);
		cumulative = 0;
		for (HighwaterError e : hs) {
			StringBuffer b = new StringBuffer();
			StringBuffer b2 = new StringBuffer();
			b.append(e.error);
			b.append(" ");
			cumulative += e.count;
			b.append(cumulative);

			b2.append(e.error);
			b2.append(" ");
			b2.append(e.count);

			SearchAlgorithm.printPair(ps, "h", b.toString());
			SearchAlgorithm.printPair(ps, "h_total", b2.toString());
		}
		Iterator<Map.Entry<ErrorPair, AtomicInteger>> i = errorPairs.entrySet()
				.iterator();
		while (i.hasNext()) {
			Map.Entry<ErrorPair, AtomicInteger> e = i.next();
			StringBuffer b = new StringBuffer();
			b.append(e.getKey().h);
			b.append(" ");
			b.append(e.getKey().highwater - e.getKey().h);
			b.append(" ");
			b.append(e.getValue());
			SearchAlgorithm.printPair(ps, "highwater_triple", b.toString());
		}

		int maxReservoir = 0;
		HashMap<NodeReservoir, AtomicInteger> res = new HashMap<NodeReservoir, AtomicInteger>();
		for (NodeReservoir n : nodeReservoirs) {
			// deduplicate LMs that have same hwm and count
			//
			// only way to know different is to compare their states, though
			AtomicInteger c = res.get(n);
			if (c == null) {
				c = new AtomicInteger(0);
				res.put(n, c);
			}
			c.incrementAndGet();

			if (n.count > maxReservoir) {
				maxReservoir = n.count;
			}
		}

		Iterator<Map.Entry<NodeReservoir, AtomicInteger>> nri = res.entrySet()
				.iterator();
		while (nri.hasNext()) {
			StringBuffer b = new StringBuffer();
			Map.Entry<NodeReservoir, AtomicInteger> next = nri.next();
			b.append(next.getKey());
			b.append(" ");
			b.append(next.getValue());
			SearchAlgorithm.printPair(ps, "reservoir", b.toString());
		}

		SearchAlgorithm.printPair(ps, "max_reservoir",
				new Integer(maxReservoir));

		hs.clear();
		hiter = hOutside.entrySet().iterator();
		while (hiter.hasNext()) {
			Map.Entry<Double, AtomicInteger> n = hiter.next();
			hs.add(new HighwaterError(n.getKey(), n.getValue().get()));
		}
		Collections.sort(hs);
		for (HighwaterError e : hs) {
			StringBuffer b = new StringBuffer();
			b.append(e.error);
			b.append(" ");
			b.append(e.count);
			SearchAlgorithm.printPair(ps, "h_outside", b.toString());
		}

	}

}
