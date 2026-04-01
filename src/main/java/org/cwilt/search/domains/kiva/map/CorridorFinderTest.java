package org.cwilt.search.domains.kiva.map;
import org.junit.Test;

public class CorridorFinderTest {
	@Test
	public void test(){
		CorridorFinder.DIRECTION d = CorridorFinder.DIRECTION.NORTH;
		
		CorridorFinder c1 = new CorridorFinder(d, 4, 7);
		System.err.println(c1);

		c1 = new CorridorFinder(d, 4, 9);
		System.err.println(c1);

	
		c1 = new CorridorFinder(d, 4, 11);
		System.err.println(c1);
}
}
