/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.search;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.PrintStream;

/**
 * Should extend Jordan
 * 
 * @author cmo66
 * 
 */
public class SearchMaster extends SearchAlgorithm {
	private int restarts;
	private final boolean restart;

	public void reset() {
		for (SearchAlgorithm a : algs) {
			a.reset();
		}
	}

	public void resetAllLimits() {
		for (SearchAlgorithm s : algs) {
			s.l.resetRestartFlags();
		}
		this.l.resetRestartFlags();
	}

	private final int threadCT;

	private final SearchAlgorithm[] algs;

	private void stopAll() {
		for (SearchAlgorithm s : algs) {
			s.stop();
		}
	}

	public void acceptSolution(Solution solution) {
		if (this.getIncumbent() == null) {
			l.endClock();
			super.setIncumbent(solution);
		}
		stopAll();
	}

	public SearchMaster(long threads, SearchAlgorithm a, boolean restart,
			SearchProblem initial) {
		super(initial, a.l.clone());
		this.restart = restart;
		restarts = -1;
		this.threadCT = (int) threads;
		this.algs = new SearchAlgorithm[threadCT];
		// have to add the algs
		for (int i = 0; i < threadCT; i++) {
			SearchAlgorithm algi = a.clone();
			algi.setSearchMaster(this);
			algs[i] = algi;
		}
	}

	public ArrayList<SearchState> solve() {
		l.startClock();

		while (getIncumbent() == null && l.keepGoingNoMem() && (restart || restarts < 0)) {
			ExecutorService executor = Executors.newFixedThreadPool(threadCT);
			for (int i = 0; i < threadCT; i++) {
				executor.execute(algs[i]);
			}
			Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
			executor.shutdown();
			int sleepCount = 1;
			while (!executor.isTerminated() && l.keepGoingNoMem()) {
				try {
					updateLimits();
					Thread.sleep(sleepCount);
					if (sleepCount < 500)
						sleepCount = sleepCount * 2;
				} catch (InterruptedException e) {
				}
			}
			stopAll();
			while (!executor.isTerminated())
				try {
					Thread.sleep(300L);
				} catch (InterruptedException e) {
				}
			updateLimits();
			restarts++;
			if (restart) {
				l.resetRestartFlags();
				if (l.keepGoingNoMem()) {
					resetAllLimits();
					reset();
					System.gc();
					l.resetRestartFlags();
				}
			}
		}

		l.endClock();
		
		if(l.getOutOfMemory()){
			cleanup();
			System.gc();
		}
		if(getIncumbent() != null)
			return getIncumbent().reconstructPath();
		else
			return null;
	}

	private void updateLimits() {
		for (int i = 0; i < threadCT; i++) {
			l.addTo(algs[i].l);
		}
	}

	public void printExtraData(PrintStream s){
		for(SearchAlgorithm a : algs){
			a.printExtraData(s);
		}
	}
	
	public void printSearchData(PrintStream s) {
		updateLimits();
		super.printSearchData(s);
		SearchAlgorithm.printPair(s, "restarts", new Integer(restarts));
	}

	@Override
	public SearchState findFirstGoal() {
		assert (false);
		System.err.println("SearchMaster can't findFirstGoal()");
		System.exit(1);
		return null;
	}

	@Override
	public SearchAlgorithm clone() {
		assert (false);
		return null;
	}

	public void cleanup(){
		for(SearchAlgorithm a : algs)
			a.cleanup();
	}

	public void incrRestarts() {
		restarts ++;
	}
}
