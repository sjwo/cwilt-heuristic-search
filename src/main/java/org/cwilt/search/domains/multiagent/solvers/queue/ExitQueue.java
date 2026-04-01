package org.cwilt.search.domains.multiagent.solvers.queue;
import java.util.List;

import org.cwilt.search.domains.multiagent.problem.Agent;
import org.cwilt.search.domains.multiagent.problem.MultiagentTask;
import org.cwilt.search.domains.multiagent.problem.MultiagentVertex;
public class ExitQueue extends AgentQueue {

	public ExitQueue(MultiagentVertex t) {
		super(t);
	}
	
	private Disperser d;
	public void setDisperser(Disperser d){
		this.d = d;
	}
	
	
	
	protected boolean earlyExitCopy(Agent a, QueueSolver qs){

		
		// get the agent's next destination
		MultiagentTask t = a.getNextTask();

		// this task had better exist
		assert (t != null);
		// this task had better not be here.
		assert (t.destination != a.getState().getEndVertex());
		assert (a.getReservationTable() == super.reservations);
		SearchToQueue s = new SearchToQueue(a.getState().getEndVertex(),
				t.destination, t.timeAtDestination, a, a.getReservationTable(),
				false, true);

		org.cwilt.search.search.SearchAlgorithm alg = new org.cwilt.search.algs.basic.bestfirst.AStar(s,
				new org.cwilt.search.search.Limit(1000, Long.MAX_VALUE, Long.MAX_VALUE, false));
		List<org.cwilt.search.search.SearchState> path = alg.solve();
		// assert(path != null);
		if (path == null) {
			// try again next time?
			// System.err.println("Finding a return path failed");
			// System.err.println("Starting at " + this.target + " to " +
			// t.destination);
			// alg.printSearchData(System.err);
			// System.exit(1);

			MultiagentVertex newTarget = s.bestWaitLocation();
			if(newTarget == null)
				return false;
			
			SearchToQueue newPlan = new SearchToQueue(a.getState()
					.getEndVertex(), newTarget, t.timeAtDestination, a,
					a.getReservationTable(), false, true);
			org.cwilt.search.search.SearchAlgorithm newAlg = new org.cwilt.search.algs.basic.bestfirst.AStar(
					newPlan, new org.cwilt.search.search.Limit(1000, Long.MAX_VALUE,
							Long.MAX_VALUE, false));
			List<org.cwilt.search.search.SearchState> newPath = newAlg.solve();

			if (newPath != null) {
				assert (newPath != null);
				for (int i = 1; i < newPath.size(); i++) {
					MultiagentVertex original = ((QueueSearchNode) newPath
							.get(i - 1)).key.v;
					MultiagentVertex next = ((QueueSearchNode) newPath.get(i)).key.v;
					a.doMove(original, next);
				}
				a.getReservationTable().claimIndefinitely(newTarget, a.getCurrentTime(), a);
				qs.acceptAgent(a);
				return true;
			} else {
				return false;
			}
		} else {
			for (int i = 1; i < path.size(); i++) {
				MultiagentVertex original = ((QueueSearchNode) path.get(i - 1)).key.v;
				MultiagentVertex next = ((QueueSearchNode) path.get(i)).key.v;
				a.doMove(original, next);
			}
			AgentQueue q = a.getState().getEndVertex().getQueue();
			if (q != null) {
				// add this agent to the queue
				q.acceptAgent(a,
						a.getTask(a.getCurrentTaskID() + 1).timeAtDestination);
			}
		}

		return true;

	}
	
	@Override
	protected boolean pushAgentThrough(Agent a, QueueSolver qs) {
		// get the agent's next destination
		MultiagentTask t = a.getNextTask();

		// this task had better exist
		assert (t != null);
		// this task had better not be here.
		assert (t.destination != a.getState().getEndVertex());
		assert (a.getReservationTable() == super.reservations);
		
		if(d != null){
			for(MultiagentVertex v : d.entry){
				//TODO try to fix this agent
				boolean ready = d.canAcceptAgent(a, v, a.getCurrentTime(), 0);
				if(!ready)
					continue;
				a.doMove(this.target, v);
				d.acceptAgent(a, 0);
				return true;
			}
			a.addWaitMoves(1);
			return false;
		}
		
		SearchToQueue s = new SearchToQueue(a.getState().getEndVertex(),
				t.destination, t.timeAtDestination, a, a.getReservationTable(),
				false, true);

		org.cwilt.search.search.SearchAlgorithm alg = new org.cwilt.search.algs.basic.bestfirst.AStar(s,
				new org.cwilt.search.search.Limit(1000, Long.MAX_VALUE, Long.MAX_VALUE, false));
		List<org.cwilt.search.search.SearchState> path = alg.solve();
		// assert(path != null);
		if (path == null && d != null) {
			// try again next time?
			// System.err.println("Finding a return path failed");
			// System.err.println("Starting at " + this.target + " to " +
			// t.destination);
			// alg.printSearchData(System.err);
			// System.exit(1);

			MultiagentVertex newTarget = s.bestWaitLocation();
			
			if(newTarget == null) {
				a.addWaitMoves(1);
				return false;
			}
			SearchToQueue newPlan = new SearchToQueue(a.getState()
					.getEndVertex(), newTarget, a.getCurrentTime(), a,
					a.getReservationTable(), false, true);
			org.cwilt.search.search.SearchAlgorithm newAlg = new org.cwilt.search.algs.basic.bestfirst.AStar(
					newPlan, new org.cwilt.search.search.Limit(1000, Long.MAX_VALUE,
							Long.MAX_VALUE, false));
			List<org.cwilt.search.search.SearchState> newPath = newAlg.solve();

			if (newPath != null) {
				assert (newPath != null);
				for (int i = 1; i < newPath.size(); i++) {
					MultiagentVertex original = ((QueueSearchNode) newPath
							.get(i - 1)).key.v;
					MultiagentVertex next = ((QueueSearchNode) newPath.get(i)).key.v;
					a.doMove(original, next);
				}
				a.getReservationTable().claimIndefinitely(newTarget, a.getCurrentTime(), a);
				qs.acceptAgent(a);
				return true;
			} else {
				a.addWaitMoves(1);
				return false;
			}
		} else if (path == null && d == null) {
			a.addWaitMoves(1);
			return false;
		} else {
		
			for (int i = 1; i < path.size(); i++) {
				MultiagentVertex original = ((QueueSearchNode) path.get(i - 1)).key.v;
				MultiagentVertex next = ((QueueSearchNode) path.get(i)).key.v;
				a.doMove(original, next);
			}
			AgentQueue q = a.getState().getEndVertex().getQueue();
			if (q != null) {
				// add this agent to the queue
				q.acceptAgent(a,
						a.getTask(a.getCurrentTaskID() + 1).timeAtDestination);
			}
		}

		return true;
	}

	public void preparePaths() {
		// only prepare the paths for this queue once.
		if (pathsPrepared)
			return;
		this.pathsPrepared = true;

		if (queue.size() == 1) {
			entry.add(target);
		}

		// this identifies the queue's entrance
		for (MultiagentVertex v : queue) {
			if (v.equals(target)) {
				continue;
			}
			for (MultiagentVertex neighbor : v.getNeighbors()) {
				// this checks to see if the cell borders open space
				if (neighbor.getQueue() == null)
					continue;
				if (
				// this is here to pick up the case when the entrance is another
				// queue's exit
				(neighbor.getQueue() != this && neighbor.getQueue().target
						.equals(neighbor))) {
					entry.add(v);
					break;
				}
			}
		}
		/*
		 * Ideally there would be a good algorithm for routing the agents
		 * through the queue, but there is not one yet.
		 */
		/*
		 * Queue<MultiagentVertex> q = new LinkedList<MultiagentVertex>();
		 * q.add(target); while (!q.isEmpty()) { MultiagentVertex current =
		 * q.poll(); for (MultiagentVertex v : current.getNeighbors()) { //
		 * don't process the target vertex. if (v.equals(target)) continue; if
		 * (queue.contains(v) && !routingTable.containsKey(v)) {
		 * routingTable.put(v, current); q.add(v); } } }
		 */
	}

	@Override
	public Gateway getGateway() {
		// exit queues do not have gateways associated with them.
		return null;
	}

}
