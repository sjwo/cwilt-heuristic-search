package org.cwilt.search.domains.multiagent.problem;
/**
 * Exception that is thrown if an agent is jammed inbetween two other agents
 * @author chris
 *
 */
public class JammedAgentException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1396157000422411264L;
	private final Agent a;
	public JammedAgentException(Agent a){
		this.a = a;
	}
	
	@Override
	public String getMessage(){
		StringBuffer b = new StringBuffer();
		b.append(a.toString());
		b.append(" got jammed");
		return b.toString();
	}
}
