package org.cwilt.search.domains.hanoi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.cwilt.search.search.SearchState;

public class HanoiState extends SearchState {
	private static int highestBit(long v) {
		if (v == 0)
			return 0;
		int lg = 0;
		if ((v & (0xFFFFFFFF00000000L)) != 0) {
			lg += 32;
			v >>= 32;
		}
		if ((v & 0xFFFF0000L) != 0) {
			lg += 16;
			v >>= 16;
		}
		if ((v & 0xFF00L) != 0) {
			lg += 8;
			v >>= 8;
		}
		if ((v & 0xF0L) != 0) {
			lg += 4;
			v >>= 4;
		}
		if ((v & 0xCL) != 0) {
			lg += 2;
			v >>= 2;
		}
		if ((v & 0x2L) != 0) {
			lg += 1;
			v >>= 1;
		}
		return lg + 1;
	}

	private boolean canReverseMove(int startPeg, int endPeg) {
		if (startPeg == endPeg)
			return false;
		int movingDisk = topDisks[startPeg];
		if (movingDisk < topDisks[endPeg])
			return false;
		if (topDisks[startPeg] == 0)
			return false;
		return true;
	}

	private boolean canMove(int startPeg, int endPeg, int parentIndex) {
		if (startPeg == endPeg)
			return false;
		int movingDisk = topDisks[startPeg];
		if (movingDisk < topDisks[endPeg])
			return false;

		// int parentStartPeg = parentIndex / problem.nPegs;
		int parentEndPeg = parentIndex % problem.nPegs;

		if (parentEndPeg == startPeg)
			return false;

		if (lastOperator >= 0) {
			int inverse = this.inverseChild(lastOperator);
			if (getOperator(startPeg, endPeg) == inverse)
				return false;
			// TODO figure out which disk was the one that got moved last, and
			// if it
			// is getting moved again, ban the move.
		}
		if (topDisks[startPeg] == 0)
			return false;
		return true;
	}

	private int getOperator(int startPeg, int endPeg) {
		return startPeg * problem.nPegs + endPeg;
	}

	private final HanoiPegs pegs;
	private final int[] topDisks;
	private final HanoiProblem problem;
	private final int lastOperator;

	private HanoiState(HanoiProblem prob, HanoiPegs peg) {
		this.problem = prob;
		this.pegs = peg;
		this.topDisks = new int[prob.nPegs];
		for (int i = 0; i < prob.nPegs; i++) {
			this.topDisks[i] = highestBit(this.pegs.pegs[i]);
		}
		this.lastOperator = -1;
	}

	public static HanoiState makeGoal(HanoiProblem p) {
		long peg0 = 0L;
		for (int i = 0; i < p.nDisks; i++) {
			peg0 = 1 | (peg0 << 1L);
		}
		long pegArray[] = new long[p.nPegs];
		pegArray[0] = peg0;
		HanoiPegs pegs = new HanoiPegs(pegArray);
		return new HanoiState(p, pegs);
	}

	private HanoiState(HanoiState parent, int startPeg, int endPeg) {
		// this should not be checked in case you're doing a reverse expand
		// assert(startPeg != parent.lastPeg);
		this.topDisks = Arrays.copyOf(parent.topDisks, parent.topDisks.length);
		this.pegs = parent.pegs.clone();
		this.problem = parent.problem;
		this.lastOperator = getOperator(startPeg, endPeg);
		int diskID = topDisks[startPeg];
		pegs.movePeg(diskID, startPeg, endPeg);
		topDisks[startPeg] = highestBit(pegs.pegs[startPeg]);
		topDisks[endPeg] = diskID;
	}

	/**
	 * @param prob
	 * @param seed
	 * @return a randomly generated towers of hanoi state
	 */
	public static HanoiState randomState(HanoiProblem prob, int seed) {
		Random r = new Random(seed);
		long[] pegs = new long[prob.nPegs];
		for (int i = 0; i < prob.nDisks; i++) {
			long mask = 1 << i;
			int id = r.nextInt(prob.nPegs);
			pegs[id] = pegs[id] | mask;
		}
		return new HanoiState(prob, new HanoiPegs(pegs));
	}

	public HanoiState(String positions, HanoiProblem prob) {
		this.lastOperator = -1;
		this.problem = prob;
		long[] p = new long[prob.nPegs];
		String[] pp = positions.split("\\s+");
		assert (pp.length == prob.nDisks);
		assert (prob.nDisks < 64);
		this.topDisks = new int[prob.nPegs];
		for (int i = 0; i < prob.nDisks; i++) {
			long mask = 1 << i;
			int position = Integer.parseInt(pp[i]);
			p[position] = p[position] ^ mask;
			if (topDisks[position] < i + 1)
				topDisks[position] = i + 1;
		}
		this.pegs = new HanoiPegs(p);
	}

	public static class HanoiPegs implements Cloneable, Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5979581019297597106L;

		public void movePeg(int diskID, int start, int end) {
			long mask = 1L << (diskID - 1);
			pegs[end] = pegs[end] | mask;
			mask = ~mask;
			pegs[start] = pegs[start] & mask;
		}

		public HanoiPegs clone() {
			return new HanoiPegs(Arrays.copyOf(pegs, pegs.length));
		}

		private final long[] pegs;

		private HanoiPegs(long[] p) {
			this.pegs = p;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(pegs);
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
			HanoiPegs other = (HanoiPegs) obj;
			if (!Arrays.equals(pegs, other.pegs))
				return false;
			return true;
		}

		private static String printBinary(long id) {
			StringBuffer b = new StringBuffer();
			for (long i = 0; i < 64; i++) {
				long mask = 1L << i;
				if ((id & mask) == 0L)
					b.append("0");
				else
					b.append("1");
			}
			return b.toString();
		}

		public String toString() {
			StringBuffer b = new StringBuffer();
			for (int i = 0; i < pegs.length; i++) {
				b.append(printBinary(pegs[i]));
				b.append("\n");
			}
			return b.toString();
		}

	}

	@Override
	public ArrayList<Child> expand() {
		// System.out.println("EXPANDING:\n" + this);
		ArrayList<Child> children = new ArrayList<Child>();
		for (int i = 0; i < pegs.pegs.length; i++) {
			for (int j = 0; j < pegs.pegs.length; j++) {
				if (canMove(i, j, this.lastOperator)) {
					double cost = problem.getCost(topDisks[i]);
					HanoiState child = new HanoiState(this, i, j);
					System.out.println("generating:\n" + child);
					children.add(new Child(child, cost));
				}
			}
		}
		return children;
	}

	// goal: all disks on peg 0
	@Override
	public boolean isGoal() {
		for (int i = 1; i < pegs.pegs.length; i++) {
			if (pegs.pegs[i] != 0)
				return false;
		}
		System.out.println("found goal:\n" + this);
		return true;
	}

	@Override
	public Object getKey() {
		return pegs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pegs == null) ? 0 : pegs.hashCode());
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
		HanoiState other = (HanoiState) obj;
		if (pegs == null) {
			if (other.pegs != null)
				return false;
		} else if (!pegs.equals(other.pegs))
			return false;
		return true;
	}

	@Override
	public int lexOrder(SearchState s) {
		return 0;
	}

	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append(pegs.toString());
		for (int i = 0; i < topDisks.length; i++)
			b.append("Peg " + i + " " + topDisks[i] + "\n");
		b.append("---------\n");
		b.append(this.toStringFull());
		return b.toString();
	}

	private String toStringFull() {
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < this.problem.nPegs; i++)
			b.append(i + ":" + pegToString(this.pegs.pegs[i]) + "\n");
		return b.toString();
	}

	private String pegToString(long peg) {
		StringBuffer b = new StringBuffer();
		long mask = 1L;
		for (int i = 0; i < this.problem.nDisks; i++) {
			if (((peg & (mask << i)) >> i) == 1) {
				int disk_id = diskIdFromPegBitArrayIndex(i, this.problem.nDisks);
				b.append(" " + disk_id);
			}

		}
		return b.toString();
	}

	private int diskIdFromPegBitArrayIndex(int index, int n_disks) {
		return n_disks - index;
	}

	public int countDisksOnPeg(int pegID) {
		int count = 0;
		long mask = 1L;
		for (int i = 0; i < this.problem.nDisks; i++) {
			if (((this.pegs.pegs[pegID] & (mask << i)) >> i) == 1) {
				count += 1;
			}

		}
		return count;
	}

	public Object abstractState(int nDisks, int bottomDisk) {
		long mask = 0L;
		for (int i = 0; i < nDisks; i++) {
			mask = 1 | (mask << 1L);
		}
		mask = mask << bottomDisk;
		long[] p = new long[problem.nPegs];
		for (int i = 0; i < problem.nPegs; i++) {
			p[i] = (pegs.pegs[i] & mask) >> bottomDisk;
		}
		return new HanoiPegs(p);
	}

	private int findPeg(int pegID) {
		long mask = 1l << pegID;
		for (int i = 0; i < pegs.pegs.length; i++) {
			long peg = pegs.pegs[i];
			long anded = peg & mask;
			if ((anded) != 0l) {
				return i;
			}
		}
		assert (false);
		return -1;
	}

	public Object abstractState(int[] disks) {
		long[] p = new long[problem.nPegs];

		for (int i = 0; i < problem.nPegs; i++) {
			long mask = 1l;
			for (int disk : disks) {
				// find out which peg this disk is on
				int diskIndex = findPeg(disk);
				p[diskIndex] |= mask;
				mask <<= 1;
			}
		}
		return new HanoiPegs(p);
	}

	public ArrayList<Child> reverseExpand() {
		ArrayList<Child> children = new ArrayList<Child>();
		for (int i = 0; i < pegs.pegs.length; i++) {
			for (int j = 0; j < pegs.pegs.length; j++) {
				if (canReverseMove(i, j)) {
					double cost = problem.getCost(topDisks[i]);
					children.add(new Child(new HanoiState(this, i, j), cost));
				}
			}
		}
		return children;
	}

	@Override
	public int d() {
		return problem.calculateD(this);
	}

	@Override
	public double h() {
		return problem.calculateH(this);
	}

	@Override
	public int nChildren() {
		return problem.nPegs * problem.nPegs;
	}

	@Override
	public int inverseChild(int i) {
		return problem.getInverse(i);
	}

	@Override
	public double convertToChild(int index, int parentIndex) {

		int startPeg = index / problem.nPegs;
		int endPeg = index % problem.nPegs;
		if (!this.canMove(startPeg, endPeg, parentIndex)) {
			return -1;
		}

		int diskID = topDisks[startPeg];
		pegs.movePeg(diskID, startPeg, endPeg);
		topDisks[startPeg] = highestBit(pegs.pegs[startPeg]);
		topDisks[endPeg] = diskID;
		return 1;
	}
}
