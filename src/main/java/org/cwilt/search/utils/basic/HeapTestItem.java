/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.utils.basic;
import java.util.Comparator;
import java.util.Random;

import org.cwilt.search.utils.experimental.BinHeapable;
public class HeapTestItem implements BinHeapable, Cloneable {

	public Object clone(){
		return new HeapTestItem(priority1, priority2);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(priority1);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(priority2);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HeapTestItem other = (HeapTestItem) obj;
		if (Double.doubleToLongBits(priority1) != Double
				.doubleToLongBits(other.priority1))
			return false;
		if (Double.doubleToLongBits(priority2) != Double
				.doubleToLongBits(other.priority2))
			return false;
		return true;
	}

	private int heapIX;
	private final double priority1;
	private final double priority2;

	@Override
	public int getHeapIndex() {
		return heapIX;
	}

	@Override
	public void setHeapIndex(int ix) {
		heapIX = ix;
	}

	public HeapTestItem(Random r, double max){
		heapIX = Heapable.NO_POS;
		this.priority1 = r.nextDouble() * max;
		this.priority2 = r.nextDouble() * max;
	}
	
	public HeapTestItem(double p1, double p2) {
		heapIX = Heapable.NO_POS;
		this.priority1 = p1;
		this.priority2 = p2;
	}

	public static class HTComparator implements Comparator<HeapTestItem> {

		@Override
		public int compare(HeapTestItem o1, HeapTestItem o2) {
			return o1.compareTo(o2);
		}

	}

	public int compareTo(HeapTestItem o) {
		if (this.priority1 < o.priority1) {
			return -1;
		} else if (this.priority1 > o.priority1) {
			return 1;
		} else if(this.priority1 == o.priority1){
			if (this.priority2 < o.priority2) {
				return -1;
			} else if (this.priority2 > o.priority2) {
				return 1;
			} else if(this.priority2 == o.priority2){
				return 0;
			}
		}
		return 0;
	}

	public String toString() {
		return "priority1 " + priority1 + " priority2 " + priority2 + " index " + heapIX + "\n";
	}

	@Override
	public double getF() {
		return this.priority1;
	}

	@Override
	public double getG() {
		return this.priority2;
	}

}