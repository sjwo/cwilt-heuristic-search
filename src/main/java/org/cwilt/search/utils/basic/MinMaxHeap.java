/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.utils.basic;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.Random;


public class MinMaxHeap<T extends Heapable> implements Iterable<T> {
	public void clear(){
		heap.clear();
		heapCount = 0;
		heap.add(null);
	}
	
	protected final ArrayList<T> heap;
	public final Comparator<T> c;
	protected int heapCount;

	public boolean isEmpty() {
		return heapCount == 0;
	}

	public int size(){
		return heapCount;
	}
	
	public T popLargest() {
		if(heapCount == 0)
			throw new EmptyStackException();
		int largestIX;
		if(heapCount == 1)
			largestIX = 1;
		else if(heapCount == 2)
			largestIX = 2;
		else if(c.compare(heap.get(2), heap.get(3)) < 0)
			largestIX = 3;
		else
			largestIX = 2;
		return remove(largestIX);
	}
	
	public T peekLargest() {
		if(heapCount == 0)
			return null;
		if (heapCount == 1)
			return heap.get(1);
		else if (heapCount == 2)
			return heap.get(2);
		else if (c.compare(heap.get(1), heap.get(2)) < 0)
			return heap.get(3);
		else
			return heap.get(2);
	}

	public MinMaxHeap(Comparator<T> c) {
		heap = new ArrayList<T>();
		heap.add(null);
		this.c = c;
		this.heapCount = 0;
	}

	private int getSmallestDescendent(int pos) {
		int[] desc = getDescendents(pos);
		assert (desc.length != 0);
		T smallest = heap.get(desc[0]);
		int smallestIX = desc[0];
		for (int i = 1; i < desc.length; i++) {
			if (c.compare(smallest, heap.get(desc[i])) > 0) {
				smallestIX = desc[i];
				smallest = heap.get(desc[i]);
			}
		}
		return smallestIX;
	}

	private int getLargestDescendent(int pos) {
		int[] desc = getDescendents(pos);
		assert (desc.length != 0);
		T largest = heap.get(desc[0]);
		int largestIX = desc[0];
		for (int i = 1; i < desc.length; i++) {
			if (c.compare(largest, heap.get(desc[i])) < 0) {
				largestIX = desc[i];
				largest = heap.get(desc[i]);
			}
		}
		return largestIX;
	}

	private ArrayList<Integer> getAllDescendants(int ix) {
		if (ix > heapCount)
			return new ArrayList<Integer>();
		else if (ix > heapCount / 2) {
			ArrayList<Integer> toReturn = new ArrayList<Integer>();
			toReturn.add(ix);
			return toReturn;
		} else {
			ArrayList<Integer> toReturn = new ArrayList<Integer>();
			toReturn.addAll(getAllDescendants(ix * 2));
			toReturn.addAll(getAllDescendants(ix * 2 + 1));
			toReturn.add(ix);
			return toReturn;
		}
	}

	public T get(int ix){
		if(ix < 0 || ix > heap.size())
			throw new ArrayIndexOutOfBoundsException();
		return heap.get(ix);
	}
	
	public T remove(int ix){
		
		if(ix < 0 || ix > heap.size())
			throw new ArrayIndexOutOfBoundsException();
		if(ix == heap.size() - 1){
			T toReturn = heap.remove(ix);
			toReturn.setHeapIndex(Heapable.NO_POS);
			heapCount --;
			return toReturn;
		}
		swap(heapCount, ix);
		T toReturn = heap.remove(heapCount);
		heapCount --;
		toReturn.setHeapIndex(Heapable.NO_POS);
		if(heapCount > 3){
			bubbleUp(ix);
			trickleDown(ix);
		}
		return toReturn;
	}
	
	private void trickleDownMax(int ix) {
		if (2 * ix <= heapCount) {
			int m = getLargestDescendent(ix);
			// grand child
			if (m > 2 * ix + 1) {
				if (c.compare(heap.get(m), heap.get(ix)) > 0) {
					swap(m, ix);
					if (c.compare(heap.get(m), heap.get(parent(m))) > 0)
						swap(m, parent(m));
					trickleDownMax(m);
				}

			}
			// child
			else {
				if (c.compare(heap.get(m), heap.get(ix)) > 0)
					swap(m, ix);
			}
		}
	}

	private static int log2(int i) {
		double d = (double) i;
		return (int) (Math.log(d) / Math.log(2.0));
	}

	private void trickleDown(int i) {
		if (log2(i) % 2 == 0)
			trickleDownMin(i);
		else
			trickleDownMax(i);
	}

	private void bubbleUpMin(int ix) {
		if (ix / 4 != 0) {
			if (c.compare(heap.get(ix), heap.get(ix / 4)) < 0) {
				swap(ix, ix / 4);
				bubbleUpMin(ix / 4);
			}
		}
	}


	public T pop() {
		if (heapCount == 0)
			throw new EmptyStackException();
		if (heapCount == 1) {
			T toReturn = heap.remove(heap.size() - 1);
			heapCount--;
			toReturn.setHeapIndex(Heapable.NO_POS);
			return toReturn;
		}
		swap(1, heap.size() - 1);
		T toReturn = heap.remove(heap.size() - 1);
		toReturn.setHeapIndex(Heapable.NO_POS);
		heapCount--;

		trickleDown(1);
		return toReturn;
	}

	private void bubbleUpMax(int ix) {
		if (ix / 4 != 0) {
			if (c.compare(heap.get(ix), heap.get(ix / 4)) > 0) {
				swap(ix, ix / 4);
				bubbleUpMax(ix / 4);
			}
		}
	}

	protected void bubbleUp(int ix) {
		if (log2(ix) % 2 == 0) {
			if (ix != 1 && c.compare(heap.get(ix / 2), heap.get(ix)) < 0) {
				swap(ix, ix / 2);
				bubbleUpMax(ix / 2);
			} else
				bubbleUpMin(ix);
		} else {
			if (ix != 1 && c.compare(heap.get(ix), heap.get(ix / 2)) < 0) {
				swap(ix, ix / 2);
				bubbleUpMin(ix / 2);
			} else {
				bubbleUpMax(ix);
			}
		}
	}

	private void trickleDownMin(int ix) {
		if (2 * ix <= heapCount) {
			int m = getSmallestDescendent(ix);
			// grand child
			if (m > 2 * ix + 1) {
				if (c.compare(heap.get(m), heap.get(ix)) < 0) {
					swap(m, ix);
					if (c.compare(heap.get(m/2), heap.get(m)) < 0)
						swap(m, parent(m));
					trickleDownMin(m);
				}
			}
			// child
			else {
				if (c.compare(heap.get(m), heap.get(ix)) < 0)
					swap(m, ix);
			}
		}
	}

	private void swap(int i1, int i2) {
		T temp = heap.get(i1);
		heap.set(i1, heap.get(i2));
		heap.set(i2, temp);
		heap.get(i1).setHeapIndex(i1);
		heap.get(i2).setHeapIndex(i2);
	}

	private int[] getDescendents(int i) {
		assert (heapCount >= 2 * i);
		if (heapCount >= i * 4 + 3)
			return new int[] { i * 2, i * 2 + 1, i * 4, i * 4 + 1, i * 4 + 2,
					i * 4 + 3 };
		else if (heapCount == i * 4 + 2)
			return new int[] { i * 2, i * 2 + 1, i * 4, i * 4 + 1, i * 4 + 2 };
		else if (heapCount == i * 4 + 1)
			return new int[] { i * 2, i * 2 + 1, i * 4, i * 4 + 1 };
		else if (heapCount == i * 4)
			return new int[] { i * 2, i * 2 + 1, i * 4 };
		else if (heapCount >= i * 2 + 1)
			return new int[] { i * 2, i * 2 + 1 };
		else
			return new int[] { i * 2 };
	}

	public void insert(T h) {
		assert (h.getHeapIndex() == Heapable.NO_POS);
		heap.add(h);
		h.setHeapIndex(heapCount + 1);
		bubbleUp(heapCount + 1);
		heapCount++;
	}

	private static int parent(int i) {
		return (i) / 2;
	}

	private boolean checkHeap(int ix) {
		ArrayList<Integer> children = getAllDescendants(ix * 2);
		children.addAll(getAllDescendants(ix * 2 + 1));
		boolean toReturn = true;
		Iterator<Integer> iter = children.iterator();
		while (iter.hasNext()) {
			int i = iter.next();
			if (log2(ix) % 2 == 0)
				toReturn = toReturn
						&& (c.compare(heap.get(ix), heap.get(i)) <= 0);
			else
				toReturn = toReturn
						&& (c.compare(heap.get(ix), heap.get(i)) >= 0);
		}
		return toReturn;
	}

	public boolean checkHeap() {

		boolean hp = true;
		for (int i = 1; i < heapCount; i++) {
			hp = hp && checkHeap(i);
		}

		boolean ix = true;
		for (int i = 1; i < heap.size(); i++) {
			ix = (ix && (heap.get(i).getHeapIndex() == i));
		}
		return ix && hp;
	}

	public static void main(String[] args) {
		Random r = new Random(0);
		MinHeap<HeapTestItem> mh = new MinHeap<HeapTestItem>(
				new HeapTestItem.HTComparator());
		MinMaxHeap<HeapTestItem> mmh = new MinMaxHeap<HeapTestItem>(
				new HeapTestItem.HTComparator());
		int testMax = 3000;
		for (int i = 0; i < testMax; i++) {
			int nextId = r.nextInt(50000);
			mh.insert(new HeapTestItem(nextId, nextId));
			mmh.insert(new HeapTestItem(nextId, nextId));
			if (!mh.checkHeap()) {
				System.err.println(i + " insert failed");
				System.exit(1);
			}
			if (!mmh.checkHeap()) {
				System.err.println(i + " insert failed (mmh)");
				System.exit(1);
			}
		}

		for (int i = 0; i < testMax; i++) {
			HeapTestItem testMH = mh.poll();
			HeapTestItem testMMH = mmh.pop();

			if (!testMH.equals(testMMH)) {
				System.err.println(i + " pop not the same");
				System.exit(1);
			}
			if (!mh.checkHeap()) {
				System.err.println(i + " pop failed");
				System.exit(1);
			}
			if (!mmh.checkHeap()) {
				System.err.println(i + " pop failed (mmh)");
				System.exit(1);
			}
		}
	}

	@Override
	public Iterator<T> iterator() {
		Iterator<T> iter = heap.iterator();
		//the first element is null, so have to burn it off
		iter.next();
		return iter;
	}

	public T peekSmallest() {
		if(heapCount == 0)
			return null;
		else 
			return heap.get(1);
	}
	
	public String toString(){
		StringBuffer b = new StringBuffer();
		
		for(T item : heap){
			b.append("\t");
			b.append(item);
			b.append("\n");
		}
		
		return b.toString();
	}

}
