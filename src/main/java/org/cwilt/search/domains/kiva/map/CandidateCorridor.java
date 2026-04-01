package org.cwilt.search.domains.kiva.map;
import java.util.List;

public abstract class CandidateCorridor {
	public int getLength(){
		return centers.size() / this.width;
	}
	public final List<GridCell> centers;
	public final int width;
	
	
	protected abstract String orientationString();
	
	private int inbound, outbound;
	
	public int getInbound(){
		return this.inbound;
	}
	public int getOutbound(){
		return this.outbound;
	}
	
	public CorridorFinder.DIRECTION getOrientation(){
		int lrCount = 0;
		int udCount = 0;
		
		int lCount = 0, rCount = 0, uCount = 0, dCount = 0;
		
		for(GridCell g : centers){
			lrCount += g.getDirectionalCount(GridCell.LEFT);
			lrCount += g.getDirectionalCount(GridCell.RIGHT);
			udCount += g.getDirectionalCount(GridCell.UP);
			udCount += g.getDirectionalCount(GridCell.DOWN);

			lCount += g.getDirectionalCount(GridCell.LEFT);
			rCount += g.getDirectionalCount(GridCell.RIGHT);
			uCount += g.getDirectionalCount(GridCell.UP);
			dCount += g.getDirectionalCount(GridCell.DOWN);

		}
		
		this.inbound = this.width / 2;
		this.outbound = this.width / 2;
		
		if(lrCount > udCount){
			assert(this instanceof HCandidateCorridor);
			if(lCount > rCount && this.width % 2 != 0){
				this.inbound ++;
			} else if (this.width % 2 != 0){
				this.outbound ++;
			}
			return CorridorFinder.DIRECTION.EAST;
		} else if (lrCount < udCount) {
			if(uCount > dCount && this.width % 2 != 0){
				this.inbound ++;
			} else if (this.width % 2 != 0){
				this.outbound ++;
			}
			assert(this instanceof VCandidateCorridor);
			return CorridorFinder.DIRECTION.NORTH;
		} else {
			if(lrCount != 0)
				throw new RuntimeException("This corridor is used equally for up/down (" + udCount + ") and L/R travel (" + lrCount + ")");
			//no data for this, just guess?
			if(this.width % 2 != 0){
				this.inbound ++;
			}
			return this.getActualOrientation();
		}
	}
	
	protected abstract CorridorFinder.DIRECTION getActualOrientation();
	
	public String toString(){
		StringBuffer b = new StringBuffer();
		b.append(orientationString());
		b.append(" ");
		b.append(width);
		b.append(" ");
		for(GridCell g : centers){
			b.append(g.toString());
			b.append(" [");
			for(int i = 0; i < 4; i++){
				b.append(g.getDirectionalCount(i));
				b.append(",");
			}
			b.append("]");
		}
		b.append(getOrientation());
		return b.toString();
	}
	
	public CandidateCorridor(List<GridCell> centers) {
		this.centers = centers;
		this.width = centers.size();
	}
	
	public boolean areAdjacentOrSame(CandidateCorridor other){
		for(GridCell g : centers){
			if(other.centers.contains(g)){
				// only like corridors should be adjacent to one another
				assert (this.getClass().equals(other.getClass()));
				return true;
			}
			for(GridCell o : other.centers){
				if(g.adjacent(o)){
					assert (this.getClass().equals(other.getClass()));
					return true;
				}
			}
		}
		return false;

		//		GridCell otherHead = other.centers.get(0);
//		GridCell thisHead = this.centers.get(this.centers.size() - width);
//		if(!otherHead.adjacent(thisHead) && !otherHead.equals(thisHead)){
//			return false;
//		}
//		return false;
	}
	
	public void merge(CandidateCorridor other) {
		// have to merge vertical corridors with other vertical corridors, and
		// horizontal corridors with other horizontal corridors)
		assert (this.getClass().equals(other.getClass()));
		
		for(GridCell g : other.centers){
			if(centers.contains(g)){
				continue;
			}
			this.centers.add(g);
		}
	}

	public static CandidateCorridor makeCandidateCorridor(
			List<GridCell> centers, CorridorFinder.DIRECTION orientation) {
		switch (orientation) {
		case EAST:
			return new HCandidateCorridor(centers);
		case NORTH:
			return new VCandidateCorridor(centers);
		default:
			throw new RuntimeException("invalid enum value: " + orientation);
		}
	}
}
