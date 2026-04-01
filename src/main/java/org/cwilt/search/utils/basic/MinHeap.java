/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.utils.basic;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Random;

public class MinHeap<T extends Heapable> implements Queue<T> {
	
	public int compare(T first, T second){
		return c.compare(first, second);
	}
	
	protected void setHeapIndex(Heapable item, int ix) {
		item.setHeapIndex(ix);
	}

	protected int getHeapIndex(Heapable item) {
		return item.getHeapIndex();
	}

	public void clear() {
		for (T item : heap) {
			setHeapIndex(item, Heapable.NO_POS);
		}
		heap.clear();
		firstEmpty = 0;
	}

	private final Comparator<T> c;
	protected final ArrayList<T> heap;
	private int firstEmpty;

	public boolean isEmpty() {
		return firstEmpty == 0;
	}

	public MinHeap(Comparator<T> c, ArrayList<T> l) {
		this.c = c;
		heap = new ArrayList<T>(l);
		firstEmpty = l.size();
		this.reHeapify();
		assert(this.c != null);
	}
	
	public MinHeap(Comparator<T> c) {
		this.c = c;
		heap = new ArrayList<T>();
		firstEmpty = 0;
		assert(this.c != null);
	}

	public void insert(T h) {
		assert (getHeapIndex(h) == Heapable.NO_POS);
		heap.add(h);
		setHeapIndex(h, firstEmpty);
		bubbleUp(firstEmpty);
		firstEmpty++;
	}

	private static int parent(int i) {
		return (i - 1) / 2;
	}

	public void bubbleUp(int ix) {
		if (ix == 0)
			return;
		T child = heap.get(ix);
		T parent = heap.get(parent(ix));

		while (c.compare(child, parent) < 0 && ix != 0) {
			heap.set(ix, parent);
			setHeapIndex(parent, ix);
			ix = parent(ix);
			// child = parent;
			parent = (T) heap.get(parent(ix));
		}
		heap.set(ix, child);
		setHeapIndex(child, ix);
	}

	private void pushDown(int ix) {
		T parent = heap.get(ix);
		int lcIX = ix * 2 + 1;
		int rcIX = ix * 2 + 2;
		T leftChild = null;
		T rightChild = null;
		if (lcIX < heap.size())
			leftChild = heap.get(lcIX);
		if (rcIX < heap.size())
			rightChild = heap.get(rcIX);
		if (leftChild == null)
			return;
		int smallerIX = 0;
		if (rightChild == null)
			smallerIX = lcIX;
		else if (c.compare(leftChild, rightChild) <= 0)
			smallerIX = lcIX;
		else if (c.compare(leftChild, rightChild) > 0)
			smallerIX = rcIX;
		assert (smallerIX != 0);
		T smallerChild = heap.get(smallerIX);
		if (c.compare(parent, smallerChild) > 0) {
			// swap them
			heap.set(smallerIX, parent);
			setHeapIndex(parent, smallerIX);
			heap.set(ix, smallerChild);
			setHeapIndex(smallerChild, ix);
			// pushDown on the child
			pushDown(smallerIX);
		}
	}

	public boolean checkHeap() {
		boolean hp = checkHeap(0);

		boolean ix = true;
		for (int i = 0; i < heap.size(); i++) {
			ix = (ix && (getHeapIndex(heap.get(i)) == i));
		}
		return ix && hp;
	}

	private boolean checkHeap(int ix) {
		if (ix >= heap.size())
			return true;

		T parent = heap.get(ix);
		int lcIX = ix * 2 + 1;
		int rcIX = ix * 2 + 2;
		T leftChild = null;
		T rightChild = null;
		if (lcIX < heap.size())
			leftChild = heap.get(lcIX);
		if (rcIX < heap.size())
			rightChild = heap.get(rcIX);
		if (leftChild == null)
			return true;
		if (c.compare(parent, leftChild) > 0)
			return false;
		if (rightChild == null)
			return true;
		if (c.compare(parent, rightChild) > 0)
			return false;
		return (checkHeap(2 * ix + 1)) && checkHeap(2 * ix + 2);
	}

	public T poll() {
		if (heap.size() == 0)
			return null;
		if (heap.size() == 1) {
			firstEmpty--;
			T toReturn = heap.remove(0);
			setHeapIndex(toReturn, Heapable.NO_POS);
			assert (getHeapIndex(toReturn) == Heapable.NO_POS);
			return toReturn;
		}
		T toReturn = (T) heap.get(0);
		heap.set(0, heap.remove(heap.size() - 1));
		setHeapIndex(heap.get(0), 0);
		firstEmpty--;
		pushDown(0);
		setHeapIndex(toReturn, Heapable.NO_POS);
		return toReturn;
	}

	public T peek() {
		if (heap.size() == 0)
			return null;
		else
			return heap.get(0);
	}

	/**
	 * removes the item at the specified location
	 * 
	 * @param ix
	 *            location to remove
	 * @return 
	 */
	public T removeAt(int ix) {
		assert (ix >= 0);
		T toReturn = heap.get(ix);
		setHeapIndex(toReturn, Heapable.NO_POS);
		if (heap.size() - 1 != ix) {
			heap.set(ix, heap.get(heap.size() - 1));
			setHeapIndex(heap.get(ix), ix);
		}
		firstEmpty--;
		heap.remove(heap.size() - 1);
		if (ix < heap.size()) {
			bubbleUp(ix);
			pushDown(ix);
		}
		assert (getHeapIndex(toReturn) == Heapable.NO_POS);
		return toReturn;
	}

	public static void main(String args[]) {

		Random r = new Random(0);
		MinHeap<HeapTestItem> mh = new MinHeap<HeapTestItem>(
				new HeapTestItem.HTComparator());
		for (int i = 0; i < 100; i++) {
			mh.insert(new HeapTestItem(r.nextDouble(), r.nextDouble()));
			if (!mh.checkHeap()) {
				System.err.println(i + " failed");
				System.exit(1);
			}
		}
		for (int i = 0; i < 100; i++) {
			mh.poll();
			if (!mh.checkHeap()) {
				System.err.println(i + " pop failed");
				System.exit(1);
			}

		}

	}

	/**
	 * If all of the objects in the MinHeap have had their priorities changed,
	 * the heap may need to be reordered. This function will do all of that at
	 * once in linear time.
	 */
	public void reHeapify() {
		for(int i = heap.size() - 1; i >= 0; i--){
			pushDown(i);
		}
	}

	@Override
	public boolean add(T arg0) {
		insert(arg0);
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends T> arg0) {
		throw new UnsupportedOperationException();
	}


	@Override
	public boolean containsAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<T> iterator() {
		return heap.iterator();
	}

	@Override
	public boolean contains(Object arg0) {
		try{
			@SuppressWarnings("unchecked")
			T r = (T) arg0;
			int ix = r.getHeapIndex();
			if(ix == Heapable.NO_POS)
				return false;
			else
				return heap.get(ix).equals(arg0);
		} catch (ClassCastException c){
			return false;
		}
	}
	@Override
	public boolean remove(Object arg0) {
		if(arg0 == null)
			return false;
		if(arg0 instanceof Integer){
			removeAt((Integer) arg0);
			return true;
		}
		try{
			@SuppressWarnings("unchecked")
			T r = (T) arg0;
			int ix = this.getHeapIndex(r);
			assert(ix != Heapable.NO_POS);
			removeAt(ix);
			return true;
		} catch (ClassCastException c){
			return false;
		}
	}
	
	public ArrayList<T> getBackbone(){
		return this.heap;
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		throw new NotImplementedException();
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		throw new NotImplementedException();
	}

	@Override
	public int size() {
		return firstEmpty;
	}

	@Override
	public Object[] toArray() {
		return heap.toArray();
	}

	@Override
	public <Type> Type[] toArray(Type[] a) {
		return heap.toArray(a);
	}

	@Override
	public T element() {
		T toReturn = peek();
		if (toReturn == null)
			throw new NoSuchElementException();
		return toReturn;
	}

	@Override
	public boolean offer(T arg0) {
		if (arg0 == null)
			throw new NullPointerException();
		return add(arg0);
	}

	@Override
	public T remove() {
		T toReturn = poll();
		if (toReturn == null)
			throw new NoSuchElementException();
		return toReturn;
	}

	public String toString(){
		return heap.toString();
	}
}
