package org.cwilt.search.utils.experimental;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Random;

import org.cwilt.search.utils.basic.FloatHistogram;
import org.cwilt.search.utils.basic.Heapable;
import org.cwilt.search.utils.basic.MinHeap;
public class FasterFloatHeap<T extends BinHeapable> implements Queue<T> {
	private static final int POINTERSTACK = 23;
	private static final int RANDOMSEED = 33333;

	private final double min, max;

	public FasterFloatHeap(double min, double max, Comparator<T> c, int s) {
		this.SPLIT = s;
		this.c = c;
		this.min = min;
		this.max = max;
		this.r = new Random(RANDOMSEED);
		this.head = new Bucket<T>(min, max, POINTERSTACK, true, c);
		this.tail = head;
	}

	private final Random r;
	private Bucket<T> head, tail;

	@SuppressWarnings("unchecked")
	private Bucket<T> findBucket(double value) {
		if (value <= head.min)
			return head;
		if (value > tail.min)
			return tail;

		Bucket<T> current = head;
		Bucket<T> toReturn = null;

		if (head.validF(value)) {
			return head;
		}

		for (int i = POINTERSTACK - 1; i >= 0;) {

			// check for equality
			Bucket<T> next = current.next[i];

			if (next == null) {
				System.err.println("failed to find a bucket for this value: "
						+ value);
				assert (false);
			}

			// check the next, see if it is larger, or smaller
			if (next.validF(value)) {
				toReturn = next;
				break;
			}

			// if it is larger, move next
			if (next.max <= value) {
				current = next;
			}
			// if it is smaller, move down
			else {
				i--;
			}
		}
		assert (toReturn != null);
		assert (toReturn.validF(value));
		assert (toReturn != null);
		// check if this thing belongs in this bucket, or the previous one.
		if (current.min == current.max && value == current.min)
			return current;
		if (value < current.max)
			return current;
		if (value == current.max) {
			// check to see if should return the next bucket.
			Bucket<T> next = current.next[0];
			if (next != null) {
				if (next.min == next.max)
					toReturn = next;
			}

		}
		assert(toReturn.validF(value));
		return toReturn;
	}

	private final Comparator<T> c;

	private static final int MINSIZE = 1;

	private final int nextPointersize() {
		int max = (1 << (POINTERSTACK - 1)) - 1;
		int start = r.nextInt(max);

		int returning = (POINTERSTACK - 1)
				- (32 - Integer.numberOfLeadingZeros(start));
		assert (returning >= 0);
		assert (returning <= (POINTERSTACK - 1));
		return returning + 1;
	}

	@SuppressWarnings({ "unchecked" })
	private Bucket<T> splitBucket(Bucket<T> b) {
		double[] samples = b.simpleSample(r);
		double avg = samples[Bucket.SAMPLES / 2];

		boolean orderLeft = b.queue instanceof org.cwilt.search.utils.basic.MinHeap;

		boolean isTail = b == tail;
		boolean isHead = b == head;

		Bucket<T> left;
		Bucket<T> right;

		// adjust the head and tail buckets if necessary
		if (isHead) {
			head = new Bucket<T>(b.min, avg, POINTERSTACK, orderLeft, c);
			left = head;
		} else {
			left = new Bucket<T>(b.min, avg, this.nextPointersize(), orderLeft,
					c);
		}
		if (isTail) {
			tail = new Bucket<T>(avg, b.max, POINTERSTACK, false, c);
			right = tail;
		} else {
			right = new Bucket<T>(avg, b.max, this.nextPointersize(), false, c);
		}

		for (T item : b.queue) {
			double value = item.getF();
			item.setHeapIndex(Heapable.NO_POS);
			Bucket<T> best = FasterFloatHeap.getBustBucket(value, left, right, null);
			
			best.queue.add(item);
//			if (left.validF(value))
//				left.queue.add(item);
//			else if (right.validF(value))
//				right.queue.add(item);
//			else {
//				assert (false);
//			}
		}
		Bucket<T> center = null;
		// check the distribution
		if (left.queue.size() < MINSIZE || right.queue.size() < MINSIZE) {
			// it looks like the splitting failed, have to try again.
			FloatHistogram h = new FloatHistogram(samples);
			double splitPoint = h.mostCommon();

			// One of the buckets might be a duplicate.

			// adjust the head and tail buckets if necessary
			if (isHead) {
				head = new Bucket<T>(b.min, splitPoint, POINTERSTACK,
						orderLeft, c);
				left = head;
			} else {
				left = new Bucket<T>(b.min, splitPoint, this.nextPointersize(),
						orderLeft, c);
			}
			if (isTail) {
				tail = new Bucket<T>(splitPoint, b.max, POINTERSTACK, false, c);
				right = tail;
			} else {
				right = new Bucket<T>(splitPoint, b.max,
						this.nextPointersize(), false, c);
			}
			if (splitPoint == b.min || splitPoint == b.max) {
				center = null;
			} else {
				center = new Bucket<T>(splitPoint, splitPoint,
						this.nextPointersize(), false, c);
			}

			// have to repopulate the buckets.
			for (T item : b.queue) {
				double value = item.getF();
				item.setHeapIndex(Heapable.NO_POS);
				
				Bucket<T> best = FasterFloatHeap.getBustBucket(value, left, right, center);
				best.queue.add(item);

//				if (center != null && center.validF(value))
//					center.queue.add(item);
//				else if (left.validF(value))
//					left.queue.add(item);
//				else if (right.validF(value)) {
//					right.queue.add(item);
//				} else {
//					assert (false);
//				}
			}

			// if the left bucket is a singleton bucket, have to reach into the
			// previous bucket to find the items that now belong there.
			
			if(left.max == left.min){
				assert(false);
			}
		}

		// fix the left one going backwards
		for (int i = 0; i < left.prev.length; i++) {
			if (i < b.prev.length) {
				left.prev[i] = b.prev[i];
				if (left.prev[i] != null)
					b.prev[i].next[i] = left;
			} else {
				// old one is too short
				Bucket<T> current = left.prev[i - 1];
				while (current.prev.length <= i) {
					current = current.prev[i - 1];
				}
				left.prev[i] = current;
				current.next[i] = left;
			}
		}
		// fix the right one going forwards
		for (int i = 0; i < right.next.length; i++) {
			if (i < b.next.length) {
				right.next[i] = b.next[i];
				if (b.next[i] != null)
					b.next[i].prev[i] = right;
			} else {
				// old one is too short
				Bucket<T> current = right.next[i - 1];
				while (current.next.length <= i) {
					current = current.next[i - 1];
				}
				right.next[i] = current;
				current.prev[i] = right;
			}
		}
		if (center != null) {
			// fix the center one going left
			for (int i = 0; i < center.next.length; i++) {
				Bucket<T> leftBucket;
				leftBucket = left;
				if (i < leftBucket.next.length) {
					// left is tall enough
					center.prev[i] = leftBucket;
				} else {
					// left is too short
					Bucket<T> current = center.prev[i - 1];
					while (current.prev.length <= i) {
						current = current.prev[i - 1];
					}
					center.prev[i] = current;
					current.next[i] = center;
				}
			}
			// fix the center one going right
			for (int i = 0; i < center.next.length; i++) {
				Bucket<T> rightBucket;
				rightBucket = right;
				if (i < rightBucket.next.length) {
					// left is tall enough
					center.next[i] = rightBucket;
				} else {
					// left is too short
					Bucket<T> current = center.next[i - 1];
					while (current.next.length <= i) {
						current = current.next[i - 1];
					}
					center.next[i] = current;
					current.prev[i] = center;
				}
			}
		}

		// left going forwards (checking for the center, if it exists
		for (int i = 0; i < left.prev.length; i++) {
			Bucket<T> rightBucket;
			if (center != null)
				rightBucket = center;
			else
				rightBucket = right;
			if (i < rightBucket.next.length) {
				// right is tall enough
				left.next[i] = rightBucket;
			} else {
				// right is too short
				assert (left.next.length > i - 1);
				Bucket<T> current = left.next[i - 1];
				assert (current != null);
				assert (current.next != null);
				while (current.next.length <= i) {
					current = current.next[i - 1];
					assert (current != null);
					assert (current.next != null);
				}
				left.next[i] = current;
				current.prev[i] = left;
			}
		}
		// right going backwards
		for (int i = 0; i < right.next.length; i++) {
			Bucket<T> leftBucket;
			if (center != null)
				leftBucket = center;
			else
				leftBucket = left;
			if (i < leftBucket.next.length) {
				// left is tall enough
				right.prev[i] = leftBucket;
			} else {
				// left is too short
				Bucket<T> current = right.prev[i - 1];
				while (current.prev.length <= i) {
					current = current.prev[i - 1];
				}
				right.prev[i] = current;
				current.next[i] = right;
			}
		}

		int newMax = Math.max(left.next.length, right.next.length);
		if (center != null)
			newMax = Math.max(newMax, center.next.length);
		for (int i = newMax; i < b.next.length; i++) {
			// this guy is now gone, so need to connect the things that used to
			// go through it.
			Bucket<T> oldLeft = b.prev[i];
			Bucket<T> oldRight = b.next[i];
			oldLeft.next[i] = oldRight;
			oldRight.prev[i] = oldLeft;
		}

		return left;

	}

	private static int id;

	@SuppressWarnings("rawtypes")
	public static class Bucket<Type extends BinHeapable> {

		private static final int SAMPLES = 51;

		public double[] simpleSample(Random r) {
			double samples[] = new double[SAMPLES];
			ArrayList<Type> bucket = null;
			if (queue instanceof ArrayQueue) {
				bucket = (ArrayQueue<Type>) queue;
			} else {
				MinHeap<Type> h = (MinHeap<Type>) queue;
				bucket = h.getBackbone();
			}

			for (int i = 0; i < SAMPLES; i++) {
				samples[i] = bucket.get(r.nextInt(bucket.size())).getF();
			}

			Arrays.sort(samples);
			return samples;
		}

		private Bucket next[];
		private Bucket prev[];

		private final int bucketID;

		public String toString() {
			StringBuffer b = new StringBuffer();
			b.append("[");
			b.append(bucketID);
			b.append("]");
			b.append("(");
			b.append(min);
			b.append(" ");
			b.append(max);
			b.append(") with ");
			b.append(queue.size());
			b.append(" height ");
			b.append(next.length);
			b.append("\n");
			return b.toString();
		}

		public Queue<Type> queue;

		public void clear() {
			queue.clear();
		}

		public boolean validF(double f) {
			if (min == max)
				return f == min;
			else {
				if (f < max && f > min)
					return true;
				else if (f == max) {
					// check the next bucket
					Bucket nextBucket = next[0];
					if (nextBucket == null)
						return true;
					else if (nextBucket.min == nextBucket.max)
						return false;
					else
						return true;
				}
				return false;
			}
		}

		private double min;
		private double max;

		public Bucket(double min, double max, int height, boolean ordered,
				Comparator<Type> c) {
			this.next = new Bucket[height];
			this.prev = new Bucket[height];
			this.bucketID = id++;
			this.max = max;
			this.min = min;
			assert (min <= max);

			if (ordered)
				queue = new MinHeap<Type>(c);
			else
				queue = new ArrayQueue<Type>();
		}

		public boolean canSplit() {
			return this.min != this.max;
		}
	}
	@SuppressWarnings("rawtypes")
	private static Bucket getBustBucket(double f, Bucket left, Bucket right, Bucket center){
		if(right.validF(f))
			return right;
		if(center != null && center.validF(f))
			return center;
		if(left.validF(f))
			return left;
		assert(false);
		return null;
	}
	@Override
	public boolean addAll(Collection<? extends T> arg0) {
		for (T item : arg0)
			this.add(item);
		return true;
	}

	private int size;

	@SuppressWarnings("unchecked")
	@Override
	public void clear() {
		this.size = 0;

		Bucket<T> b = head;
		while (b != null) {
			b.clear();
			b = b.next[0];
		}

		this.head = new Bucket<T>(min, max, POINTERSTACK, true, c);
		this.tail = this.head;
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		for (Object o : arg0) {
			if (!contains(o))
				return false;
		}
		return true;
	}

	@Override
	public boolean isEmpty() {
		return this.size == 0;
	}

	@Override
	public Iterator<T> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		boolean removed = false;
		;
		for (Object o : arg0) {
			if (this.remove(o))
				removed = true;
		}
		return removed;
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <Type> Type[] toArray(Type[] arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(Object arg0) {
		@SuppressWarnings("unchecked")
		T r = (T) arg0;
		int ix = r.getHeapIndex();
		if (ix == Heapable.NO_POS)
			return false;
		else {
			Bucket<T> b = this.findBucket(r.getF());
			return b.queue.contains(arg0);
		}
	}

	@Override
	public boolean remove(Object arg0) {
		// assert(check());
		if (arg0 == null)
			return false;
		@SuppressWarnings("unchecked")
		T r = (T) arg0;

		if (r.getHeapIndex() == Heapable.NO_POS)
			return false;

		Bucket<T> b = this.findBucket(r.getF());
		boolean removed = b.queue.remove(arg0);
		if (removed)
			size--;

		// assert(check());
		return removed;
	}

	private final int SPLIT;

	@SuppressWarnings("unchecked")
	@Override
	public boolean add(T arg0) {

		assert (check());
		double value = arg0.getF();
		Bucket<T> bucket = findBucket(value);

		if (bucket == head && !bucket.validF(value))
			bucket.min = value - 1;
		if (bucket == tail && !bucket.validF(value))
			bucket.max = value + 1;

		assert (bucket.validF(value));
		this.size++;
		boolean toReturn;
		if (bucket.queue.size() >= SPLIT) {
			if (bucket.canSplit()) {
				Bucket<T> left = splitBucket(bucket);
				Bucket<T> next = left.next[0];
				if (left.validF(value)) {
					toReturn = left.queue.add(arg0);
				} else if (next.validF(value)) {
					toReturn = next.queue.add(arg0);
				} else {
					toReturn = next.next[0].queue.add(arg0);
				}
			} else {
				// all of the items have the same cost, so just add it.
				toReturn = bucket.queue.add(arg0);
			}
		} else {
			toReturn = bucket.queue.add(arg0);
		}
		
		if(head.queue.isEmpty())
			this.smashHead();
		assert (check());
		
		
		return toReturn;
	}

	@Override
	public T peek() {
		assert (!head.queue.isEmpty());
		return head.queue.peek();
	}

	/**
	 * Compacts the head into the next element in the list.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void smashHead() {
		if (this.isEmpty())
			return;

		Bucket[] currentNextPointers = head.next;

		assert (this.head.queue.isEmpty());
		assert (this.head != null);
		assert (this.head.queue != null);
		while (this.head.queue.isEmpty()) {
			if (this.head.next[0] == null)
				break;
			this.head = this.head.next[0];
		}
		// head is now pointing to a non empty thing, but have to reset the
		// minimum value of the head.

		Bucket[] oldNext = head.next;
		head.next = currentNextPointers;
		head.prev = new Bucket[POINTERSTACK];
		for (int i = 0; i < oldNext.length; i++) {
			head.next[i] = oldNext[i];
		}
		for (int i = oldNext.length; i < POINTERSTACK; i++) {
			Bucket current = head.next[i - 1];
			while (current.next.length <= i) {
				current = current.next[i - 1];
			}
			assert (current != null);
			assert (current.next != null);
			// assert(current.next[i] != null);
			current.prev[i] = head;
			head.next[i] = current;
		}

		head.queue = new MinHeap<T>(c, (ArrayQueue<T>) head.queue);
	}

	@Override
	public T poll() {
		assert (!head.queue.isEmpty());
		this.size--;
		T toReturn = head.queue.poll();
		if (this.size == 0) {
			this.clear();
		}
		if (head.queue.isEmpty()) {
			this.smashHead();
		}
		return toReturn;
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
		return add(arg0);
	}

	@Override
	public T remove() {
		T toReturn = poll();
		if (toReturn == null)
			throw new NoSuchElementException();
		return toReturn;
	}

	@SuppressWarnings("unchecked")
	public String toString() {
		StringBuffer b = new StringBuffer();
		Bucket<T> current = head;
		while (current != null) {
			b.append(current);
			current = current.next[0];
		}

		return b.toString();

	}

	@SuppressWarnings({ "unchecked", "unused" })
	private boolean indexCheck() {
		// item count and integrity check

		for (int i = 0; i < 1; i++) {
			Bucket<T> current = head;
			int itemCount = 0;
			while (current != null) {
				for (T item : current.queue) {
					double value = item.getF();
					assert (current.validF(value));
				}
				itemCount += current.queue.size();
				Bucket<T> next = current.next[i];
				if (next != null) {
					assert (next.prev[i] == current);
				}
				current = next;
			}
			if (i == 0)
				assert (itemCount == this.size());
		}

		for (int i = 0; i < POINTERSTACK; i++) {
			Bucket<T> current = tail;
			while (current != null) {
				Bucket<T> prev = current.prev[i];
				if (prev != null) {
					assert (prev.next[i] == current);
				}
				current = prev;
			}
		}
		return true;
	}

	@SuppressWarnings({ "unchecked", "unused" })
	private boolean bucketCheck() {
		{
			Bucket<T> current = head;
			while (current != null) {
				Bucket<T> next = current.next[0];
				if (next != null) {
					assert (current.max == next.min);
					assert (current.min != next.max);
				}
				current = next;
			}
		}
		for (int i = 0; i < POINTERSTACK; i++) {
			Bucket<T> current = head;
			while (current != null) {
				Bucket<T> next = current.next[i];
				if (next != null) {
					assert (next.prev[i] == current);
				}
				current = next;
			}
		}
		return true;
	}


	public boolean check() {
		return true;
	}

}
