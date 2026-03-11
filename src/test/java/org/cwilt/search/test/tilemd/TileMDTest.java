package org.cwilt.search.test.tiles;

import org.cwilt.search.algs.basic.bestfirst.AStar;
import org.cwilt.search.algs.basic.incremental.PartialExpansionBestFirstSearch;
import org.cwilt.search.domains.tiles.TileProblem;
import org.cwilt.search.search.Limit;
import org.cwilt.search.search.SearchAlgorithm;
import org.cwilt.search.search.SearchNode;
import org.junit.Test;
import java.io.IOException;

public class TileMDTest {

	@Test
	public void test() throws IOException, ClassNotFoundException {
		String[] pdbArgs = null;
		TileProblem tp = new TileProblem("/home/aifs2/sjw/tunable/mess/domains/elevenpuzzle/random_1_wilt", "unit", pdbArgs);
		SearchAlgorithm aStar = new AStar(tp, new Limit());
		aStar.solve();

		aStar.printSearchData(System.out);
	}

}

