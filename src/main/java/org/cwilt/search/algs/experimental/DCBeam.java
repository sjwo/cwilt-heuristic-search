package org.cwilt.search.algs.experimental;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.cwilt.search.algs.basic.Beam;
import org.cwilt.search.search.Limit;
import org.cwilt.search.search.SearchNode;
import org.cwilt.search.utils.basic.Utils;
/**
 * DCBeam
 * 
 * Data Collection Beam Search
 * 
 * Beam searches that collect data, but also run slower.
 * 
 * @author cmo66
 *
 */

public class DCBeam extends Beam implements Cloneable{
	public DCBeam(org.cwilt.search.search.SearchProblem initial, Limit l, int beamWidth) {
		super(initial, l, beamWidth);
		diversityByLevel = new ArrayList<Float>();
		backToEve = new ArrayList<Float>();
	}
	
	public DCBeam clone(){
		return new DCBeam(prob, l, beamWidth);
	}
	
	private final ArrayList<Float> diversityByLevel;
	private final ArrayList<Float> backToEve;
	
	private int countToEve(HashMap<Object, SearchNode> children){
		HashMap<Object, SearchNode> parents = new HashMap<Object, SearchNode>();
		int eveCount = 0;
		while(true){
			for(SearchNode n : children.values()){
				if(n != null && !parents.containsKey(n.getState().getKey())){
					parents.put(n.getParent().getState().getKey(), n.getParent());
				}
			}
			if(parents.size() <= 1)
				break;
			HashMap<Object, SearchNode> newParents = children;
			children = parents;
			parents = newParents;
			parents.clear();
			eveCount ++;
		}
		return eveCount;
	}
	
	@Override
	protected void processLayer(){
		HashSet<Object> parents = new HashSet<Object>();
		HashMap<Object, SearchNode> children = new HashMap<Object, SearchNode>();
		Iterator<SearchNode> parentIter = super.parents.iterator();
		while(parentIter.hasNext()){
			SearchNode n = parentIter.next();
			parents.add(n.getState().getKey());
		}
		super.processLayer();
		Iterator<SearchNode> childIter = super.parents.iterator();
		HashSet<Object> fertileParents = new HashSet<Object>();
		while(childIter.hasNext()){
			SearchNode child = childIter.next();
			fertileParents.add(child.getParent().getState().getKey());
			children.put(child.getState().getKey(), child);
		}
		float percentFertile = ((float) fertileParents.size()) / ((float) parents.size());
		diversityByLevel.add(percentFertile);
		backToEve.add((float) countToEve(children));
	}
	
	public void printExtraData(PrintStream ps){
		super.printExtraData(ps);
		//for(float f : diversityByLevel)
		//	SolutionLog.printPair(ps, getName() + "diversity", new Float(f));
		printPair(ps, getName() + " average diversity", new Float(Utils.arraylistAverage(diversityByLevel)));
		printPair(ps, getName() + " to eve", new Float(Utils.arraylistAverage(backToEve)));
	}
}
