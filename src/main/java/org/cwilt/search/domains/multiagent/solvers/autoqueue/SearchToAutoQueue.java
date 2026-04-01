package org.cwilt.search.domains.multiagent.solvers.autoqueue;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;

import org.cwilt.search.search.SearchState;
import org.cwilt.search.domains.kiva.map.GridCell;
import org.cwilt.search.domains.multiagent.problem.Agent;
import org.cwilt.search.domains.multiagent.problem.MultiagentVertex;
import org.cwilt.search.domains.multiagent.problem.ReservationTable;
public class SearchToAutoQueue implements org.cwilt.search.search.SearchProblem {
	public final boolean waitAtStart;

	public final HashSet<MultiagentVertex> waitLocations;
	public final boolean filterEarlyWaits;
	public final int goalTime;
	public final int estimatedCost;

	public SearchToAutoQueue(MultiagentVertex start, MultiagentVertex goal,
			int timeAtGoal, Agent a, ReservationTable r, boolean waitAtStart,
			boolean filterEarlyWaits) {
		// assert(timeAtGoal <= 0);
		this.timeAtGoal = timeAtGoal;
		this.filterEarlyWaits = filterEarlyWaits;
		this.waitAtStart = waitAtStart;
		this.start = start;
		this.target = ((GridCell) goal).getAutoQueue();
		this.goal = goal;
		this.agent = a;
		this.res = r;
		this.waitLocations = new HashSet<MultiagentVertex>();
		this.estimatedCost = (int) start.distanceTo(goal);
		this.goalTime = 0;
	}

	public SearchToAutoQueue(MultiagentVertex start, MultiagentVertex goal,
			int timeAtGoal, Agent a, ReservationTable r, boolean waitAtStart,
			boolean filterEarlyWaits, int goalTime) {
		// assert(timeAtGoal <= 0);
		this.timeAtGoal = timeAtGoal;
		this.filterEarlyWaits = filterEarlyWaits;
		this.waitAtStart = waitAtStart;
		this.start = start;
		this.target = ((GridCell) goal).getAutoQueue();
		this.goal = goal;
		this.agent = a;
		this.res = r;
		this.waitLocations = new HashSet<MultiagentVertex>();
		this.estimatedCost = (int) start.distanceTo(goal);
		this.goalTime = goalTime;
	}

	
	public MultiagentVertex bestWaitLocation() {
		if (waitLocations.isEmpty())
			return null;

		MultiagentVertex v = null;
		for (MultiagentVertex current : waitLocations) {
			if(current.isMultiagentGoal()){
				continue;
			}
			if (v == null || current.nearestPopularVertex() > v.nearestPopularVertex())
				v = current;
		}
		return v;
	}

	public final Agent agent;
	public final int timeAtGoal;
	public final MultiagentVertex start;
	public final MultiagentVertex goal;
	public final AutoQueue target;
	public final ReservationTable res;

	@Override
	public SearchState getInitial() {
		return new AutoQueueSearchNode(start, agent.getCurrentTime(), this, null);
	}

	@Override
	public SearchState getGoal() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ArrayList<SearchState> getGoals() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCalculateD() {
	}

	@Override
	public void printProblemData(PrintStream ps) {
	}
}
