/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.domains.grid;
import java.util.ArrayList;

import org.cwilt.search.search.SearchState;
public class GridState extends SearchState {

	public GridState clone() {
		return new GridState(this.getX(), this.getY(), this.problem);
	}

	public static class Location {
		private final int xPos;
		private final int yPos;

		public Location(int xPos, int yPos) {
			this.xPos = xPos;
			this.yPos = yPos;
		}

		public String toString() {
			return (xPos + " " + yPos);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + xPos;
			result = prime * result + yPos;
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
			Location other = (Location) obj;
			if (xPos != other.xPos)
				return false;
			if (yPos != other.yPos)
				return false;
			return true;
		}

	}

	private static final double rt2 = Math.sqrt(2.0);
	private final Location loc;
	private final GridProblem problem;
	// avoid generating the parent nodes
	private final GridState parent;

	public String toString() {
		return loc.toString();
	}

	private double getCost(GridState parent, GridState child) {
		if (problem.getCost() == GridProblem.COST.UNIT
				&& problem.getMovement() == GridProblem.MOVEMENT.FOUR)
			return 1.0;
		else if (problem.getCost() == GridProblem.COST.LIFE) {
			return problem.map[child.getY()][child.getX()].exitCost;
		} else if (problem.getCost() == GridProblem.COST.RANDOM) {
			return problem.map[child.getY()][child.getX()].exitCost;
		} else if (problem.getCost() == GridProblem.COST.UNIT
				&& (problem.getMovement() == GridProblem.MOVEMENT.EIGHT || problem
						.getMovement() == GridProblem.MOVEMENT.MOVINGAI)) {
			if (this.loc.xPos == child.loc.xPos)
				return 1.0;
			if (this.loc.yPos == child.loc.yPos)
				return 1.0;
			else
				return rt2;
		} else
			throw new org.cwilt.search.utils.basic.NotImplementedException();
	}

	private static final DIRECTION[] fourway = { DIRECTION.NORTH,
			DIRECTION.SOUTH, DIRECTION.EAST, DIRECTION.WEST };
	private static final DIRECTION[] diagonal = { DIRECTION.NW, DIRECTION.NE,
			DIRECTION.SW, DIRECTION.SE };

	private static enum DIRECTION {
		NORTH, SOUTH, EAST, WEST, NW, NE, SW, SE
	}

	private GridState(int xPos, int yPos, GridState parent) {
		this.loc = new Location(xPos, yPos);
		this.problem = parent.problem;
		this.parent = parent;
	}

	private GridState canMove(DIRECTION d, boolean pruneParent) {
		int childX = 0;
		int childY = 0;
		switch (d) {
		case NORTH:
			childX = this.loc.xPos;
			childY = this.loc.yPos - 1;
			break;
		case SOUTH:
			childX = this.loc.xPos;
			childY = this.loc.yPos + 1;
			break;
		case EAST:
			childX = this.loc.xPos + 1;
			childY = this.loc.yPos;
			break;
		case WEST:
			childX = this.loc.xPos - 1;
			childY = this.loc.yPos;
			break;
		case NE:
			childX = this.loc.xPos + 1;
			childY = this.loc.yPos - 1;
			break;
		case NW:
			childX = this.loc.xPos - 1;
			childY = this.loc.yPos - 1;
			break;
		case SE:
			childX = this.loc.xPos + 1;
			childY = this.loc.yPos + 1;
			break;
		case SW:
			childX = this.loc.xPos - 1;
			childY = this.loc.yPos + 1;
			break;
		}
		if (problem.canMove(childX, childY)) {
			GridState child = new GridState(childX, childY, this);
			if (child.equals(this.parent) && pruneParent) {
				return null;
			} else {
				if (problem.getMovement() != GridProblem.MOVEMENT.MOVINGAI)
					return child;
				else {
					// check for diagonal moves that clip corners
					if (this.loc.xPos == child.loc.xPos)
						return child;
					else if (this.loc.yPos == child.loc.yPos)
						return child;
					else if (d == DIRECTION.NW
							&& (this.canMove(DIRECTION.NORTH, false) != null && this
									.canMove(DIRECTION.WEST, false) != null)) {
						return child;
					} else if (d == DIRECTION.NE
							&& (this.canMove(DIRECTION.NORTH, false) != null && this
									.canMove(DIRECTION.EAST, false) != null)) {
						return child;
					} else if (d == DIRECTION.SW
							&& (this.canMove(DIRECTION.SOUTH, false) != null && this
									.canMove(DIRECTION.WEST, false) != null)) {
						return child;
					} else if (d == DIRECTION.SE
							&& (this.canMove(DIRECTION.SOUTH, false) != null && this
									.canMove(DIRECTION.EAST, false) != null)) {
						return child;
					} else
						return null;
				}
			}

		} else
			return null;
	}

	public GridState(int xPos, int yPos, GridProblem prob) {
		this.loc = new Location(xPos, yPos);
		this.problem = prob;
		this.parent = null;
	}

	@Override
	public ArrayList<Child> expand() {
		problem.expanded(loc.xPos, loc.yPos);
		ArrayList<Child> children = new ArrayList<Child>();

		for (DIRECTION d : fourway) {
			GridState child = canMove(d, problem.pruneParent());
			if (child != null) {
				problem.generated(child.loc.xPos, child.loc.yPos);
				children.add(new Child(child, getCost(this, child)));
			}
		}

		if (problem.getMovement() == GridProblem.MOVEMENT.EIGHT
				|| problem.getMovement() == GridProblem.MOVEMENT.MOVINGAI) {
			for (DIRECTION d : diagonal) {
				GridState child = canMove(d, problem.pruneParent());
				if (child != null) {
					problem.generated(child.loc.xPos, child.loc.yPos);
					children.add(new Child(child, getCost(this, child)));
				}
			}
		}

		return children;
	}

	private double h(GridState goal) {
		int hereXPos = loc.xPos;
		int hereYPos = loc.yPos;
		int goalXPos = goal.loc.xPos;
		int goalYPos = goal.loc.yPos;
		if (problem.getCost() == GridProblem.COST.RANDOM) {
			return problem.map[hereYPos][hereXPos].h;
		} else if (problem.getMovement() == GridProblem.MOVEMENT.FOUR
				&& problem.getCost() == GridProblem.COST.UNIT) {
			return Math.abs(hereXPos - goalXPos)
					+ Math.abs(hereYPos - goalYPos);
		} else if ((problem.getMovement() == GridProblem.MOVEMENT.EIGHT || problem
				.getMovement() == GridProblem.MOVEMENT.MOVINGAI)
				&& problem.getCost() == GridProblem.COST.UNIT) {
			double xDiff = Math.abs(hereXPos - goalXPos);
			double yDiff = Math.abs(hereYPos - goalYPos);
			return Math.min(xDiff, yDiff) * rt2 + Math.abs(xDiff - yDiff);
		} else if (problem.getMovement() == GridProblem.MOVEMENT.FOUR
				&& problem.getCost() == GridProblem.COST.LIFE) {
			return problem.map[hereYPos][hereXPos].h;
		} else
			throw new org.cwilt.search.utils.basic.NotImplementedException();
	}

	private int d(GridState goal) {
		int hereXPos = loc.xPos;
		int hereYPos = loc.yPos;
		int goalXPos = goal.loc.xPos;
		int goalYPos = goal.loc.yPos;
		if (problem.getCost() == GridProblem.COST.RANDOM
				|| (problem.getMovement() == GridProblem.MOVEMENT.FOUR)) {
			return Math.abs(hereXPos - goalXPos)
					+ Math.abs(hereYPos - goalYPos);
		} else if ((problem.getMovement() == GridProblem.MOVEMENT.EIGHT || problem
				.getMovement() == GridProblem.MOVEMENT.MOVINGAI)
				&& problem.getCost() == GridProblem.COST.UNIT) {
			int xDiff = Math.abs(hereXPos - goalXPos);
			int yDiff = Math.abs(hereYPos - goalYPos);
			return Math.max(xDiff, yDiff);
		} else
			throw new org.cwilt.search.utils.basic.NotImplementedException();
	}

	@Override
	public double h() {
		assert (problem != null);
		return h(problem.getGoal());
	}

	public double distTo(SearchState s) {
		return h((GridState) s);
	}

	@Override
	public boolean isGoal() {
		return this.equals(problem.getGoal());
	}

	@Override
	public Object getKey() {
		return loc;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((loc == null) ? 0 : loc.hashCode());
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
		GridState other = (GridState) obj;
		if (loc == null) {
			if (other.loc != null)
				return false;
		} else if (!loc.equals(other.loc))
			return false;
		return true;
	}

	@Override
	public int lexOrder(SearchState s) {
		GridState other = (GridState) s;
		if (this.loc.xPos < other.loc.xPos)
			return -1;
		else if (this.loc.xPos > other.loc.xPos)
			return 1;
		else if (this.loc.yPos > other.loc.yPos)
			return 1;
		else if (this.loc.yPos > other.loc.yPos)
			return 1;
		return 0;
	}

	public int getX() {
		return loc.xPos;
	}

	public int getY() {
		return loc.yPos;
	}

	public ArrayList<Child> reverseExpand() {
		problem.reverseExpanded(loc.xPos, loc.yPos);
		ArrayList<Child> children = new ArrayList<Child>();

		for (DIRECTION d : fourway) {
			GridState child = canMove(d, false);
			if (child != null) {
				problem.generated(child.loc.xPos, child.loc.yPos);
				children.add(new Child(child, getCost(child, this)));
			}
		}

		if (problem.getMovement() == GridProblem.MOVEMENT.EIGHT
				|| problem.getMovement() == GridProblem.MOVEMENT.MOVINGAI) {
			for (DIRECTION d : diagonal) {
				GridState child = canMove(d, false);
				if (child != null) {
					problem.generated(child.loc.xPos, child.loc.yPos);
					children.add(new Child(child, getCost(child, this)));
				}
			}
		}

		return children;
	}

	@Override
	public int d() {
		return d(problem.getGoal());
	}
}
