package org.cwilt.search.domains.multiagent.solvers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.PriorityQueue;

import org.cwilt.search.search.Limit;
import org.cwilt.search.search.SearchProblem;
import org.cwilt.search.search.SearchState;
import org.cwilt.search.algs.basic.bestfirst.AStar;
import org.cwilt.search.domains.multiagent.problem.Agent;
import org.cwilt.search.domains.multiagent.problem.AgentState;
import org.cwilt.search.domains.multiagent.problem.MultiagentProblem;
import org.cwilt.search.domains.multiagent.problem.MultiagentTask;
import org.cwilt.search.domains.multiagent.problem.MultiagentVertex;
import org.cwilt.search.domains.multiagent.solvers.queue.AgentQueue;
import org.cwilt.search.domains.multiagent.solvers.queue.EntryQueue;
public class GhostQueueSolver extends org.cwilt.search.domains.multiagent.solvers.MultiagentSolver {
	HashMap<MultiagentVertex, GhostAgentQueue> queues = new HashMap<MultiagentVertex, GhostAgentQueue>();
	PriorityQueue<Agent> agents;

	public GhostQueueSolver(MultiagentProblem p) {
		super(p);

		this.agents = new PriorityQueue<Agent>(10,
				new MultiagentSolver.AgentComparator());
		for (Agent a : problem.getAgents()) {
			agents.add(a);
		}

		for (MultiagentVertex v : p.getGraph().getAllVertexes()) {
			if (v.getQueue() != null) {
				if (v.getQueue() instanceof EntryQueue) {
					v.getQueue().preparePaths();
					if (queues.get(v.getQueue().getTarget()) == null)
						queues.put(v.getQueue().getTarget(),
								new GhostAgentQueue(v.getQueue()));
				}
			}
		}
	}

	private static final class EnqueuedAgent implements
			Comparable<EnqueuedAgent> {
		public final Agent a;
		public final int remainingTime;
		public int waitDuration;

		public EnqueuedAgent(Agent a, int timeToDestination, int duration) {
			this.a = a;
			this.remainingTime = timeToDestination;
			this.waitDuration = duration;
		}

		@Override
		public int compareTo(EnqueuedAgent arg0) {
			return this.remainingTime - arg0.remainingTime;
		}
	}

	private final class GhostAgentQueue {
		public boolean isDone() {
			return this.onTarget == null && this.queue.isEmpty() && this.futureArrivals.isEmpty();
		}

		public final PriorityQueue<EnqueuedAgent> queue = new PriorityQueue<EnqueuedAgent>();
		public final MultiagentVertex queueLocation;
		public final MultiagentVertex queueTarget;
		public EnqueuedAgent onTarget;

		public GhostAgentQueue(AgentQueue q) {
			this.queueTarget = q.getTarget();
			this.queueLocation = q.getFinalQueue();
			this.onTarget = null;
		}

		public void AdvanceTime(int currentTime) {
			ListIterator<EnqueuedAgent> iter = futureArrivals.listIterator();
			while (iter.hasNext()) {
				EnqueuedAgent next = iter.next();
				assert(next.a.getCurrentTime() >= currentTime);
				if (next.a.getCurrentTime() == currentTime) {
					iter.remove();
					queue.add(next);
				}
			}
			if (this.onTarget == null) {
				this.onTarget = queue.poll();
				if (this.onTarget != null)
					this.onTarget.a.doGlobalGhostMove(queueLocation,
							queueTarget);
			}
			if (this.onTarget != null && this.onTarget.waitDuration != 0) {
				this.onTarget.a.doGlobalGhostMove(queueTarget, queueTarget);
				this.onTarget.waitDuration--;
			}
			if (this.onTarget != null && this.onTarget.waitDuration == 0) {
				this.onTarget.a.doTask(true);
				// send this agent on its way, and mark its task as completed.

				// TODO fix this
				// go sit on the unpopular destination
				MultiagentTask task = onTarget.a.getNextTask();
				Agent agent = onTarget.a;

				assert (task != null);
				assert (task.destination != null);
				assert (agent != null);
				SearchProblem np = agent.getSimpleProblem(task.destination);
				AStar a = new AStar(np, getLimit());
				ArrayList<SearchState> returnPath = a.solve();
				if (returnPath == null) {
					assert (false);
				}
				for (int i = 1; i < returnPath.size(); i++) {
					AgentState nextMove = (AgentState) returnPath.get(i);
					agent.doGlobalGhostMove(nextMove.getStartVertex(),
							nextMove.getEndVertex());
				}
				assert (agent.canDoTask());
				agent.doTask(false);
				this.onTarget = null;
				GhostQueueSolver.this.agents.add(agent);
			}
			for (EnqueuedAgent a : queue) {
				a.a.doGlobalGhostMove(queueLocation, queueLocation);
			}
		}

		LinkedList<EnqueuedAgent> futureArrivals = new LinkedList<EnqueuedAgent>();

		public void addAgent(EnqueuedAgent ea) {
			futureArrivals.add(ea);
		}
	}

	private static final Limit getLimit() {
		return new Limit();
		// return new Limit(1000, 100000, 100000, false);
	}

	/**
	 * 
	 * @param agent
	 *            Agent to move to its next queue destination
	 */
	private void goToQueue(Agent agent) {
		assert (agent != null);

		MultiagentTask task = agent.getNextTask();

		assert (task != null);
		GhostAgentQueue targetQueue = queues.get(task.destination);
		assert (targetQueue != null);

		SearchProblem np = agent.getSimpleProblem(targetQueue.queueLocation);
		AStar a = new AStar(np, getLimit());
		ArrayList<SearchState> path = a.solve();
		if (path == null)
			a.printSearchData(System.err);
		assert (path != null);
		for (int i = 1; i < path.size(); i++) {
			AgentState s = (AgentState) path.get(i);
			agent.doGlobalGhostMove(s.getStartVertex(), s.getEndVertex());
		}
		AgentState last = (AgentState) path.get(path.size() - 1);
		assert (last.getEndVertex().equals(targetQueue.queueLocation));
		MultiagentTask nextTask = agent.getTask(agent.getCurrentTaskID() + 1);
		assert (nextTask != null);
		int dist = (int) nextTask.destination
				.distanceTo(targetQueue.queueTarget);
		EnqueuedAgent ea = new EnqueuedAgent(agent, (dist),
				task.timeAtDestination);
		targetQueue.addAgent(ea);

	}

	private boolean isDone() {
		if (!this.agents.isEmpty())
			return false;

		Iterator<Entry<MultiagentVertex, GhostAgentQueue>> iter = queues
				.entrySet().iterator();
		while (iter.hasNext()) {
			if (!iter.next().getValue().isDone())
				return false;
		}
		return true;
	}

	private int currentTime;

	@Override
	public void solve() {

		while (!isDone()) {

			while (!agents.isEmpty()
					&& agents.peek().getCurrentTime() == currentTime) {
				Agent next = agents.poll();
				if(!(next.getNextTask() == null))
					goToQueue(next);
			}

			Iterator<Entry<MultiagentVertex, GhostAgentQueue>> iter = queues
					.entrySet().iterator();
			while (iter.hasNext()) {
				GhostAgentQueue q = iter.next().getValue();
				q.AdvanceTime(currentTime);
			}
			currentTime++;
		}
	}

}
