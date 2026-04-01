package org.cwilt.search.domains.multiagent.solvers.autoqueue;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;

import org.cwilt.search.domains.kiva.SearchTracker;
import org.cwilt.search.domains.kiva.map.GridCell;
import org.cwilt.search.domains.multiagent.problem.Agent;
import org.cwilt.search.domains.multiagent.problem.AgentState;
import org.cwilt.search.domains.multiagent.problem.MultiagentProblem;
import org.cwilt.search.domains.multiagent.problem.MultiagentTask;
import org.cwilt.search.domains.multiagent.problem.MultiagentVertex;
import org.cwilt.search.domains.multiagent.solvers.MultiagentSolver;
import org.cwilt.search.domains.multiagent.solvers.queue.QueueOverflow;
public class AutoQueueSolver extends MultiagentSolver {
	boolean[] finished;

	public static final int EXPANSION_FACTOR = 16;
	private final PriorityQueue<Agent> agents;
	private final HashSet<AutoQueue> queues = new HashSet<AutoQueue>();
	private static final int TIMEOUT = 2000;
	private int currentTime = 0;

	public AutoQueueSolver(MultiagentProblem p) {
		super(p);
		this.agents = new PriorityQueue<Agent>(10,
				new MultiagentSolver.AgentComparator());
		for (Agent a : problem.getAgents()) {
			agents.add(a);
		}

		for (MultiagentVertex v : p.getGraph().getAllVertexes()) {
			GridCell c = (GridCell) v;
			if (c.getAutoQueue() != null) {
				queues.add(c.getAutoQueue());
				c.getAutoQueue().preparePaths();
				c.getAutoQueue().setSolver(this);
			}
		}
		this.finished = new boolean[agents.size()];
		for (int i = 0; i < agents.size(); i++) {
			finished[i] = false;
		}
	}

	private int failedSearches = 0;

	public int getFailedSearches() {
		return this.failedSearches;
	}

	public void solve() throws QueueOverflow {
		for (Agent a : agents) {

			super.problem.getReservationTable().claimIndefinitely(
					a.getState().getEndVertex(), 1, a);
		}

		while (true) {
			// System.err.println("current time is " + currentTime);
			if (currentTime > TIMEOUT) {
				break;
				// throw new RuntimeException("Passed " + TIMEOUT +
				// " time steps");
			}
			// pop all the robots whose time has come to move and plan them, or
			// issue a wait instruction.

			assert (agents.isEmpty() || agents.peek().getCurrentTime() >= currentTime);
			while (!agents.isEmpty()
					&& agents.peek().getCurrentTime() == currentTime) {

				Agent nextAgent = agents.poll();

				MultiagentTask nextTask = nextAgent.getNextTask();
				assert (nextTask != null);
				MultiagentVertex goalVertex;
				int goalTime;

				goalVertex = nextTask.destination;
				goalTime = nextTask.timeAtDestination;

				// System.err.println(nextAgent+ " at " + currentTime + " " +
				// nextAgent.getState().getEndVertex()+ " going to " +
				// goalVertex);

				SearchToAutoQueue s = new SearchToAutoQueue(nextAgent
						.getState().getEndVertex(), goalVertex, goalTime,
						nextAgent, problem.getReservationTable(), true, false);
				int allocatedExpansions = s.estimatedCost * EXPANSION_FACTOR;
				if (allocatedExpansions == 0)
					allocatedExpansions = 10000;
				// AutoQueueSearcher a = new AutoQueueSearcher(s, new
				// search.Limit(Long.MAX_VALUE, allocatedExpansions,
				// Long.MAX_VALUE, false));
				org.cwilt.search.algs.basic.bestfirst.AStar a = new org.cwilt.search.algs.basic.bestfirst.AStar(
						s, new org.cwilt.search.search.Limit(Long.MAX_VALUE,
								allocatedExpansions, Long.MAX_VALUE, false));
				ArrayList<org.cwilt.search.search.SearchState> path = a.solve();

				SearchTracker.getTracker().incrSearchTime3dAll(a.getLimit());
				if (path == null) {
					// try again next time?
					// this currently assumes the agent is able to do this, if
					// it isn't try jiggling around instead?

					// int waitToDo = ((int) a.getMinimumH()) + 10;
					failedSearches++;
					SearchTracker.getTracker().incrSearchTime3dFail(
							a.getLimit());
					int waitToDo = 10;
					boolean canWait = nextAgent.canAddWaitMoves(waitToDo);
					if (!canWait) {
						rerouteAgent(nextAgent);
					} else {
						nextAgent.addWaitMoves(waitToDo);
					}
					// boolean wandered = nextAgent.wander();
					// assert(wandered);

					// System.err.println(nextAgent);
					// System.err.print(nextAgent.getState());
					// System.err.println("going to " + goalVertex);
					// System.err.println("Expanded "
					// + a.getLimit().getExpansions() + " nodes");
					// System.err.println("Best h value: "
					// + a.getMinimumH());
					// System.err.println();
					agents.add(nextAgent);
				} else {
					problem.getReservationTable().releaseIndefiniteClaim(
							nextAgent.getState().getEndVertex());

					for (int i = 1; i < path.size(); i++) {
						MultiagentVertex original = ((AutoQueueSearchNode) path
								.get(i - 1)).key.v;
						MultiagentVertex next = ((AutoQueueSearchNode) path
								.get(i)).key.v;
						nextAgent.doMove(original, next);
					}
					assert (goalVertex != null);
					if (((GridCell) goalVertex).getAutoQueue() == null) {
						// this agent is now at its goal, add it back into the
						// queue so it can get solved for along with the other
						// mobile units.

						this.acceptAgent(nextAgent);
						continue;
					} else {
						((GridCell) goalVertex).getAutoQueue().acceptAgent(
								nextAgent, goalTime);
					}
				}

			}

			// advance time on all of the queues
			// boolean allEmpty = true;
			assert (!queues.isEmpty());
			for (AutoQueue q : queues) {
				// System.err.println("advancing " + q);
				q.advanceAgents(this);
				q.checkAgents();
			}

			for (AutoQueue q : queues) {
				if (!q.isDone()) {
					// System.err.println("entry");
					// allEmpty = false;
				}
			}

			// System.err.println("Time " + currentTime + " allEmpty: " +
			// allEmpty + " agents " + agents.isEmpty());
			if (currentTime == maxAgentTime - 1 && done())
				break;
			else if (agents.peek() != null) {

				// System.err.println(agents.peek());
				// System.err.println(agents.peek().getCurrentTime());
			}
			currentTime++;
		}
	}

	private static final int MAX_POP = 10;

	/**
	 * 
	 * @param a
	 *            Agent to re-route.
	 */
	void rerouteAgent(Agent nextAgent) {
		
		for (int popAmount = 0; popAmount < MAX_POP; popAmount++) {
			MultiagentTask nextTask = nextAgent.getNextTask();
			assert (nextTask != null);
			MultiagentVertex goalVertex;
			int goalTime;

			goalVertex = nextTask.destination;
			goalTime = nextTask.timeAtDestination;

			assert (goalVertex.getAutoQueue() == null);

			// System.err.println(nextAgent+ " at " + currentTime + " " +
			// nextAgent.getState().getEndVertex()+ " going to " +
			// goalVertex);
			
			AgentState state = nextAgent.getPreviousState(1);
			if(state.getEndVertex().getAutoQueue() != null){
				assert(false);
			}
			nextAgent.backoffUntil(state);
			
			SearchToAutoQueue s = new SearchToAutoQueue(nextAgent.getState()
					.getEndVertex(), goalVertex, goalTime, nextAgent,
					problem.getReservationTable(), true, false, currentTime);
			int allocatedExpansions = s.estimatedCost * EXPANSION_FACTOR;
			allocatedExpansions = 10000;
			org.cwilt.search.algs.basic.bestfirst.AStar a = new org.cwilt.search.algs.basic.bestfirst.AStar(s,
					new org.cwilt.search.search.Limit(Long.MAX_VALUE, allocatedExpansions,
							Long.MAX_VALUE, false));
			ArrayList<org.cwilt.search.search.SearchState> path = a.solve();
			if(path == null)
				continue;
			SearchTracker.getTracker().incrSearchTime3dAll(a.getLimit());
			problem.getReservationTable().releaseIndefiniteClaim(
					nextAgent.getState().getEndVertex());
			
			for (int i = 1; i < path.size(); i++) {
				MultiagentVertex original = ((AutoQueueSearchNode) path
						.get(i - 1)).key.v;
				MultiagentVertex next = ((AutoQueueSearchNode) path.get(i)).key.v;
				nextAgent.doMove(original, next);
			}
		}
	}

	boolean done() {
		for (boolean b : finished) {
			if (!b)
				return false;
		}
		return true;
	}

	int maxAgentTime = 0;

	public void acceptAgent(Agent a) {
		if (a.isDone()) {
			finished[a.getID()] = true;
		}
		if (a.getCurrentTime() > this.maxAgentTime) {
			this.maxAgentTime = a.getCurrentTime();
		}
		agents.add(a);
	}
}
