package org.cwilt.search.utils.experimental;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;

import org.junit.Test;

import org.cwilt.search.utils.basic.MinHeap;
import org.cwilt.search.utils.basic.MinMaxHeap;
import org.cwilt.search.utils.basic.PairingHeap;
import org.cwilt.search.utils.experimental.BinHeapTest.BHT;
public class DBinHeapTest {
	MinHeap<BHT> mh = new MinHeap<BHT>(new BHT.FGComparator());
	DBinHeap<BHT> db = new DBinHeap<BHT>();
	BinHeap<BHT> fb = new BinHeap<BHT>(BinHeap.HEAPTYPE.FHEAP, 0);
	PairingHeap<BHT> ph = new PairingHeap<BHT>(new BHT.FGComparator());
	
	Random r = new Random(0);

	@Test
	public void zeroAddTest() {
		BHT b1 = new BHT(0, 0);
		BHT b2 = new BHT(1, 1);
		db.add(b1);
		assert (db.contains(b1));
		db.add(b2);
		assert (db.contains(b2));
		BHT b1polled = db.poll();
		BHT b2polled = db.poll();
		assert (b1polled.equals(b1polled));
		assert (b2polled.equals(b2polled));

	}

	@Test
	public void simplePeekTest() {
		BHT b = new BHT(0, 0);
		db.add(b);
		assert (db.contains(b));
		BHT b2 = db.peek();

		assert (b2.equals(b));
		assert (b2 == b);
	}

	@Test
	public void simplePollTest() {
		BHT b = new BHT(0, 0);
		db.add(b);
		assert (db.contains(b));
		BHT b2 = db.poll();

		assert (b2.equals(b));
		assert (b2 == b);
	}

	@Test
	public void removeObjectTest() {
		// if trying to find something, have to actually have the item you want
		// removed.

		PriorityQueue<BHT> q = new PriorityQueue<BHT>();

		for (int i = 0; i < ct; i++) {
			BHT b = new BHT(r.nextDouble(), r.nextDouble());
			db.add(b);
			q.add(b);
		}
		for (int i = 0; i < ct; i++) {
			BHT mhNext = q.poll();
			assert (db.contains(mhNext));
			boolean r = db.remove(mhNext);
			assert (r);
			assert (mhNext != null);
		}
		assert (db.isEmpty());
	}

	@Test
	public void longRemoveTest() {
		for (int i = 0; i < ct; i++) {
			BHT b = new BHT(r.nextDouble(), r.nextDouble());
			BHT copy = (BHT) b.clone();
			db.add(b);
			mh.add(copy);
		}
		for (int i = 0; i < ct; i++) {
			BHT mhNext = mh.poll();
			BHT bhNext = db.poll();
			assert (bhNext != null);
			assert (mhNext != null);
			assert (mhNext.getF() == bhNext.getF());
			assert (mhNext.getG() == bhNext.getG());
		}

	}

	private void trieWorkout(int sz) {
		for (int i = 0; i < sz; i++) {
			BHT b = new BHT(r.nextDouble(), r.nextDouble());
			db.add(b);
		}
		while (!db.isEmpty()) {
			db.poll();
		}
		return;
	}

	private void heapWorkout(int sz) {
		for (int i = 0; i < sz; i++) {
			BHT b = new BHT(r.nextDouble(), r.nextDouble());
			mh.add(b);
		}
		while (!mh.isEmpty()) {
			mh.poll();
		}
		return;
	}

	
	private void fTrieWorkout(int sz) {
		for (int i = 0; i < sz; i++) {
			BHT b = new BHT(r.nextDouble(), r.nextDouble());
			fb.add(b);
		}
		while (!fb.isEmpty()) {
			fb.poll();
		}
		return;
	}


	private void listWorkout(int sz) {
		LinkedList<BHT> l = new LinkedList<BHT>();
		
		for (int i = 0; i < sz; i++) {
			BHT b = new BHT(r.nextDouble(), r.nextDouble());
			l.add(b);
		}
		while (!l.isEmpty()) {
			l.poll();
		}
		return;
	}

	private void pairingWorkout(int sz) {
		
		for (int i = 0; i < sz; i++) {
			BHT b = new BHT(r.nextDouble(), r.nextDouble());
			ph.add(b);
		}
		while (!ph.isEmpty()) {
			ph.poll();
		}
		return;
	}

	private void mmhWorkout(int sz) {
		MinMaxHeap<BHT> l = new MinMaxHeap<BHT>(new BHT.FGComparator());
		
		for (int i = 0; i < sz; i++) {
			BHT b = new BHT(r.nextDouble(), r.nextDouble());
			l.insert(b);
		}
		while (!l.isEmpty()) {
			l.pop();
		}
		return;
	}

	
	private void ffhWorkout(int sz) {
		FastFloatHeap<BHT> l = new FastFloatHeap<BHT>(new BHT.FGComparator(), 0, 1, 5000);
		
		for (int i = 0; i < sz; i++) {
			BHT b = new BHT(r.nextDouble(), r.nextDouble());
			l.add(b);
		}
		while (!l.isEmpty()) {
			l.poll();
		}
		return;
	}

	private void alistWorkout(int sz) {
		ArrayList<BHT> l = new ArrayList<BHT>();
		
		for (int i = 0; i < sz; i++) {
			BHT b = new BHT(r.nextDouble(), r.nextDouble());
			l.add(b);
		}
		while (!l.isEmpty()) {
			l.remove(l.size() - 1);
		}
		return;
	}

	
	private static void usage() {
		System.err.println("needs 2 arguments:");
		System.err.println("<num-items> t|h");
		System.exit(1);
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			usage();
		}

		DBinHeapTest dbt = new DBinHeapTest();
		int sz = 0;
		try {
			sz = Integer.parseInt(args[0]);
		} catch (NumberFormatException n) {
			usage();
		}
		
		if(sz == 0){
			System.err.println("didn't catch a size");
			usage();
		}
		
		if (args[1].equals("t")) {
			long trieStart = System.currentTimeMillis();
			dbt.trieWorkout(sz);
			long trieEnd = System.currentTimeMillis();
			System.out.println("trie: " + (trieEnd - trieStart));
		} else if (args[1].equals("a")) {
			long heapStart = System.currentTimeMillis();
			dbt.alistWorkout(sz);
			long heapEnd = System.currentTimeMillis();
			System.out.println("arraylist: " + (heapEnd - heapStart));
			System.out.flush();
		} else if (args[1].equals("l")) {
			long heapStart = System.currentTimeMillis();
			dbt.listWorkout(sz);
			long heapEnd = System.currentTimeMillis();
			System.out.println("linkedlist: " + (heapEnd - heapStart));
			System.out.flush();
		} else if (args[1].equals("h")) {
			long heapStart = System.currentTimeMillis();
			dbt.heapWorkout(sz);
			long heapEnd = System.currentTimeMillis();
			System.out.println("heap: " + (heapEnd - heapStart));
			System.out.flush();
		} else if (args[1].equals("ffh")) {
			long heapStart = System.currentTimeMillis();
			dbt.ffhWorkout(sz);
			long heapEnd = System.currentTimeMillis();
			System.out.println("heap: " + (heapEnd - heapStart));
			System.out.flush();
		} else if (args[1].equals("ph")) {
			long heapStart = System.currentTimeMillis();
			dbt.pairingWorkout(sz);
			long heapEnd = System.currentTimeMillis();
			System.out.println("Pairing heap: " + (heapEnd - heapStart));
			System.out.flush();
		} else if (args[1].equals("mmh")) {
			long heapStart = System.currentTimeMillis();
			dbt.mmhWorkout(sz);
			long heapEnd = System.currentTimeMillis();
			System.out.println("minmax heap: " + (heapEnd - heapStart));
			System.out.flush();
		} else if (args[1].equals("f")) {
			long heapStart = System.currentTimeMillis();
			dbt.fTrieWorkout(sz);
			long heapEnd = System.currentTimeMillis();
			System.out.println("ftrie: " + (heapEnd - heapStart));
			System.out.flush();
		} else {
			System.err.println("didn't catch a data structure");
			System.err.println(args);
			usage();
		}

	}

	private static final int ct = 100;

	@Test
	public void duplicateRemoveTest() {

		for (int i = 0; i < ct; i++) {
			BHT b = new BHT(0, 0);
			BHT copy = (BHT) b.clone();
			db.add(b);
			mh.add(copy);
		}

		for (int i = 0; i < ct; i++) {
			BHT b = new BHT(1, 1);
			BHT copy = (BHT) b.clone();
			db.add(b);
			mh.add(copy);
		}

		for (int i = 0; i < ct * 2; i++) {
			BHT mhNext = mh.poll();
			BHT bhNext = db.poll();
			assert (bhNext != null);
			assert (mhNext != null);
			assert (mhNext.getF() == bhNext.getF());
			assert (mhNext.getG() == bhNext.getG());
		}

	}

	@Test
	public void removePollTest() {
		BHT b = new BHT(0, 0);
		db.add(b);
		assert (db.contains(b));
		boolean r = db.remove(b);

		assert (r);
	}

	@Test
	public void addTest() {
		BHT b = new BHT(r.nextDouble(), r.nextDouble());
		db.add(b);
		assert (db.contains(b));
	}
}
