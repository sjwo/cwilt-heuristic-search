package org.cwilt.search.domains.citynav;
import org.junit.Test;

import org.cwilt.search.search.SearchState;
import org.cwilt.search.search.SearchState.Child;
public class CitynavTest {
	Citynav cn = new Citynav(150, 150, 5, 5, 100, 1);
	@Test
	public void backwardExpTest(){
		SearchState root = cn.getInitial();
		
		
		for (Child c : root.expand()) {
			SearchState s = c.child;
			boolean foundParent = false;
			for (Child recv : s.reverseExpand()) {
				if (recv.child.getKey().equals(root.getKey())) {
					foundParent = true;
					double toChild = recv.transitionCost;
					double toParent = c.transitionCost;
					if(toChild != toParent){
						System.err.printf("%f %f\n", toParent, toChild);
					}
					assert (toParent == toChild);
				}
			}
			assert (foundParent);
		}

		// get all of the reverse expanded children
		for (Child c : root.reverseExpand()) {
			SearchState s = c.child;
			boolean foundParent = false;
			for (Child recv : s.expand()) {
				if (recv.child.getKey().equals(root.getKey())) {
					double toChild = recv.transitionCost;
					double toParent = c.transitionCost;
					assert (toParent == toChild);
					foundParent = true;
				}
			}
			assert (foundParent);
		}
	}
}
