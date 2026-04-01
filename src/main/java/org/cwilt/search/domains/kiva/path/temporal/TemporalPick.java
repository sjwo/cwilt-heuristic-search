package org.cwilt.search.domains.kiva.path.temporal;
import java.util.ArrayList;

import org.cwilt.search.domains.kiva.map.GridCell;
import org.cwilt.search.domains.kiva.path.timeless.Move;
import org.cwilt.search.domains.kiva.path.timeless.NavigationProblem;
public class TemporalPick extends TemporalMove {
	public TemporalPick(GridCell pos, DIRECTION dir, int time, NavigationProblem p) {
		super(pos, pos, dir, dir, time, p);
	}

	@Override
	protected void getChildren(ArrayList<Move> children) {
		super.getStraightChildren(children);
		super.getWaitChild(children);
	}
}
