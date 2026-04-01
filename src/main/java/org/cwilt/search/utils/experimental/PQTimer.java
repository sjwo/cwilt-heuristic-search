package org.cwilt.search.utils.experimental;
import java.text.ParseException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import org.cwilt.search.utils.basic.MinHeap;
import org.cwilt.search.utils.basic.PairingHeap;
import org.cwilt.search.utils.experimental.BinHeapTest.BHT;
import org.cwilt.search.utils.floats.FloatRadixHeap;
import org.cwilt.search.utils.floats.RadixHeap;
public class PQTimer {
	private static final String[] choices = { "h", "ll", "al", "ph", "ffh",
			"drh", "frh", "ffh2" };
	private static final String[] names = { "Min Heap", "Linked List",
			"Ring Buffer", "Pairing Heap", "Fast Float Heap",
			"Double Radix Heap", "Float Radix Heap", "Faster Float Heap" };

	private static void usage() {
		System.err.println("needs 2 arguments:");
		System.err.println("<num-items> ");

		for (String c : choices) {
			System.err.print(c + "|");
		}

		System.exit(1);
	}

	private final Queue<BHT> pq;
	private final BHT[] items;
	private final Random r = new Random(0);
	private final int nameIndex;

	private static final double RANGE = 10000000d;

	public PQTimer(String s, String sizeString, String dsArg)
			throws ParseException {
		if (s.equals(choices[0])) {
			pq = new MinHeap<BHT>(new BHT.FGComparator());
			nameIndex = 0;
		} else if (s.equals(choices[1])) {
			pq = new LinkedList<BHT>();
			nameIndex = 1;
		} else if (s.equals(choices[2])) {
			pq = new ArrayQueue<BHT>();
			nameIndex = 2;
		} else if (s.equals(choices[3])) {
			pq = new PairingHeap<BHT>(new BHT.FGComparator());
			nameIndex = 3;
		} else if (s.equals(choices[4])) {
			int bucketSize = Integer.parseInt(dsArg);
			pq = new FastFloatHeap<BHT>(new BHT.FGComparator(), -1, 1,
					bucketSize);
			nameIndex = 4;
		} else if (s.equals(choices[5])) {
			pq = new FloatRadixHeap<BHT>();
			nameIndex = 5;
		} else if (s.equals(choices[6])) {
			pq = new RadixHeap<BHT>();
			nameIndex = 6;
		} else if (s.equals(choices[7])) {
			int bucketSize = Integer.parseInt(dsArg);
			pq = new FasterFloatHeap<BHT>(-1, 1, new BHT.FGComparator(), bucketSize);
			nameIndex = 7;
		} else {
			throw new ParseException("Invalid Data Structure Selection", 0);
		}
		int workoutLength = Integer.parseInt(sizeString);
		this.items = new BHT[workoutLength];
		for (int i = 0; i < workoutLength; i++) {
			items[i] = new BHT(r.nextDouble() * RANGE, r.nextDouble() * RANGE);
		}
	}

	public void workout() {
		long addStart = System.currentTimeMillis();
		for (BHT b : items) {
			pq.add(b);
		}
		long addEnd = System.currentTimeMillis();
		while (!pq.isEmpty())
			pq.poll();
		long pollEnd = System.currentTimeMillis();
		double addTime = addEnd - addStart;
		double pollTime = pollEnd - addEnd;
		double nItems = (double) items.length;
		addTime = addTime / nItems * 1000d;
		pollTime = pollTime / nItems * 1000d;
		System.err.println(names[nameIndex] + "\nadd " + addTime + "\npoll "
				+ pollTime);
	}

	public void orderedWorkout() {
		Arrays.sort(items, new BHT.ReverseFGComparator());
		long addStart = System.currentTimeMillis();
		for (BHT b : items) {
			pq.add(b);
		}
		long addEnd = System.currentTimeMillis();
		while (!pq.isEmpty())
			pq.poll();
		long pollEnd = System.currentTimeMillis();
		double addTime = addEnd - addStart;
		double pollTime = pollEnd - addEnd;
		double nItems = (double) items.length;
		addTime = addTime / nItems * 1000d;
		pollTime = pollTime / nItems * 1000d;
		System.err.println(names[nameIndex] + "\nadd " + addTime + "\npoll "
				+ pollTime);
	}

	public static void main(String[] args) throws ParseException {
		if (args.length == 2) {
			PQTimer t = new PQTimer(args[0], args[1], "No Parameter");
			t.orderedWorkout();
		} else if (args.length == 3) {
			PQTimer t = new PQTimer(args[0], args[1], args[2]);
			t.orderedWorkout();
		} else
			usage();

	}

}
