package org.cwilt.search.domains.multiagent.solvers.queue;
import org.cwilt.search.domains.multiagent.problem.Agent;
import org.cwilt.search.domains.multiagent.problem.MultiagentVertex;
public final class QueuedAgent implements Comparable<QueuedAgent>, Cloneable {
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append(a);
		b.append(" @ ");
		b.append(v);
		b.append(" for ");
		b.append(this.queueTime);
		b.append("\n");
		return b.toString();
	}

	/**
	 * Current location of the agent within the queue
	 */
	public MultiagentVertex v;
	/**
	 * Agent moving around the queue
	 */
	public final Agent a;
	/**
	 * Amount of time the agent must sit on the goal vertex of the queue
	 */
	public int goalTime;
	/**
	 * Amount of time the agent has spent in total in this queue
	 */
	public int queueTime;

	public QueuedAgent(MultiagentVertex v, Agent a, int goalTime) {
		this.a = a;
		this.v = v;
		this.goalTime = goalTime;
		this.queueTime = 0;
	}

	private QueuedAgent(MultiagentVertex v, Agent a, int goalTime, int queueTime) {
		this.a = a;
		this.v = v;
		this.goalTime = goalTime;
		this.queueTime = queueTime;
	}

	@Override
	public int compareTo(QueuedAgent arg0) {
		int diff = arg0.queueTime - this.queueTime;
		if (diff != 0)
			return diff;
		else
			return this.a.getID() - arg0.a.getID();
	}

	public Object clone() {
		return new QueuedAgent(v, a, goalTime, queueTime);
	}
}
