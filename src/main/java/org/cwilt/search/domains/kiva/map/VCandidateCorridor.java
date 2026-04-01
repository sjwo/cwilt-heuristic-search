package org.cwilt.search.domains.kiva.map;
import java.util.List;

import org.cwilt.search.domains.kiva.map.CorridorFinder.DIRECTION;

public class VCandidateCorridor extends CandidateCorridor{

	public VCandidateCorridor(List<GridCell> centers) {
		super(centers);
	}
	@Override
	protected String orientationString() {
		return "Vertical";
	}
	@Override
	protected DIRECTION getActualOrientation() {
		return DIRECTION.NORTH;
	}

}
