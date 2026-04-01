package org.cwilt.search.domains.kiva.drive;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import org.cwilt.search.domains.kiva.map.GridCell;
import org.cwilt.search.domains.kiva.path.simplified.SimpleMove;
import org.cwilt.search.domains.kiva.path.timeless.NavigationProblem;
import org.cwilt.search.domains.kiva.problem.KivaProblem;
import org.cwilt.search.search.SearchState.Child;
public class DFSRandomWalk implements Comparator<Child>{
	private final SimpleMove start;
	private final int length;
	private final Random r;
	private final KivaProblem p;
	private final Drive d;
	private boolean done;
	private int tries;
	
	public DFSRandomWalk(int length, int seed, KivaProblem p, Drive d){
		this.start = (SimpleMove) d.getLastMove();
		GridCell goal = (GridCell) d.getNextTask().destination;
		this.start.setProblem(new NavigationProblem((org.cwilt.search.domains.kiva.path.timeless.Move) start, goal, d, p));
		this.length = length;
		this.r = new Random(seed);
		this.p = p;
		this.d = d;
		this.done = false;
	}
	
	
	public ArrayList<SimpleMove> getPath(){
		ArrayList<SimpleMove> p = new ArrayList<SimpleMove>();
		p.add(start);
		wander(p);
		
		return p;
	}
	
	private void wander(ArrayList<SimpleMove> path){
		tries ++;
		if(tries > 10000){
			assert(false);
		}
		if(path.size() == length){
			if(d.canClaimSpace(path.get(path.size() - 1).endPosition, d.getCurrentTime() + length+1, 4))
				done = true;
			return;
		}
		if(path.size() > length)
			assert(false);
		ArrayList<Child> children = path.get(path.size() - 1).expand();
		Collections.shuffle(children, r);
		Collections.sort(children, this);
		for(int i = 0; i < children.size() && !done; i++){
			Child c = children.get(i);
			SimpleMove m = (SimpleMove) c.child;
			if(!m.endPosition.isTravel())
				continue;
			if(m.canDo(p, d, (GridCell) d.getNextTask().destination)){
				assert(path.size() > 0);
				SimpleMove last = path.get(path.size() - 1);
				assert(last.getTime() == m.getTime() - 1);
				path.add(m);
				wander(path);
				if(!done)
					path.remove(path.size() - 1);
				
			}
		}
	}


	@Override
	public int compare(Child arg0, Child arg1) {
		double dist0 = start.endPosition.distanceTo(((SimpleMove)arg0.child).endPosition);
		double dist1 = start.endPosition.distanceTo(((SimpleMove)arg1.child).endPosition);
		// TODO Auto-generated method stub
		return (int) (dist1 - dist0);
	}
}
