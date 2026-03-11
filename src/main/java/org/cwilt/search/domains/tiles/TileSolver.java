package org.cwilt.search.domains.tiles;
import java.io.IOException;
import java.util.ArrayList;

import org.cwilt.search.algs.basic.EnforcedHillClimbing;
import org.cwilt.search.algs.basic.bestfirst.AStar;
import org.cwilt.search.search.Limit;
import org.cwilt.search.search.SearchAlgorithm;
import org.cwilt.search.search.SearchProblem;
import org.cwilt.search.search.SearchState;
import org.cwilt.search.search.Solution;
import org.cwilt.search.utils.TemporaryLoadAndWritePath;
public class TileSolver extends SearchAlgorithm {

	private final ArrayList<ArrayList<Integer>> solveOrder;

	private TileState initialState;

	public TileSolver(Limit l, SearchProblem initial) {
		super(initial, l);
		if (!(super.initial instanceof TileState)) {
			System.err.println("Tile Solver can only solve tile problems");
		}
		this.initialState = (TileState) initial.getInitial();
		solveOrder = new ArrayList<ArrayList<Integer>>();
		this.setSolveOrder();
	}

	@Override
	protected void cleanup() {

	}

	@Override
	public SearchAlgorithm clone() {
		super.checkClone(TileSolver.class.getCanonicalName());
		return new TileSolver(l.clone(), prob);
	}

	@Override
	public SearchState findFirstGoal() {
		return null;
	}

	@Override
	public void reset() {

	}

	@Override
	public ArrayList<SearchState> solve() {
		l.startClock();
		
		double cost = 0;
		ArrayList<SearchState> finalPath = new ArrayList<SearchState>();

		AbstractedTileState initialAbs = new AbstractedTileState(initialState.prob);

		for (int i = 0; i < initialState.b.c.length; i++)
			initialState.prob.getCost().abstractTile(i);

		for (ArrayList<Integer> ints : solveOrder) {
			for (Integer nextTile : ints){
				initialState.prob.getCost().refineTile(nextTile);
			}
			initialAbs.recalculateMD();

			EnforcedHillClimbing a = new EnforcedHillClimbing(new org.cwilt.search.search.SimpleSearchProblem(initialAbs),
					l.childLimit());
			finalPath.addAll(a.solve());
			l.addTo(a.getLimit());

			cost += a.getFinalCost();
			
			for (Integer nextTile : ints) {
				initialState.prob.lockTile(nextTile);
			}

			initialAbs = (AbstractedTileState) a.getIncumbent().getGoal().getState();
			finalPath.remove(finalPath.size() - 1);
		}
		
		for(int i = 0; i < initialState.prob.getAcross() * initialState.prob.getDown(); i++){
			if(initialState.prob.canMove(i))
				initialState.prob.getCost().refineTile(i);
		}
		initialAbs.recalculateMD();
		AStar a = new AStar(new org.cwilt.search.search.SimpleSearchProblem(initialAbs), l.childLimit());
		ArrayList<SearchState> sol = a.solve();

		
		cost += a.getFinalCost();
		
		l.addTo(a.getLimit());
		l.endClock();
		
		if(sol == null){
			return null;
		}
		finalPath.addAll(sol);
		setIncumbent(new Solution(a.getIncumbent().getGoal(), cost, l.getDuration(), 
				finalPath.size(), 
				l.getExpansions(), l.getGenerations(), l.getDuplicates()));
		
		return finalPath;
	}

	public static void test() throws IOException, ClassNotFoundException {
		TileProblem p = new TileProblem(TemporaryLoadAndWritePath.getTempPath()
				+ "/tiledata/1", "unit", null);
		TileSolver s = new TileSolver(new Limit(), p);
		ArrayList<SearchState> solution = s.solve();
		assert (solution != null);
		s.printSearchData(System.out);
	}

	private void setSolveOrder() {
		int originalAcross = initialState.prob.getAcross();
		int originalDown = initialState.prob.getDown();

		int across = originalAcross;
		int down = originalDown;
		while (!(across < 4) || !(down < 4)) {
			if (across < down) {
				for (int i = 0; i < across - 2; i++) {
					ArrayList<Integer> inta = new ArrayList<Integer>();
					inta.add(i + (down - 1) * (originalAcross));
					solveOrder.add(inta);
				}
				ArrayList<Integer> inta = new ArrayList<Integer>();
				int id1 = across - 1 + (down - 1) * (originalAcross);
				int id2 = across - 2 + (down - 1) * (originalAcross);
				inta.add(id1);
				inta.add(id2);
				solveOrder.add(inta);
				down--;
			} else {
				for (int i = 0; i < down - 2; i++) {
					ArrayList<Integer> inta = new ArrayList<Integer>();
					inta.add(i * originalAcross + across - 1);
					solveOrder.add(inta);
				}
				ArrayList<Integer> inta = new ArrayList<Integer>();
				int id1 = across + (down - 1) * originalAcross - 1;
				int id2 = across + (down - 2) * originalAcross - 1;
				inta.add(id1);
				inta.add(id2);
				solveOrder.add(inta);
				across--;
			}
		}
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		test();
	}

}
