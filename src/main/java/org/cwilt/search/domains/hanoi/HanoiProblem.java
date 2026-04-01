package org.cwilt.search.domains.hanoi;

/**
 * 
 * --time 600.000000 --probargs /home/aifs2/cmo66/cjava/santa/hanoipdb9  --type hanoi --problem /home/aifs2/group/data/hanoi/instance/4/12/2 --alg astar --rest 0
 * 
 * This problem expects probargs, which are the pattern databases to use, in increasing order.
 * 
 */

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.cwilt.search.search.SearchState;
import org.cwilt.search.search.SearchState.Child;
import org.cwilt.search.utils.TemporaryLoadAndWritePath;

public class HanoiProblem implements org.cwilt.search.search.SearchProblem {
	final int nDisks;
	final int nPegs;
	protected final HanoiState initial;

	protected final HanoiPDB[] pdbs;
	protected final HanoiAbstraction[] abs;
	protected final int[] inverses;

	protected final double[] cost;

	private void initInverses() {
		for (int i = 0; i < inverses.length; i++) {
			int start = i % nPegs;
			int end = i / nPegs;
			inverses[i] = end + nPegs * start;
		}

		// System.err.println(java.util.Arrays.toString(inverses));
	}

	public final static HanoiProblem randomProblem(HanoiProblem parent, int seed) {
		System.out.println("HanoiProblem: randomProblem");
		return new HanoiProblem(parent, HanoiState.randomState(parent, seed));
	}

	public static double[] initCost(String costs, int length) {
		double cost[] = new double[length];
		if (costs == null || costs.equals("unit")) {
			for (int i = 0; i < cost.length; i++) {
				cost[i] = 1;
			}
		} else if (costs.equals("square")) {
			for (int i = 0; i < cost.length; i++) {
				cost[cost.length - i - 1] = (i + 1) * (i + 1);
			}
		} else if (costs.equals("reversesquare")) {
			for (int i = 0; i < cost.length; i++) {
				cost[i] = (i + 1) * (i + 1);
			}
		} else {
			throw new RuntimeException("unacceptable cost:<" + costs + ">");
		}
		return cost;
	}

	public double getCost(int disk) {
		assert (disk <= cost.length);
		assert (disk > 0);
		return cost[disk - 1];
	}

	public HanoiProblem(int nPegs, int nDisks, double[] costs, String[] pdbArgs)
			throws ClassNotFoundException {
		System.out.println("HanoiProblem(int nPegs, int nDisks, double[] costs, String[] pdbArgs)");
		this.nPegs = nPegs;
		this.nDisks = nDisks;
		initial = HanoiState.makeGoal(this);
		this.pdbs = new HanoiPDB[pdbArgs.length];
		this.abs = new HanoiAbstraction[pdbArgs.length];
		this.inverses = new int[nPegs * nPegs];
		this.initInverses();
		this.cost = costs;

		assert (this.nDisks == costs.length);
		initPDBS(pdbArgs);
	}

	public HanoiProblem(int nPegs, int nDisks, double[] costs,
			HanoiPDB[] pdbArgs) {
		System.out.println("HanoiProblem(int nPegs, int nDisks, double[] costs, HanoiPDB[] pdbArgs)");
		this.nPegs = nPegs;
		this.nDisks = nDisks;
		initial = HanoiState.makeGoal(this);
		this.abs = new HanoiAbstraction[pdbArgs.length];
		this.inverses = new int[nPegs * nPegs];
		this.initInverses();
		this.cost = costs;

		assert (this.nDisks == costs.length);
		this.pdbs = pdbArgs;

		int bottomDisk = 0;
		for (int i = 0; i < pdbArgs.length; i++) {
			abs[i] = new HanoiAbstraction(bottomDisk, pdbs[i].getnDisks());
			bottomDisk += abs[i].getNDisks();
		}

	}

	public HanoiProblem(HanoiProblem p, HanoiState initial) {
		System.out.println("HanoiProblem((HanoiProblem p, HanoiState initial)");
		this.nDisks = p.nDisks;
		this.nPegs = p.nPegs;
		this.initial = initial;
		this.pdbs = p.pdbs;
		this.abs = p.abs;

		this.cost = p.cost;
		this.inverses = new int[nPegs * nPegs];
		this.initInverses();
	}

	// THIS ONE
	public HanoiProblem(String path, String costString, String[] pdbArgs)
			throws IOException, ClassNotFoundException {
		System.out.println("HanoiProblem(String path, String costString, String[] pdbArgs)");
		System.out.println("path: " + path);
		System.out.println("costString: " + costString);
		System.out.println("pdbArgs: " + Arrays.toString(pdbArgs));
		FileInputStream fs = new FileInputStream(path);
		DataInputStream ds = new DataInputStream(fs);
		BufferedReader br = new BufferedReader(new InputStreamReader(ds));
		// use simple heuristic instead of pdb
		if (pdbArgs.length == 1 && Integer.parseInt(pdbArgs[0]) == 0) {
			System.out.println("HanoiProblem.pdbs <- null");
			pdbs = null;
			abs = null;
		} else {
			System.out.println("HanoiProblem.pdbs <- HanoiPDB[" + pdbArgs.length + "]");
			pdbs = new HanoiPDB[pdbArgs.length];
			abs = new HanoiAbstraction[pdbArgs.length];

			initPDBS(pdbArgs);
		}

		String nextLine = br.readLine();
		this.nPegs = Integer.parseInt(nextLine);
		nextLine = br.readLine();
		this.nDisks = Integer.parseInt(nextLine);
		nextLine = br.readLine();
		br.close();
		String pegPositions = nextLine;
		initial = new HanoiState(pegPositions, this);

		this.cost = initCost(costString, nDisks);

		this.inverses = new int[nPegs * nPegs];
		this.initInverses();
	}

	public HanoiProblem(String path, String costString, HanoiPDB[] pdbs)
			throws IOException {
		System.out.println("HanoiProblem(String path, String costString, HanoiPDB[] pdbs)");
		FileInputStream fs = new FileInputStream(path);
		DataInputStream ds = new DataInputStream(fs);
		BufferedReader br = new BufferedReader(new InputStreamReader(ds));
		this.pdbs = pdbs;
		abs = new HanoiAbstraction[0];

		String nextLine = br.readLine();
		this.nPegs = Integer.parseInt(nextLine);
		nextLine = br.readLine();
		this.nDisks = Integer.parseInt(nextLine);
		nextLine = br.readLine();
		br.close();
		String pegPositions = nextLine;
		initial = new HanoiState(pegPositions, this);

		this.cost = initCost(costString, nDisks);

		this.inverses = new int[nPegs * nPegs];
		this.initInverses();
	}

	private DisjointHanoiPDB disjointPDB = null;

	private void initPDBS(String[] pdbArgs) throws ClassNotFoundException {
		int bottomDisk = 0;

		if (pdbArgs.length == 0)
			return;

		try {
			int skipDisks = Integer.parseInt(pdbArgs[0]);
			abs[0] = null;
			pdbs[0] = null;
			bottomDisk += skipDisks;
		} catch (NumberFormatException ex) {
			Object newPDB = HanoiPDB.readPDB(pdbArgs[0]);
			if (newPDB instanceof DisjointHanoiPDB) {
				disjointPDB = (DisjointHanoiPDB) newPDB;
				return;
			}
			pdbs[0] = (HanoiPDB) newPDB;
			abs[0] = new HanoiAbstraction(bottomDisk, pdbs[0].getnDisks());
			bottomDisk += abs[0].getNDisks();
		}

		for (int i = 1; i < pdbArgs.length; i++) {
			// check if this pattern database is a number
			try {
				int skipDisks = Integer.parseInt(pdbArgs[i]);
				abs[i] = null;
				pdbs[i] = null;
				bottomDisk += skipDisks;
			} catch (NumberFormatException ex) {
				pdbs[i] = (HanoiPDB) HanoiPDB.readPDB(pdbArgs[i]);
				abs[i] = new HanoiAbstraction(bottomDisk, pdbs[i].getnDisks());
				bottomDisk += abs[i].getNDisks();
			}
		}
	}

	public String toString() {
		return "pegs: " + nPegs + " disks " + nDisks + "\n" + initial;
	}

	public HanoiState getInitial() {
		return initial;
	}

	public static void main(String[] s) throws IOException,
			ClassNotFoundException {
		String[] pdbString = new String[0];
		HanoiProblem p = new HanoiProblem(TemporaryLoadAndWritePath.getTempPath()
				+ "/hanoidata/7_0", null, pdbString);
		System.out.println(p);
		ArrayList<Child> children = p.initial.expand();
		for (Child c : children) {
			System.err.println(c.child);
		}
	}

	public double calculateH(HanoiState hanoiState) {
		if (disjointPDB != null) {
			return disjointPDB.getH(hanoiState);
		}

		double h = 0;
		if (pdbs != null) {
			for (int i = 0; i < pdbs.length; i++) {
				if (abs[i] == null)
					continue;
				Object abstractedState = abs[i].abstractState(hanoiState);
				h += pdbs[i].getH(abstractedState);
			}
		} else {
			// simple heuristic: number of disks not on goal peg
			// TODO: provide for non-unit cost
			h = this.calculateD(hanoiState);
		}
		return h;
	}

	public int calculateD(HanoiState hanoiState) {
		System.out.println("HanoiProblem: calculateD");
		if (disjointPDB != null) {
			return disjointPDB.getD(hanoiState);
		}
		int h = 0;
		if (this.pdbs != null) {
			for (int i = 0; i < pdbs.length; i++) {
				if (abs[i] == null)
					continue;
				Object abstractedState = abs[i].abstractState(hanoiState);
				h += pdbs[i].getD(abstractedState);
			}

			return h;
		} else {
			int n_disks_on_goal_peg = hanoiState.countDisksOnPeg(0);
			int d = this.nDisks - n_disks_on_goal_peg;
			System.out.println("HanoiProblem: calculateD -- with simple heuristic = " + d);
			return d;
		}
	}

	@Override
	public SearchState getGoal() {
		return HanoiState.makeGoal(this);
	}

	@Override
	public ArrayList<SearchState> getGoals() {
		ArrayList<SearchState> toReturn = new ArrayList<SearchState>();
		toReturn.add(getGoal());
		return toReturn;
	}

	@Override
	public void setCalculateD() {
	}

	public void printProblemData(PrintStream p) {

	}

	public int getInverse(int index) {
		return inverses[index];
	}

}
