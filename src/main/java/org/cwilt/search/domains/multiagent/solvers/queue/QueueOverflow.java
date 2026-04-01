package org.cwilt.search.domains.multiagent.solvers.queue;
public class QueueOverflow extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7165191389410061696L;

	private final int time;
	private final AgentQueue location;
	
	public QueueOverflow(int time, AgentQueue location){
		this.time = time;
		this.location = location;
	}
	
	public String getMessage(){
		StringBuffer b = new StringBuffer();
		b.append("Queue overflowed at time ");
		b.append(time);
		b.append(" Queue ");
		b.append(location);
		return b.toString();
	}
}
