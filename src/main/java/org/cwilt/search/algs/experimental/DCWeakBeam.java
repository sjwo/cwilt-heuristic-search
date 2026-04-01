package org.cwilt.search.algs.experimental;
import java.io.PrintStream;
import java.util.HashMap;

import org.cwilt.search.search.Limit;
import org.cwilt.search.search.SearchNode;
public class DCWeakBeam extends WeakBeam{

	private HashMap<Object, SearchNode> globalClosed;
	private int globalDuplicates;
	
	public DCWeakBeam(org.cwilt.search.search.SearchProblem initial, Limit l, int beamWidth) {
		super(initial, l, beamWidth);
		this.globalDuplicates = 0;
		this.globalClosed = new HashMap<Object, SearchNode>();
	}


	
	@Override
	protected void addToClosed(SearchNode child){
		super.addToClosed(child);
		globalClosed.put(child.getState().getKey(), child);
	}
	
	@Override
	protected SearchNode getIncumbentNode(SearchNode child){
		SearchNode globalIncumbent = globalClosed.get(child.getState().getKey());
		if(globalIncumbent != null)
			globalDuplicates ++;
		return super.getIncumbentNode(child);
	}
	
	@Override
	public void printExtraData(PrintStream ps){
		super.printExtraData(ps);
		printPair(ps, "global duplicates", new Integer(globalDuplicates));
	}
	public DCWeakBeam clone(){
		super.checkClone(DCWeakBeam.class.getCanonicalName());
		return new DCWeakBeam(prob, l.clone(), beamWidth);
	}
}
