/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.search;
import java.util.ArrayList;
import java.util.Iterator;

public class SearchNodeDepth extends SearchNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3339745874415563933L;
	protected final int depth;
	protected SearchNodeDepth(SearchNodeDepth parent, SearchState s, double g) {
		super(parent, s, g);
		if(parent != null)
			this.depth = parent.depth + 1;
		else
			this.depth = 0;
	}

	public static SearchNodeDepth makeInitial(SearchState s) {
		SearchNodeDepth n = new SearchNodeDepth(null, s, 0);
		return n;
	}
	
	public int getDepth(){
		return this.depth;
	}
	
	
	public ArrayList<SearchNodeDepth> reverseExpand(){
		ArrayList<SearchState.Child> baseChildren = s.reverseExpand();
		ArrayList<SearchNodeDepth> children = new ArrayList<SearchNodeDepth>();
		Iterator<SearchState.Child> it = baseChildren.iterator();
		while (it.hasNext()) {
			SearchState.Child c = it.next();
			children.add(new SearchNodeDepth(this, c.child, c.transitionCost + g));
		}
		return children;
	}
	
	public ArrayList<SearchNode> expand() {
		ArrayList<SearchState.Child> baseChildren = s.expand();
		ArrayList<SearchNode> children = new ArrayList<SearchNode>();
		Iterator<SearchState.Child> it = baseChildren.iterator();
		while (it.hasNext()) {
			SearchState.Child c = it.next();
			children.add(new SearchNodeDepth(this, c.child, c.transitionCost + g));
		}
		return children;
	}
}
