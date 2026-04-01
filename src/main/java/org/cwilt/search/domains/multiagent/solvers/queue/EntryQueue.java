package org.cwilt.search.domains.multiagent.solvers.queue;
import java.util.HashMap;
import java.util.LinkedList;

import org.cwilt.search.domains.multiagent.problem.Agent;
import org.cwilt.search.domains.multiagent.problem.MultiagentVertex;
public class EntryQueue extends AgentQueue{
	
	private final HashMap<MultiagentVertex, Double> distances = new HashMap<MultiagentVertex, Double>();
	
	private static final class Node {
		public final MultiagentVertex v;
		public final double cost;
		public Node(MultiagentVertex v, double cost){
			this.cost = cost;
			this.v = v;
		}
	}

	public Double distanceToQueue(MultiagentVertex v){
		return distances.get(v);
	}
	
	private final void initDistances(){
		LinkedList<Node> open = new LinkedList<Node>();
		for(MultiagentVertex v : entry){
			open.add(new Node(v, 0));
		}
		while(!open.isEmpty()){
			Node n = open.poll();
			if(distances.containsKey(n.v))
				continue;
			distances.put(n.v, new Double(n.cost));
			for(MultiagentVertex v : n.v.getNeighbors()){
				if(!v.isForbidden(null, this.target) && v.getQueue() == null)
					open.add(new Node(v, n.cost + 1));
			}
		}
		
	}
	
	private final ExitQueue exit;
	
	public ExitQueue getExit(){
		return exit;
	}
	
	public EntryQueue(MultiagentVertex t, ExitQueue exit) {
		super(t);
		this.exit = exit;
	}
	
	/**
	 * 
	 * @param next Node after this queue's target vertex
	 * @return if this path's directionality matches that of this queue.
	 */
	public boolean pathMatches(MultiagentVertex next){
		if(queue.contains(next))
			return false;
		else
			return true;
	}
	
	@Override
	public Gateway getGateway(){
		if(gateway == null)
			throw new RuntimeException("No gateway for this queue");
		else
			return gateway;
	}
	
	public void setGateway(EntryQueue other){
		this.gateway = new Gateway(this, other);
		other.gateway = this.gateway;
	}

	private Gateway gateway;

	@Override
	protected boolean pushAgentThrough(Agent a, QueueSolver s) throws QueueOverflow {
		boolean ready = exit.canAcceptAgent(a, this.exit.entry.get(0), a.getCurrentTime(), 0);
		if(!ready)
			throw new QueueOverflow(super.currentTime, this);
		a.doMove(this.target, this.exit.entry.get(0));
		exit.acceptAgent(a, 0);
		a.doTask(true);
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
				if (neighbor.getQueue() == null ||
				// this is here to pick up the case when the entrance is another
				// queue's exit
						(neighbor.getQueue() != this && neighbor.getQueue().target
								.equals(neighbor))) {
					entry.add(v);
					break;
				}
			}
		}
		
		this.initDistances();
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

	
}
