package org.cwilt.search.misc.analysis;
import java.util.ArrayList;

import org.cwilt.search.utils.basic.Utils;
public class CellAnalysis {
	
	private final ArrayList<Float> coreSize;
	private final ArrayList<Float> regionSize;
	private final ArrayList<Float> borderSize;
	private final ArrayList<Float> coreDepth;
	private final ArrayList<Float> branchingFactor;
	private int coreCompleteCount;
	private int totalCount;
	private int completeCount;

	
	public CellAnalysis(){
		branchingFactor = new ArrayList<Float>();
		coreSize = new ArrayList<Float>();
		regionSize = new ArrayList<Float>();
		borderSize = new ArrayList<Float>();
		coreDepth = new ArrayList<Float>();
	}
	
	public void addCell(Cell c){
		coreSize.add((float) c.coreCount);
		regionSize.add((float) c.regionCount);
		borderSize.add((float)c.borderCount);
		coreDepth.add((float)c.coreDepth);
		branchingFactor.add((float) c.getBranchingFactor());
		totalCount ++;
		if(c.coreComplete)
			coreCompleteCount++;
		if(c.cellComplete)
			completeCount ++;
	}
	public String toString(){
		StringBuffer b = new StringBuffer();
		float coreCompletePercent = ((float) coreCompleteCount) / ((float) totalCount) * 100;
		float completePercent = ((float) completeCount) / ((float) totalCount) * 100;
		b.append("complete cores ");
		b.append(coreCompleteCount);
		b.append("/");
		b.append(totalCount);
		b.append(" (");
		b.append(coreCompletePercent);
		b.append("%)");
		b.append("\ncomplete cells ");
		b.append(completeCount);
		b.append("/");
		b.append(totalCount);
		b.append(" (");
		b.append(completePercent);
		b.append("%)");
		b.append("\ncore size: ");
		b.append(Utils.arraylistAverage(coreSize));
		b.append("\ncore depth: ");
		b.append(Utils.arraylistAverage(coreDepth));
		b.append("\nregion size: ");
		b.append(Utils.arraylistAverage(regionSize));
		b.append("\nborder size: ");
		b.append(Utils.arraylistAverage(borderSize));
		b.append("\nbranching factor: ");
		b.append(Utils.arraylistAverage(branchingFactor));
		
		return b.toString();
	}
}
