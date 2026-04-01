package org.cwilt.search.domains.tiles;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

import org.cwilt.search.search.Limit;
import org.cwilt.search.search.SearchState;
public class TileProblem implements org.cwilt.search.search.SearchProblem, Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5805552095490989248L;
	private final char[] goal;
	private final char[] initial;
	private final double[][] mdArray;
	private final int[][] mdArrayD;
	private final int across;
	private final int down;
	private final BitSet canMove;
	private final TileAbstraction cost;
	private final TileAbstraction mdCost;
	private boolean calculateD;
	
	private final TilesPDB[] pdbs;

	public TileAbstraction getCost() {
		return cost;
	}

	public double getH(TileState state) {
	    double h = 0;
	    for(int i = 0; i < pdbs.length; i++){
	        h += pdbs[i].calculateH(state);
	    }
	    return h;
	}
    public int getD(TileState state) {
        int h = 0;
        for(int i = 0; i < pdbs.length; i++){
            h += pdbs[i].calculateD(state);
        }
        return h;
    }
	
	public int getAcross() {
		return across;
	}

	public int getDown() {
		return down;
	}

	private int getDistance(int position, int goal) {
		int posRow = position % across;
		int posCol = position / across;
		int goalRow = goal % across;
		int goalCol = goal / across;
		return Math.abs(posRow - goalRow) + Math.abs(posCol - goalCol);
	}
	
	public boolean canMove(int tileID) {
	    if(tileID == 255)
	        return true;
		return canMove.get(tileID);
	}

	public boolean isGoal(char[] a) {
		assert (a.length == goal.length);
		return Arrays.equals(a, goal);
	}

	public char[] copyArray() {
		return Arrays.copyOf(initial, initial.length);
	}

	public double mdIncrement(int tileID, int originalPos, int finalPos) {
	    //don't change the manhattan distance for these abstracted guys
	    if(tileID == 255)
	        return 0;
	    
		double oldMD = mdArray[tileID][finalPos];
		double newMD = mdArray[tileID][originalPos];
		return oldMD - newMD;
	}

	public int mddIncrement(int tileID, int originalPos, int finalPos) {
		int oldMD = mdArrayD[tileID][finalPos];
		int newMD = mdArrayD[tileID][originalPos];
		return oldMD - newMD;
	}
	
	public double mdFromScratch(TileState s1, TileState s2) {
		double value = 0;

		//cache where each tile is in state 1 so you don't have to look for it later.
		char[] locations = new char[across * down];
		for(char i = 0; i < across * down; i++){
			locations[s1.b.c[i]] = i;
		}
		
		for(int i = 0; i < across * down; i++){
			//don't process the blank tile.
			if(s2.b.c[i] == 0)
				continue;
			int initialColumn = i % across;
			int initialRow = i / across;
			char currentValue = s2.b.c[i];
			char finalIndex = locations[currentValue];
			assert(finalIndex >= 0);
			int finalColumn = finalIndex % across;
			int finalRow = finalIndex / across;
			double mdInt = Math.abs(finalColumn - initialColumn) + Math.abs(finalRow - initialRow);
			value = value + mdInt * this.cost.getCost(currentValue);
		}
		return value;
	}
	
	public boolean calcMD(){
	    return this.pdbs == null;
	}
		
	public double mdFromScratch(TileState s) {
		double md = 0;
		for (int ix = 0; ix < across * down; ix++) {
			int tileHere = s.b.c[ix];
			if (!cost.isAbstracted(tileHere)) {
				double price = mdArray[tileHere][ix];
				md = md + price;
			}
		}
		return md;
	}

	public int mddFromScratch(TileState s) {
		int md = 0;
		for (int ix = 0; ix < across * down; ix++) {
			int tileHere = s.b.c[ix];
			if (!mdCost.isAbstracted(tileHere)) {
				int price = mdArrayD[tileHere][ix];
				md = md + price;
			}
		}
		return md;
	}

	private void initMDArray() {
		for (int i = 0; i < across * down; i++) {
			mdArray[i] = new double[across * down];
			for (int j = 0; j < across * down; j++) {
				mdArray[i][j] = (getDistance(i, goal[j]) * cost.getCost(i));
			}
		}

		for (int i = 0; i < across * down; i++) {
			mdArrayD[i] = new int[across * down];
			for (int j = 0; j < across * down; j++) {
				mdArrayD[i][j] = (int) (getDistance(i, goal[j]) * mdCost
						.getCost(i));
			}
		}

	}

	/**
	 * Makes a goal
	 * @param across 
	 * @param down 
	 * @param cost 
	 */
	public TileProblem(int across, int down, String cost) {
		if (cost != null && cost.equals("inverse"))
			this.cost = TileAbstraction.makeInverse(across * down);
		else if (cost != null && cost.equals("reverseinverse"))
			this.cost = TileAbstraction.reverseInverse(across * down);
		else if(cost == null || cost.equals("unit"))
			this.cost = TileAbstraction.makeUnit(across * down);
		else
			throw new IllegalArgumentException(cost);
		
		this.pdbs = null;
		this.canMove = new BitSet();
		this.across = across;
		this.down = down;
		this.goal = new char[across * down];
		this.initial = new char[across * down];
		for (int i = 0; i < across * down; i++) {
			this.goal[i] = (char) i;
			this.initial[i] = (char) i;
			this.canMove.set(i);
		}
		this.mdCost = TileAbstraction.makeUnit(across * down);
		this.mdArray = new double[across * down][];
		this.mdArrayD = new int[across * down][];
		initMDArray();
		this.calculateD = false;
	}

	private static int swap(char[] ary, Random r) {
		int firstIX = r.nextInt(ary.length);
		int secondIX = r.nextInt(ary.length);

		int toReturn = 1;
		if (ary[firstIX] == 0)
			return 0;
		else if (ary[secondIX] == 0)
			return 0;
		else if (firstIX == secondIX)
			return 0;
		char temp = ary[firstIX];
		ary[firstIX] = ary[secondIX];
		ary[secondIX] = temp;
		return toReturn;
	}

	private static int swapZero(char[] ary, int ix) {
		int firstIX = ix;
		char temp = ary[firstIX];
		ary[firstIX] = ary[0];
		ary[0] = temp;
		return 1;
	}

	private static final boolean isBlack(int across, int cellIndex) {
		if (cellIndex == 0)
			return false;
		int column = cellIndex % across;
		int row = cellIndex / across;
		if (row % 2 == 0) {
			return (column % 2) == 0;
		} else {
			return (column % 2) != 0;
		}
	}

	public static final TileProblem random(int across, int down, int seed,
			String cost) {
		TileProblem toReturn = new TileProblem(across, down, cost);
		Random r = new Random(seed);
		{
			int i = 0;
			while (i < 4000) {
				i += swap(toReturn.initial, r);
			}
		}
		int blankIndex;
		blankIndex = r.nextInt(across * down);
		if (isBlack(across, blankIndex)) {
			int i = 0;
			while (i < 1) {
				i += swap(toReturn.initial, r);
			}
		}

		swapZero(toReturn.initial, blankIndex);

		return toReturn;
	}

	/**
	 * @param path
	 *            string for the path from which to load the tile problem
	 * @param cost 
	 * @param pdbArgs 
	 * @throws IOException
	 *             if the path can't be read from
	 * @throws ClassNotFoundException 
	 */
	public TileProblem(String path, String cost, String[] pdbArgs) throws IOException, ClassNotFoundException {
		this.canMove = new BitSet();

		FileInputStream fs = new FileInputStream(path);
		DataInputStream ds = new DataInputStream(fs);
		BufferedReader br = new BufferedReader(new InputStreamReader(ds));

		// reads the map size
		String nextLine = br.readLine();
		String[] s = nextLine.split("\\s+");
		assert (s.length == 2);
		down = Integer.parseInt(s[0]);
		across = Integer.parseInt(s[1]);
		this.goal = new char[across * down];
		this.initial = new char[across * down];

		if (cost != null && cost.equals("inverse"))
			this.cost = TileAbstraction.makeInverse(across * down);
		else if (cost != null && cost.equals("inversesquare"))
			this.cost = TileAbstraction.makeInverseSquare(across * down);
		else if (cost != null && cost.equals("reverseinverse"))
			this.cost = TileAbstraction.reverseInverse(across * down);
		else if (cost != null && cost.equals("reverseinversesquare"))
			this.cost = TileAbstraction.reverseInverseSquare(across * down);
		else if (cost != null && cost.equals("sqrt"))
			this.cost = TileAbstraction.makeSqrt(across * down);
		else if(cost == null || cost.equals("unit"))
			this.cost = TileAbstraction.makeUnit(across * down);
		else{
			br.close();
			throw new IllegalArgumentException(cost);
		}

		this.mdCost = TileAbstraction.makeUnit(across * down);

		br.readLine();

		for (int i = 0; i < across * down; i++) {
			String nextInt = br.readLine();
			int ix = Integer.parseInt(nextInt);
			this.initial[ix] = (char) i;
			this.canMove.set(i);
		}
		br.readLine();
		for (int i = 0; i < across * down; i++) {
			String nextInt = br.readLine();
			this.goal[i] = (char) Integer.parseInt(nextInt);
		}
		br.close();
		
		
		/**
		 * Initalize the stuff to calculate the Manhattan Distance
		 * 
		 * Skip this stuff if using pattern databases
		 */
		if(pdbArgs == null){
		    this.mdArray = new double[across * down][];
		    this.mdArrayD = new int[across * down][]; 
		    initMDArray();
		    this.pdbs = null;
		} else {
	        this.mdArray = null;
	        this.mdArrayD = null;
		    this.pdbs = new TilesPDB[pdbArgs.length];
		    for(int i = 0; i < pdbArgs.length; i++){
		        pdbs[i] = TilesPDB.readPDB(pdbArgs[i]);
		    }
		}
	}

	public double getCost(int tileID) {
		return cost.getCost(tileID);
	}

	protected TileProblem(TileProblem prob, TileAbstraction abs, BitSet canMove) {
		this.calculateD = prob.calculateD;
		this.cost = abs;
		this.canMove = canMove;
		this.across = prob.across;
		this.down = prob.down;
		this.mdArray = new double[across * down][];
		this.mdArrayD = new int[across * down][];
		initMDArray();
		this.initial = prob.initial.clone();
		this.goal = prob.goal.clone();
		this.pdbs = null;
		this.mdCost = TileAbstraction.makeUnit(across * down);
	}

	public static String printTileArray(char[] a, int across, int down) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < down; i++) {
			for (int j = 0; j < across; j++) {
				int pos = i * across + j;
				Integer val = (int) a[pos];
				buf.append(val.toString());
				buf.append("\t");
			}
			buf.append("\n");
		}
		return buf.toString();
	}

	public String toString() {
		// return printTileArray(initial, across, down);
		// return printMDArray() + "\n" +
		return printTileArray(initial, across, down);
	}

	public String printMDArray() {
		int size = across * down;
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				Double val = (Double) mdArray[i][j];
				buf.append(val.toString());
				buf.append("\t");
			}
			buf.append("\n");
		}
		return buf.toString();

	}

	public static void main(String[] args) throws IOException {
		for (int i = 1; i < 100; i++) {
			TileProblem p = TileProblem.random(4, 4, i * 1900000, "unit");
			TileSolver a = new TileSolver(new Limit(), p);
			ArrayList<SearchState> sol = a.solve();
			a.printSearchData(System.out);
			assert (sol != null);
		}
	}

	public void lockTile(Integer nextTile) {
		canMove.clear(nextTile);
	}

	@Override
	public ArrayList<SearchState> getGoals() {
		ArrayList<SearchState> goals = new ArrayList<SearchState>();
		goals.add(new TileState(this, true));
		return goals;
	}

	public SearchState getGoal() {
		return new TileState(this, true);
	}

	@Override
	public SearchState getInitial() {
		return new TileState(this, false);
	}

	@Override
	public void setCalculateD() {
		this.calculateD = true;
	}

	public boolean calculateD() {
		return calculateD;
	}
	@Override
	public void printProblemData(PrintStream ps) {
	}

}
