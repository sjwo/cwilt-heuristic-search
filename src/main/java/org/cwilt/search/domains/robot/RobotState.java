package org.cwilt.search.domains.robot;
import java.util.ArrayList;

import org.cwilt.search.algs.basic.bestfirst.AStar;
import org.cwilt.search.algs.experimental.bidirectional.DHAddAstar;
import org.cwilt.search.search.Limit;
import org.cwilt.search.search.SearchState;
public class RobotState extends SearchState {

	public int indexLocation() {
		return k.x + k.y * prob.xSize;
	}

	public double distTo(SearchState other) {
		return prob.distTo(this, (RobotState) other);
	}

	public static final int cellSize = 40;
	// private static final double cellOffset = cellSize / 2;

	private static final double maxAccel = 150;
	private static final double maxTurn = 90;

	static final int[] headings = { 0, 11, 23, 34, 45, 56, 68, 79, 90, 101,
			113, 124, 135, 146, 158, 169, 180, 191, 203, 214, 225, 236, 248,
			259, 270, 281, 293, 304, 315, 326, 338, 349, };
	static final int[] speeds = { 0, 20, 40, 60, 80, 100, 120, 140, 160, 180,
			200, 220, 240, 260, 280, 300 };

	// private static final int[] revspeeds = { 0, -20, -40, -60, -80, -100,
	// -120, -140,
	// -160, -180, -200, -220, -240, -260, -280, -300 };

	public static final int maxSpeed = 300;

	private final Key k;
	private final RobotProblem prob;

	public String toString() {
		return k.toString();
	}

	private static class Key {
		public final int x, y, heading, speed;

		public String toString() {
			return String.format("(%4d,%4d) heading %3d speed %3d", x
					* cellSize, y * cellSize, heading, speed);
		}

		public Key(int x, int y, int heading, int speed) {
			this.x = x;
			this.y = y;
			this.heading = heading;
			this.speed = speed;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + heading;
			result = prime * result + speed;
			result = prime * result + x;
			result = prime * result + y;
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
			Key other = (Key) obj;
			if (heading != other.heading)
				return false;
			if (speed != other.speed)
				return false;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
		}

	}

	private RobotState(RobotState parent, int deltaX, int deltaY, int speed,
			int heading) {
		this.prob = parent.prob;
		this.k = new Key(parent.k.x + deltaX, parent.k.y + deltaY, speed,
				heading);
	}

	public double straightLineDistance(RobotState other) {
		double dx = Math.abs(this.k.x - other.k.x);
		double dy = Math.abs(this.k.y - other.k.y);

		double brakeTime = this.k.speed / maxAccel;
		double brakingDistance = this.k.speed * brakeTime / 2;

		double goalDist = Math.sqrt(dx * dx + dy * dy);

		if (brakingDistance > goalDist)
			return brakingDistance + (brakingDistance - goalDist);
		else
			return goalDist;
	}

	public double straightLineTime(RobotState other) {
		return straightLineDistance(other) / maxSpeed;
	}

	public static int snap(int heading) {
		int incumbent = 0;
		int diff = heading;
		for (int h : headings) {
			if (Math.abs(h - heading) < diff) {
				diff = Math.abs(h - heading);
				incumbent = h;
			}
		}
		return incumbent;
	}

	public RobotState(RobotProblem prob, int x, int y, int heading, int speed) {
		this.k = new Key(x, y, snap(heading), speed);
		this.prob = prob;
	}

	static final class DXY {
		public final int x;
		public final int y;
		public final double dist;

		public DXY(int x, int y, double dist) {
			this.x = x;
			this.y = y;
			this.dist = dist;
		}

		public String toString() {
			StringBuffer b = new StringBuffer();
			b.append("(");
			b.append(x);
			b.append(",");
			b.append(y);
			b.append(")");
			return b.toString();
		}
	}

	private static final int positionDelta = 1;
	private static final double cut = Math.ceil(Math.toDegrees(Math
			.atan(0.5)));
	private static final double r2 = Math.sqrt(2) * positionDelta;

	private final static DXY fwdDXY[] = { new DXY(positionDelta, 0, positionDelta),
			new DXY(positionDelta, positionDelta, r2),
			new DXY(0, positionDelta, positionDelta), 
			new DXY(-positionDelta, positionDelta, r2),
			new DXY(-positionDelta, 0, positionDelta),
			new DXY(-positionDelta, -positionDelta, r2),
			new DXY(0, -positionDelta, positionDelta),
			new DXY(positionDelta, -positionDelta, r2),
	};

	static final DXY getDXY(double heading) {
		if ((heading < cut) || (heading > (360d - cut)))
			return fwdDXY[0];
		else if (heading < (90d - cut))
			return fwdDXY[1];
		else if (heading < (90d + cut))
			return fwdDXY[2];
		else if (heading < (180d - cut))
			return fwdDXY[3];
		else if (heading < (180d + cut))
			return fwdDXY[4];

		else if (heading < (270d - cut))
			return fwdDXY[5];
		else if (heading < (270d + cut))
			return fwdDXY[6];
		else
			return fwdDXY[7];
	}

	static final DXY getrevDXY(double heading) {
		if ((heading < cut) || (heading > (360 - cut)))
			return new DXY(-positionDelta, 0, positionDelta);
		else if (heading < (90 - cut))
			return new DXY(-positionDelta, -positionDelta, r2);
		else if (heading < (90 + cut))
			return new DXY(0, -positionDelta, positionDelta);

		else if (heading < (180 - cut))
			return new DXY(positionDelta, -positionDelta, r2);
		else if (heading < (180 + cut))
			return new DXY(positionDelta, 0, positionDelta);

		else if (heading < (270 - cut))
			return new DXY(positionDelta, positionDelta, r2);
		else if (heading < (270 + cut))
			return new DXY(0, positionDelta, positionDelta);
		else
			return new DXY(-positionDelta, positionDelta, r2);
	}

	private static final double hDiff(double h1, double h2) {
		double diff = Math.abs(h1 - h2);
		if (diff > 180)
			return 360 - diff;
		else
			return diff;
	}

	private static final double headingDelta = 360. / 32.;

	private static final int d = (int) Math.ceil(headingDelta / 2.0);

	private static final boolean hDiffFromProper(int heading) {
		heading = heading % 45;
		return (!((heading < d) || ((45 - heading) < d)));
	}

	@Override
	public ArrayList<Child> expand() {
		ArrayList<Child> children = new ArrayList<Child>();

		for(int i = 0; i < speeds.length; i++) {
			int s = speeds[i];
			double meanSpeed = (s + k.speed) / 2;
			DXY dxy = getDXY(k.heading);
			double timeAtHeading = dxy.dist * cellSize / meanSpeed;
			double accel = Math.abs(s - k.speed);
			double timeForAccel = accel / maxAccel;
			// System.err.printf("%3d %f %f\n", s, timeForAccel, timeAtHeading);
			if (timeForAccel <= timeAtHeading) {
				for(int j = 0; j < headings.length; j++) {
					int h = headings[j];
					DXY ndxy = getDXY(h);
					double timeAtH = ndxy.dist * cellSize / meanSpeed;
					double turn = hDiff(h, k.heading);
					double timeForTurn = turn / maxTurn;
					if (timeForTurn <= timeAtH) {
						// System.err.printf("%3d %3d %f %f\n", s, h, timeAtH,
						// timeForTurn);
						if (s == 0) {
							RobotState child = new RobotState(this, 0, 0, h, s);
							children.add(new Child(child, Math.max(
									timeForAccel, timeForTurn)));
						} else {
							if (prob.validLocation(k.x + ndxy.x, k.y + ndxy.y)) {
								RobotState child = new RobotState(this, ndxy.x,
										ndxy.y, h, s);
								double penalty;
								if (hDiffFromProper(h))
									penalty = timeAtH * 0.01;
								else
									penalty = 0;
								children.add(new Child(child, timeAtH + penalty));
							}
						}
					}
				}
			}
		}
		return children;
	}

	@Override
	public double h() {
		// return prob.getD(this);
		// return 0;
		return prob.getH(this);
	}

	@Override
	public boolean isGoal() {
		return prob.isGoal(this);
	}

	@Override
	public Object getKey() {
		return k;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((k == null) ? 0 : k.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		RobotState other = (RobotState) obj;
		if (k == null) {
			if (other.k != null)
				return false;
		} else if (!k.equals(other.k))
			return false;
		return true;
	}

	public int getX() {
		return k.x;
	}

	public int getSpeed() {
		return k.speed;
	}

	public int getHeading() {
		return k.heading;
	}

	public int getY() {
		return k.y;
	}

	@Override
	public int lexOrder(SearchState s) {
		RobotState other = (RobotState) s;
		if (other.k.equals(this.k))
			return 0;
		else if (this.k.x < other.k.x)
			return -1;
		else if (this.k.x > other.k.x)
			return 1;
		else if (this.k.y < other.k.y)
			return -1;
		else if (this.k.y > other.k.y)
			return 1;
		else if (this.k.heading < other.k.heading)
			return -1;
		else if (this.k.heading > other.k.heading)
			return 1;
		else if (this.k.speed < other.k.speed)
			return -1;
		else if (this.k.speed > other.k.speed)
			return 1;
		else
			return 0;
	}

	public static void main(String[] args) {
		RobotProblem p = new RobotProblem();
		// RobotState r = new RobotState(p, 100, 100, 0, 0);

		// System.err.println(r);
		// SearchNode n = SearchNode.makeInitial(r);
		// ArrayList<? extends SearchNode> children = n.expand();
		// for (SearchNode s : children)
		// System.err.println(s);
		// System.err.println(children.size());
		//
		// ArrayList<? extends SearchNode> revchildren = n.reverseExpand();
		// for (SearchNode s : revchildren)
		// System.err.println(s);
		// System.err.println(revchildren.size());

		AStar a = new AStar(p, new Limit());
		a.solve();
		a.printSearchData(System.out);

		DHAddAstar h = new DHAddAstar(p, new Limit(), 0.1, 1.0);
		h.solve();
		h.printSearchData(System.out);
	}

	public ArrayList<Child> reverseExpand() {
		// assert(false);

		ArrayList<Child> children = new ArrayList<Child>();

		for (int h : headings) {
			for (int s : speeds) {
				double meanSpeed = (s + k.speed) / 2;
				DXY dxy = getrevDXY(h);
				double timeAtHeading = dxy.dist * cellSize / meanSpeed;
				double accel = Math.abs(s - k.speed);
				double timeForAccel = accel / maxAccel;

				DXY ndxy = getrevDXY(k.heading);
				double timeAtH = ndxy.dist * cellSize / meanSpeed;
				double turn = hDiff(h, k.heading);
				double timeForTurn = turn / maxTurn;

				if (timeForTurn <= timeAtH) {
					// System.err.printf("%3d %f %f\n", s, timeForAccel,
					// timeAtHeading);
					if (timeForAccel <= timeAtHeading) {
						// System.err.printf("%3d %3d %f %f\n", s, h, timeAtH,
						// timeForTurn);
						if (k.speed == 0) {
							RobotState child = new RobotState(this, 0, 0, h, s);
							children.add(new Child(child, Math.max(
									timeForAccel, timeForTurn)));
						} else {
							if (prob.validLocation(k.x + ndxy.x, k.y + ndxy.y)) {
								RobotState child = new RobotState(this, ndxy.x,
										ndxy.y, h, s);
								double penalty;
								if (hDiffFromProper(k.heading))
									penalty = timeAtH * 0.01;
								else
									penalty = 0;
								children.add(new Child(child, timeAtH + penalty));
							}
						}
					}
				}
			}
		}
		return children;
	}

	@Override
	public int d() {
		return prob.getD(this);
	}
}
