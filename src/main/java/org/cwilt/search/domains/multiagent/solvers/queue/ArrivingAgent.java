package org.cwilt.search.domains.multiagent.solvers.queue;
import org.cwilt.search.domains.multiagent.problem.Agent;
import org.cwilt.search.domains.multiagent.problem.MultiagentVertex;
public final class ArrivingAgent implements Comparable<ArrivingAgent> {
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append(agent);
		b.append(" @ ");
		b.append(arrivalVertex);
		b.append(" @ ");
		b.append(arrivalTime);
		b.append("\n");
		return b.toString();
	}

	public final MultiagentVertex arrivalVertex;
	public final Agent agent;
	public final int goalTime;
	public final int arrivalTime;

	public ArrivingAgent(Agent a, int t, int goalTime) {
		this.arrivalVertex = a.getState().getEndVertex();
		this.agent = a;
		this.arrivalTime = t;
		this.goalTime = goalTime;
	}

	public ArrivingAgent(Agent a, MultiagentVertex v, int t, int goalTime) {
		this.arrivalVertex = v;
		this.agent = a;
		this.arrivalTime = t;
		this.goalTime = goalTime;
	}

	@Override
	public int compareTo(ArrivingAgent arg0) {
		int diff = this.arrivalTime - arg0.arrivalTime;
		if (diff != 0)
			return diff;
		return this.agent.getID() - arg0.agent.getID();
	}
}
