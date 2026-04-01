package org.cwilt.search.domains.kiva.map;
import java.util.List;

import org.cwilt.search.domains.kiva.map.CorridorFinder.DIRECTION;
public class HCandidateCorridor extends CandidateCorridor{

	public HCandidateCorridor(List<GridCell> centers) {
		super(centers);
	}

	@Override
	protected String orientationString() {
		return "Horizontal";
	}

	@Override
	protected DIRECTION getActualOrientation() {
		return DIRECTION.EAST;
	}

}
