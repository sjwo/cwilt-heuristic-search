package org.cwilt.search.domains.asteroids.planner;
import java.util.ArrayList;

import org.cwilt.search.domains.asteroids.AsteroidProblem;
public abstract class AsteroidPlanner {
	private long maxStepTime = 0l;

	public final long getMaxStepTime() {
		return maxStepTime;
	}

	/**
	 * Asteroids state, I don't think this will be useful, but it is here in
	 * case anyone finds it useful.
	 */
	protected final AsteroidProblem a;
	/**
	 * Current state of the system
	 */
	private AsteroidState current;
	/**
	 * How long to wait before declaring the solver too late with selecting the
	 * next action. If no timeout is desired, this timeout can be set to
	 * Integer.MAX_VALUE.
	 */
	protected final int timeout;
	/**
	 * The path that the ship will follow. This is created bycalling this
	 * class's nextState function repeatedly.
	 */
	protected final ArrayList<AsteroidState> path;
	/**
	 * Maximum number of time steps to simulate the system.
	 */
	private static final int MAX_TIME = 2000;

	public AsteroidPlanner(AsteroidProblem a, int timeout) {
		this.a = a;
		this.current = a.getInitial();
		this.timeout = timeout;
		this.path = new ArrayList<AsteroidState>(MAX_TIME);
	}

	/**
	 * 
	 * This function is called to advance the system to the next time state.
	 * There is an outer loop that runs the solver by calling this function
	 * repeatedly. If this function fails to return sufficiently quickly, the
	 * caller throws an exception, terminating the program.
	 * 
	 * @return The next state that is selected. This state must be a child of
	 *         the current state.
	 */
	public abstract AsteroidState nextState();

	public static class SolverTimeout extends Exception {
		private int time;
		/**
		 * 
		 */
		private static final long serialVersionUID = 2766864464054796777L;

		public SolverTimeout(int time) {
			this.time = time;
		}

		@Override
		public String getMessage() {
			return "Timed out at time " + time;
		}
	}

	public static class CrashedShip extends Exception {
		private int time;
		/**
		 * 
		 */
		private static final long serialVersionUID = 2766864464054796777L;

		public CrashedShip(int time) {
			this.time = time;
		}

		@Override
		public String getMessage() {
			return "Crashed spaceship at time " + time;
		}
	}

	private long solveTime = 0l;

	public long getSolveTime() {
		return solveTime;
	}

	public void solve() throws SolverTimeout, CrashedShip {
		path.add(current);

		long startTime = System.currentTimeMillis();
		for (int i = 0; i < MAX_TIME; i++) {
			long start = System.currentTimeMillis();
			AsteroidState next = nextState();
			long end = System.currentTimeMillis();
			long diff = end - start;

			if (diff > maxStepTime)
				maxStepTime = diff;

			if (diff > timeout) {
				long endTime = System.currentTimeMillis();
				solveTime = endTime - startTime;
				throw new SolverTimeout(i);
			}
			if (next == null) {
				long endTime = System.currentTimeMillis();
				solveTime = endTime - startTime;
				throw new CrashedShip(i);
			}
			path.add(next);
			if (next.isGoal())
				break;
			current = next;
			// System.err.println(next);
		}
		long endTime = System.currentTimeMillis();
		solveTime = endTime - startTime;

	}

	protected final AsteroidState getCurrentState() {
		return current;
	}

	public ArrayList<AsteroidState> getPlan() {
		return path;
	}

}
