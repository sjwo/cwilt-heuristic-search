package org.cwilt.search.domains.kiva.map;
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

public class CorridorFinder {
	private enum CELL {
		BLOCKED, OPEN, EITHER
	}

	public enum DIRECTION {
		NORTH, EAST
	}

	public String toString() {
		StringBuffer b = new StringBuffer();
		for (CELL[] c : definition) {
			for (CELL cell : c) {
				switch (cell) {
				case BLOCKED:
					b.append("X");
					break;
				case OPEN:
					b.append("_");
					break;
				case EITHER:
					b.append("?");
					break;
				}
			}
			b.append("\n");
		}
		return b.toString();
	}

	private final CELL[][] definition;


	private final void initE1(int length) {
		for (CELL[] c : definition) {
			for (int i = 0; i < c.length; i++) {
				c[i] = CELL.EITHER;
			}
		}
		// make the corridor
		for (int i = 0; i < definition.length; i++) {
			definition[i][2] = CELL.OPEN;
		}
		for (int i = 0; i < definition[0].length; i++) {
			definition[0][i] = CELL.OPEN;
			definition[definition.length - 1][i] = CELL.OPEN;
		}
		definition[length/2][0] = CELL.BLOCKED;
		definition[length/2][1] = CELL.BLOCKED;
		definition[length/2][3] = CELL.BLOCKED;
		definition[length/2][4] = CELL.BLOCKED;
		centers[0] = new Offset(length/2, 2);
	}

	private final void initN1(int length) {
		for (CELL[] c : definition) {
			for (int i = 0; i < c.length; i++) {
				c[i] = CELL.EITHER;
			}
		}
		// make the corridor
		for (int i = 0; i < definition[0].length; i++) {
			definition[2][i] = CELL.OPEN;
		}
		for (int i = 0; i < definition.length; i++) {
			definition[i][0] = CELL.OPEN;
			definition[i][definition[0].length - 1] = CELL.OPEN;
		}
		definition[0][length/2] = CELL.BLOCKED;
		definition[1][length/2] = CELL.BLOCKED;
		definition[3][length/2] = CELL.BLOCKED;
		definition[4][length/2] = CELL.BLOCKED;
		centers[0] = new Offset(2, length/2);
	}

	private final void initE2(int length) {
		for (CELL[] c : definition) {
			for (int i = 0; i < c.length; i++) {
				c[i] = CELL.EITHER;
			}
		}
		// make the corridor
		for (int i = 0; i < definition.length; i++) {
			definition[i][2] = CELL.OPEN;
			definition[i][3] = CELL.OPEN;
		}
		for (int i = 0; i < definition[0].length; i++) {
			definition[0][i] = CELL.OPEN;
			definition[length-1][i] = CELL.OPEN;
		}
		definition[length/2][0] = CELL.BLOCKED;
		definition[length/2][1] = CELL.BLOCKED;
		definition[length/2][4] = CELL.BLOCKED;
		definition[length/2][5] = CELL.BLOCKED;
		centers[0] = new Offset(length/2, 2);
		centers[1] = new Offset(length/2, 3);
	}

	private final void initN2(int length) {
		for (CELL[] c : definition) {
			for (int i = 0; i < c.length; i++) {
				c[i] = CELL.EITHER;
			}
		}
		// make the corridor
		for (int i = 0; i < definition[0].length; i++) {
			definition[2][i] = CELL.OPEN;
			definition[3][i] = CELL.OPEN;
		}
		for (int i = 0; i < definition.length; i++) {
			definition[i][0] = CELL.OPEN;
			definition[i][length-1] = CELL.OPEN;
		}
		definition[0][length/2] = CELL.BLOCKED;
		definition[1][length/2] = CELL.BLOCKED;
		definition[4][length/2] = CELL.BLOCKED;
		definition[5][length/2] = CELL.BLOCKED;
		centers[0] = new Offset(2, length/2);
		centers[1] = new Offset(3, length/2);
	}

	private final void initE3(int length) {
		for (CELL[] c : definition) {
			for (int i = 0; i < c.length; i++) {
				c[i] = CELL.EITHER;
			}
		}
		// make the corridor
		for (int i = 0; i < definition.length; i++) {
			definition[i][2] = CELL.OPEN;
			definition[i][3] = CELL.OPEN;
			definition[i][4] = CELL.OPEN;
		}
		for (int i = 0; i < definition[0].length; i++) {
			definition[0][i] = CELL.OPEN;
			definition[length-1][i] = CELL.OPEN;
		}
		definition[length/2][0] = CELL.BLOCKED;
		definition[length/2][1] = CELL.BLOCKED;
		definition[length/2][5] = CELL.BLOCKED;
		definition[length/2][6] = CELL.BLOCKED;
		centers[0] = new Offset(length/2, 2);
		centers[1] = new Offset(length/2, 3);
		centers[2] = new Offset(length/2, 4);
	}

	private final void initN3(int length) {
		for (CELL[] c : definition) {
			for (int i = 0; i < c.length; i++) {
				c[i] = CELL.EITHER;
			}
		}
		// make the corridor
		for (int i = 0; i < definition[0].length; i++) {
			definition[2][i] = CELL.OPEN;
			definition[3][i] = CELL.OPEN;
			definition[4][i] = CELL.OPEN;
		}
		for (int i = 0; i < definition.length; i++) {
			definition[i][0] = CELL.OPEN;
			definition[i][length-1] = CELL.OPEN;
		}
		definition[0][length/2] = CELL.BLOCKED;
		definition[1][length/2] = CELL.BLOCKED;
		definition[5][length/2] = CELL.BLOCKED;
		definition[6][length/2] = CELL.BLOCKED;
		centers[0] = new Offset(2, length/2);
		centers[1] = new Offset(3, length/2);
		centers[2] = new Offset(4, length/2);
	}

	private final void initN4(int length) {
		for (CELL[] c : definition) {
			for (int i = 0; i < c.length; i++) {
				c[i] = CELL.EITHER;
			}
		}
		// make the corridor
		for (int i = 0; i < definition[0].length; i++) {
			definition[2][i] = CELL.OPEN;
			definition[3][i] = CELL.OPEN;
			definition[4][i] = CELL.OPEN;
			definition[5][i] = CELL.OPEN;
		}
		for (int i = 0; i < definition.length; i++) {
			definition[i][0] = CELL.OPEN;
			definition[i][length-1] = CELL.OPEN;
		}
		definition[0][length/2] = CELL.BLOCKED;
		definition[1][length/2] = CELL.BLOCKED;
		definition[6][length/2] = CELL.BLOCKED;
		definition[7][length/2] = CELL.BLOCKED;
		centers[0] = new Offset(2, length/2);
		centers[1] = new Offset(3, length/2);
		centers[2] = new Offset(4, length/2);
		centers[3] = new Offset(5, length/2);
	}

	private final void initE4(int length) {
		for (CELL[] c : definition) {
			for (int i = 0; i < c.length; i++) {
				c[i] = CELL.EITHER;
			}
		}
		// make the corridor
		for (int i = 0; i < definition.length; i++) {
			definition[i][2] = CELL.OPEN;
			definition[i][3] = CELL.OPEN;
			definition[i][4] = CELL.OPEN;
			definition[i][5] = CELL.OPEN;
		}
		for (int i = 0; i < definition[0].length; i++) {
			definition[0][i] = CELL.OPEN;
			definition[length-1][i] = CELL.OPEN;
		}
		definition[length/2][0] = CELL.BLOCKED;
		definition[length/2][1] = CELL.BLOCKED;
		definition[length/2][6] = CELL.BLOCKED;
		definition[length/2][7] = CELL.BLOCKED;
		centers[0] = new Offset(length/2, 2);
		centers[1] = new Offset(length/2, 3);
		centers[2] = new Offset(length/2, 4);
		centers[3] = new Offset(length/2, 5);
	}
	
	private final Offset[] centers;
	
	public final int width;
	
	public class Offset {
		public final int x, y;

		public Offset(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	/**
	 * 
	 * @param g
	 *            Grid to look for the corridor in
	 * @param x
	 *            X of the start
	 * @param y
	 *            Y of the start
	 * @return whether or not there is a corridor centered at this location
	 */
	public boolean matches(Grid g, int x, int y) {

		try {
			for (int i = 0; i < definition.length; i++) {
				for (int j = 0; j < definition[0].length; j++) {
					if (!matches(g.grid[x + i][y + j], i, j))
						return false;
				}
			}
			return true;
		} catch (ArrayIndexOutOfBoundsException ex) {
			return false;
		}
	}

	private boolean matches(GridCell g, int x, int y) {
		if (x > definition.length)
			return false;
		if (y > definition[0].length)
			return false;
		CELL c = definition[x][y];
		if (c == CELL.EITHER)
			return true;
		if (g.canEnter() && c == CELL.OPEN)
			return true;
		if (!g.canEnter() && c == CELL.BLOCKED)
			return true;
		return false;
	}

	private final DIRECTION d;
	
	public DIRECTION getOrientation(){
		return d;
	}
	
	public CorridorFinder(DIRECTION d, int width, int length) {
		this.width = width;
		this.d = d;
		centers = new Offset[width];
		if (d == DIRECTION.EAST && width == 1) {
			definition = new CELL[length][5];
			initE1(length);
		} else if (d == DIRECTION.NORTH && width == 1) {
			definition = new CELL[5][length];
			initN1(length);
		} else if (d == DIRECTION.EAST && width == 2) {
			definition = new CELL[length][6];
			initE2(length);
		} else if (d == DIRECTION.NORTH && width == 2) {
			definition = new CELL[6][length];
			initN2(length);
		} else if (d == DIRECTION.EAST && width == 3) {
			definition = new CELL[length][7];
			initE3(length);
		} else if (d == DIRECTION.NORTH && width == 3) {
			definition = new CELL[7][length];
			initN3(length);
		} else if (d == DIRECTION.EAST && width == 4) {
			definition = new CELL[length][8];
			initE4(length);
		} else if (d == DIRECTION.NORTH && width == 4) {
			definition = new CELL[8][length];
			initN4(length);
		} else {
			throw new RuntimeException(
					"invalid combination for corridor finder");
		}
	}

	public void mark(Grid g, int x, int y) {
		for (Offset o : centers) {
			GridCell here = g.grid[x + o.x][y + o.y];
			here.setColor(Color.MAGENTA);
		}
	}
	
	
	public List<GridCell> getCenters(Grid g, int x, int y) {
		LinkedList<GridCell> toReturn = new LinkedList<GridCell>();
		for (Offset o : centers) {
			GridCell here = g.grid[x + o.x][y + o.y];
			toReturn.add(here);
		}
		return toReturn;
	}
}
