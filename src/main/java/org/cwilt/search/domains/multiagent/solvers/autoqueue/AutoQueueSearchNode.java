package org.cwilt.search.domains.multiagent.solvers.autoqueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.cwilt.search.domains.multiagent.problem.MultiagentVertex;
import org.cwilt.search.search.SearchState;
import org.cwilt.search.utils.basic.Heapable;
public class AutoQueueSearchNode extends SearchState implements Heapable{
	private final SearchToAutoQueue problem;
	private final AutoQueueSearchNode parent;
	private final double h;
	
	public ArrayList<SearchState> reconstructPath(){
		AutoQueueSearchNode current = this;
		ArrayList<SearchState> path = new ArrayList<SearchState>();
		while(current != null){
			path.add(current);
			current = current.parent;
		}
		Collections.reverse(path);
		return path;
	}
	public final class QueueSearchKey {

		@Override
		public String toString() {
			return "QueueSearchKey [v=" + v + ", time=" + time
					+ ", fC=" + fCongestion + ", c=" + congestion + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp = prime;
//			temp = Double.doubleToLongBits(congestion);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result + time;
			result = prime * result + ((v == null) ? 0 : v.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			QueueSearchKey other = (QueueSearchKey) obj;
//			if (Double.doubleToLongBits(congestion) != Double
//					.doubleToLongBits(other.congestion))
//				return false;
			if (time != other.time)
				return false;
			if (v == null) {
				if (other.v != null)
					return false;
			} else if (!v.equals(other.v))
				return false;
			return true;
		}

		public final MultiagentVertex v;
		public final int time;
		public final double congestion;
		public final double fCongestion;

		public QueueSearchKey(MultiagentVertex v, int time, double congestion, double fCongestion) {
			this.v = v;
			this.time = time;
			this.congestion = congestion;
			this.fCongestion = fCongestion;
		}

	}

	public final QueueSearchKey key;

	public AutoQueueSearchNode(MultiagentVertex key, int time,
			SearchToAutoQueue target, AutoQueueSearchNode parent) {
		double congestion;
		this.parent = parent;
		if (parent == null) {
			congestion = 0;
		} else {
			congestion = parent.key.congestion + parent.key.v.getCongestion();
		}

		this.problem = target;
		double hc = 0;//key.getHCongestion(problem.goal);
		this.key = new QueueSearchKey(key, time, congestion, congestion + hc);
		this.h = this.key.v.distanceTo(problem.goal);


//		double h = this.h();
//		
//		if(h != hc){
//			//happy if this happens
//		} else {
//			GridCell e = (GridCell) target.goal;
//			assert(!e.isTravel());
//		}

		assert (congestion < 100000000000000000.0d);

		assert (target.res.checkReservation(key, time, target.agent));
//		throw new RuntimeException("H congestion calculation has been disabled");
	}

	@Override
	public ArrayList<Child> expand() {
		// generate the adjacent children
		List<MultiagentVertex> adj = key.v.getNeighbors(problem.goal);
		ArrayList<Child> children = new ArrayList<Child>(adj.size() + 1);

		// if this is not the start node
		if (!this.problem.start.equals(this.key.v)) {
			// and it is a queue cell, it should not have any children.
			if (this.key.v.getAutoQueue() != null)
				return children;
		}

		if (key.v.getAutoQueue() == null
				&& problem.res.canClaimIndefinitely(this.key.v,
						problem.agent.getCurrentTime() - 2, problem.agent)) {
			problem.waitLocations.add(key.v);
		}
		

		
		for (MultiagentVertex v : adj) {

			if (v.isForbidden(problem.start, problem.goal)) {
				continue;
			}

			boolean atStart = v.equals(problem.start);
			if (atStart && !problem.waitAtStart)
				continue;

			if (problem.res.checkReservation(v, key.time + 1, problem.agent)
					&& problem.res.checkReservation(key.v, key.time + 1,
							problem.agent)) {
				children.add(new Child(new AutoQueueSearchNode(v, key.time + 1,
						problem, this), 1));
			}
		}

		// check to see if can wait at the start
		boolean atStart = key.v.equals(problem.start);

		if ((!atStart || problem.waitAtStart)
				&& problem.res.checkReservation(key.v, key.time + 1,
						problem.agent) && key.v.getAutoQueue() == null
				&& !problem.filterEarlyWaits) {

			children.add(new Child(new AutoQueueSearchNode(key.v, key.time + 1,
					problem, this), 1));
		}
		return children;
	}

	@Override
	public ArrayList<Child> reverseExpand() {
		throw new UnsupportedOperationException();
	}

	@Override
	public double h() {
		return h;
	}

	@Override
	public int d() {
		return (int) h();
	}
	//TODO have to check to make sure this vertex can be claimed for a few time steps.
	@Override
	public boolean isGoal() {
		
		if (problem.target == null) {
			boolean locationMatches = problem.goal.equals(key.v);
			if(!locationMatches)
				return false;
			
			boolean timePassed = key.time != problem.agent.getCurrentTime();
			if(!timePassed){
				return false;
			}
			boolean timePassed2 = key.time > problem.goalTime;
			if(!timePassed2)
				return false;
			boolean open1 = problem.res.checkReservation(key.v, key.time, problem.agent);
			boolean open2 = problem.res.checkReservation(key.v, key.time + 1, problem.agent);
			
			return open1 && open2;
		}
		
		
		return problem.target.canAcceptAgent(problem.agent, key.v, key.time,
				problem.timeAtGoal);
	}

	@Override
	public Object getKey() {
		return key;
	}

	@Override
	public int lexOrder(SearchState s) {
		AutoQueueSearchNode other = (AutoQueueSearchNode) s;
		assert (this.key.congestion > 0);
		assert (other.key.congestion > 0);
		throw new RuntimeException("Can't use congestion - values are nonsense");
//		return (int) (other.key.congestion - this.key.congestion);
	}
	
	public static class AutoQueueComparator implements Comparator<AutoQueueSearchNode> {

		@Override
		public int compare(AutoQueueSearchNode arg0, AutoQueueSearchNode arg1) {
			double f0 = arg0.key.time + arg0.h;
			double f1 = arg1.key.time + arg1.h;
			if(f0 < f1) {
				return -1;
			} else if(f0 > f1) {
				return 1;
			} else {
				if(arg0.key.fCongestion < arg1.key.fCongestion){
					return 1;
				} else if (arg0.key.fCongestion > arg1.key.fCongestion){
					return -1;
				} else {
					return 0;
				}
			}
		}
		
	}
	
	public String toString() {
		double f0 = key.time + h();
		double h0 = h();
		return String.format("f=%f h=%f %s\n", f0, h0, key.toString());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AutoQueueSearchNode other = (AutoQueueSearchNode) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}
	
	private int heapIndex = Heapable.NO_POS;
	
	@Override
	public int getHeapIndex() {
		return heapIndex;
	}

	@Override
	public void setHeapIndex(int ix) {
		this.heapIndex = ix;
	}

}
