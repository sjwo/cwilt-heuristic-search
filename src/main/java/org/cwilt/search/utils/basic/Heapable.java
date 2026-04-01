/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.utils.basic;
public interface Heapable {
	public static final int NO_POS = -1;
	public int getHeapIndex();
	public void setHeapIndex(int ix);
}
