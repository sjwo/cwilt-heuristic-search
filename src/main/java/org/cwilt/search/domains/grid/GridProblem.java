/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */

package org.cwilt.search.domains.grid;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
//import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.cwilt.search.search.Limit;
import org.cwilt.search.search.SearchAlgorithm;
import org.cwilt.search.search.SearchProblem;
import org.cwilt.search.search.SearchState;
public class GridProblem extends JPanel implements SearchProblem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 832071940439696083L;

	public void resetExpGenData() {
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				map[i][j].generated(-1);
				map[i][j].expanded(-1);
			}
		}
	}

	private final boolean drawNumbers;

	public void setCell(org.cwilt.search.misc.analysis.Cell c) {
		this.c = c;
	}

	private org.cwilt.search.misc.analysis.Cell c;

	public void generated(int x, int y) {
		map[y][x].generated(genCount++);
	}

	public void expanded(int x, int y) {
		map[y][x].expanded(expCount++);
	}

	public void onPath(int x, int y) {
		map[y][x].onPath();
	}

	public GridState getGoal() {
		return goal;
	}

	public boolean canMove(int x, int y) {
		if (x < 0)
			return false;
		else if (x >= map[0].length)
			return false;
		else if (y < 0)
			return false;
		else if (y >= map.length)
			return false;
		else {
			return map[y][x].s == STATUS.OPEN;
		}
	}

	public static enum MOVEMENT {
		FOUR, EIGHT, MOVINGAI
	}

	public static enum COST {
		UNIT, LIFE, RANDOM
	}

	enum STATUS {
		BLOCKED, OPEN
	}

	protected class GridCell implements Comparable<GridCell> {
		public double exitCost;
		public double h = Double.MAX_VALUE;
		public double hStar;
		public double hError;
		public int expValue;
		public int revExpValue;
		public int genValue;
		private boolean onPath;
		final int xPos;
		final int yPos;

		public void onPath() {
			onPath = true;
		}

		public GridState.Location getLoc() {
			return new GridState.Location(xPos, yPos);
		}

		public Color getColor() {
			if (this.s == STATUS.BLOCKED)
				return Color.BLACK;
			else if (maxHError > 0) {
				float val = (float) this.hError / (float) maxHError;
				// special case for when the node was not blocked, but was
				// nonetheless unreachable
				if (this.hError == -1) {
					return Color.gray;
				}
				return new Color(1, 1 - val, 1 - val);
			} else if (this.expValue >= 0) {
				float val = (float) expValue / (float) expCount;
				// val = 1 - (val * 0.6f + 0.2f);
				// return new Color(val, val, val);
				return new Color(1, 1 - val, 0);
			} else if (this.revExpValue > 0) {
				return new Color(100, 100, 100, 0);
			} else if (c != null) {
				if (c.coreContainsNode(this.getLoc())) {
					return Color.gray;
				} else if (c.regionContainsNode(this.getLoc())) {
					return Color.LIGHT_GRAY;
				} else {
					for (int i = 0; i < c.neighbors.size(); i++) {
						org.cwilt.search.misc.analysis.Cell neighbor = c.neighbors.get(i);
						if (neighbor.coreContainsNode(this.getLoc())) {
							return Color.green;
						} else if (neighbor.regionContainsNode(this.getLoc())) {
							return Color.blue;
						}
					}
					return Color.WHITE;
				}
			} else
				return Color.WHITE;
		}

		public void draw(Graphics2D g, Dimension d) {
			float xSize = d.width / width;
			float ySize = d.height / height;
			int xLoc = (int) (xPos * xSize);
			int yLoc = (int) (yPos * ySize);
			Rectangle r = new Rectangle(xLoc, yLoc, (int) xSize, (int) ySize);
			Color c = this.getColor();
			g.setColor(c);
			g.fill(r);
			String number = Integer.toString(this.expValue);

			if (onPath) {
				if (drawNumbers) {
					number = "(" + number + ")";
				} else {
					Ellipse2D.Float ellipse = new Ellipse2D.Float(xLoc, yLoc,
							xSize, ySize);
					g.setColor(Color.GREEN);
					g.fill(ellipse);
				}
			}
			if (this.expValue >= 0) {
				float val = (float) expValue / (float) expCount;
				if (val > 0.5)
					g.setColor(Color.white);
				else
					g.setColor(Color.black);
				if (drawNumbers) {
					float xCenter = xLoc + (xSize / 2);
					float yCenter = yLoc + (ySize / 2);
					float textWidth = g.getFontMetrics().stringWidth(number);
					float textHeight = g.getFontMetrics().getHeight() - 4;
					float xPos = xCenter - (textWidth / 2);
					float yPos = yCenter + (textHeight / 2);
					g.drawString(number, xPos, yPos);
				}
			}

			if (maxHError > 0) {
				g.setFont(new Font("", 0, 8));

				number = Integer.toString((int) hError);
				g.setColor(Color.black);
				float xCenter = xLoc + (xSize / 2);
				float yCenter = yLoc + (ySize / 2);
				float textWidth = g.getFontMetrics().stringWidth(number);
				float textHeight = g.getFontMetrics().getHeight() - 4;
				float xPos = xCenter - (textWidth / 2);
				float yPos = yCenter + (textHeight / 2);
				g.drawString(number, xPos, yPos);
			}
		}

		public void generated(int val) {
			genValue = val;
		}

		public void expanded(int val) {
			expValue = val;
		}

		public void reverseExpanded(int val) {
			revExpValue = val;
		}

		public final STATUS s;

		public GridCell(STATUS s, int xPos, int yPos, double exitCost) {
			this.exitCost = exitCost;
			this.s = s;
			this.onPath = false;
			this.expValue = -1;
			this.genValue = -1;
			this.xPos = xPos;
			this.yPos = yPos;
			this.hError = -1;
		}

		public String toString() {

			return xPos + " " + yPos + " " + h;
			// switch (s) {
			// case OPEN:
			// return " ";
			// case BLOCKED:
			// return "#";
			// }
			// return "";
		}

		@Override
		public int compareTo(GridCell arg0) {
			if (this.hStar < arg0.hStar)
				return -1;
			else if (this.hStar > arg0.hStar)
				return 1;
			else
				return 0;
		}
	}

	private final boolean pruneParent;

	private int expCount;
	private int genCount;

	protected final GridCell[][] map;
	private final GridState start;
	private final GridState goal;
	private final COST cost;
	private final MOVEMENT movement;
	protected final int width;
	protected final int height;

	public boolean pruneParent() {
		return pruneParent;
	}

	private GridProblem(GridProblem parent, int seed) {
		this.drawNumbers = parent.drawNumbers;
		this.pruneParent = parent.pruneParent;
		this.expCount = 0;
		this.genCount = 0;
		this.map = parent.map.clone();
		this.height = parent.height;
		this.width = parent.width;
		this.cost = parent.cost;
		this.movement = parent.movement;
		this.c = null;
		Dimension preferredSize = new Dimension(defaultCellSize * this.width,
				defaultCellSize * this.height);
		this.setPreferredSize(preferredSize);
		this.goal = parent.goal.clone();

		Random r = new Random(seed);
		GridState s = null;
		while (s == null) {
			int sx = r.nextInt(this.map[0].length);
			int sy = r.nextInt(this.map.length);
			if (canMove(sx, sy)) {
				s = new GridState(sx, sy, this);
			}
		}
		this.start = s;
	}

	protected GridProblem(int xSize, int ySize, boolean pruneParent,
			boolean drawNumbers) {
		this.drawNumbers = drawNumbers;
		this.pruneParent = pruneParent;
		this.expCount = 0;
		this.genCount = 0;
		this.map = new GridCell[ySize][xSize];
		this.height = ySize;
		this.width = xSize;
		this.cost = COST.UNIT;
		this.movement = MOVEMENT.FOUR;
		this.start = new GridState(0, ySize / 2, this);
		this.goal = new GridState(xSize - 1, ySize / 2, this);
		Dimension preferredSize = new Dimension(defaultCellSize * this.width,
				defaultCellSize * this.height);
		this.setPreferredSize(preferredSize);
		this.c = null;
	}

	public GridProblem(GridProblem parent, int startX, int startY, int endX,
			int endY) {
		this.drawNumbers = parent.drawNumbers;
		this.pruneParent = parent.pruneParent;
		this.width = parent.width;
		this.height = parent.height;
		this.start = new GridState(startX, startY, this);
		this.goal = new GridState(endX, endY, this);
		this.movement = parent.movement;
		this.cost = parent.cost;
		this.map = parent.map;
	}

	public GridProblem(String path, boolean pruneParent, boolean drawNumbers,
			int startX, int startY, int endX, int endY)
			throws IOException, ParseException {
		this.drawNumbers = drawNumbers;
		this.pruneParent = pruneParent;
		FileInputStream fs = new FileInputStream(path);
		DataInputStream ds = new DataInputStream(fs);
		BufferedReader br = new BufferedReader(new InputStreamReader(ds));
		Scanner s = new Scanner(br);
		try {
			s.nextLine();
			s.next();
			height = s.nextInt();
			s.next();
			width = s.nextInt();
			s.nextLine();
			s.nextLine();
			map = new GridCell[height][width];

			for (int i = 0; i < height; i++) {
				String row = s.nextLine();
				for (int j = 0; j < width; j++) {
					STATUS stat;
					char c = row.charAt(j);
					if (c == '.')
						stat = STATUS.OPEN;
					else if (c == 'G')
						stat = STATUS.OPEN;
					else if (c == '@')
						stat = STATUS.BLOCKED;
					else if (c == 'O')
						stat = STATUS.BLOCKED;
					else if (c == 'T')
						stat = STATUS.BLOCKED;
					else {
						throw new ParseException(row, j);
					}
					map[i][j] = new GridCell(stat, j, i, 1.0);
				}
			}

			this.start = new GridState(startX, startY, this);
			this.goal = new GridState(endX, endY, this);
			this.movement = MOVEMENT.MOVINGAI;
			this.cost = COST.UNIT;
		} finally {
			br.close();
			s.close();
		}
	}

	public GridProblem(String path, boolean pruneParent, boolean drawNumbers,
			COST overrideCost, int startX, int startY, int endX, int endY)
			throws IOException, ParseException {
		this.drawNumbers = drawNumbers;
		this.pruneParent = pruneParent;
		FileInputStream fs = new FileInputStream(path);
		DataInputStream ds = new DataInputStream(fs);
		BufferedReader br = new BufferedReader(new InputStreamReader(ds));
		Scanner s = new Scanner(br);
		try {
			height = s.nextInt();
			width = s.nextInt();
			s.nextLine();
			s.nextLine();
			map = new GridCell[height][width];
			for (int i = 0; i < height; i++) {
				String row = s.nextLine();
				for (int j = 0; j < width; j++) {
					STATUS stat;
					char c = row.charAt(j);
					if (c == ' ')
						stat = STATUS.OPEN;
					else if (c == '#')
						stat = STATUS.BLOCKED;
					else {
						throw new ParseException(row, j);
					}
					map[i][j] = new GridCell(stat, j, i, 1.0);
				}
			}
			String cost = s.nextLine();
			if (overrideCost != null)
				this.cost = overrideCost;
			else if (cost.compareTo("Unit") == 0)
				this.cost = COST.UNIT;
			else if (cost.compareTo("Life") == 0) {
				this.cost = COST.LIFE;
			} else
				this.cost = COST.UNIT;
			String movement = s.nextLine();
			if (movement.compareTo("Four-way") == 0) {
				this.movement = MOVEMENT.FOUR;
			} else if (movement.compareTo("Eight-way") == 0)
				this.movement = MOVEMENT.EIGHT;
			else
				this.movement = MOVEMENT.EIGHT;

			this.start = new GridState(startX, startY, this);
			this.goal = new GridState(endX, endY, this);

			if(this.cost == COST.LIFE){
				this.initHValues();
			}
			
			Dimension preferredSize = new Dimension(defaultCellSize
					* this.height, defaultCellSize * this.width);
			this.setPreferredSize(preferredSize);
			this.c = null;
		} finally {
			br.close();
			s.close();
		}
	}

	private static final class GridCellComparator implements
			Comparator<GridCell> {

		@Override
		public int compare(GridCell arg0, GridCell arg1) {
			return (int) (arg0.h - arg1.h);
		}

	}

	protected double nextCost(GridCell c) {
		switch (this.cost) {
		case UNIT:
			return 0;
		case LIFE:
			return c.yPos + 1;
		case RANDOM:
			assert (false);
		}
		return 0;
	}

	protected final void initHValues() {

		for (int i = 0; i < this.width; i++) {
			for (int j = 0; j < this.height; j++) {
				this.map[j][i].exitCost = nextCost(this.map[j][i]);
			}
		}

		GridProblem gp = this;
		int startX = gp.getGoal().getX();
		int startY = gp.getGoal().getY();
		PriorityQueue<GridCell> q = new PriorityQueue<GridCell>(10,
				new GridCellComparator());
		q.add(gp.map[startY][startX]);
		gp.map[startY][startX].h = 0;
		while (!q.isEmpty()) {
			GridCell next = q.poll();
			// left
			if (next.xPos > 0) {
				GridCell child = gp.map[next.yPos][next.xPos - 1];
				if (child.h > next.h + child.exitCost) {
					q.remove(child);
					q.add(child);
					child.h = next.h + child.exitCost;
				}
			}
			// right
			if (next.xPos < gp.width - 1) {
				GridCell child = gp.map[next.yPos][next.xPos + 1];
				if (child.h > next.h + child.exitCost) {
					q.remove(child);
					q.add(child);
					child.h = next.h + child.exitCost;
				}
			}
			// up
			if (next.yPos > 0) {
				GridCell child = gp.map[next.yPos - 1][next.xPos];
				if (child.h > next.h + child.exitCost) {
					q.remove(child);
					q.add(child);
					child.h = next.h + child.exitCost;
				}
			}
			// down
			if (next.yPos < gp.height - 1) {
				GridCell child = gp.map[next.yPos + 1][next.xPos];
				if (child.h > next.h + child.exitCost) {
					q.remove(child);
					q.add(child);
					child.h = next.h + child.exitCost;
				}
			}
		}
	}

	public GridProblem(String path, boolean pruneParent, boolean drawNumbers,
			COST overrideCost) throws IOException, ParseException {
		this.drawNumbers = drawNumbers;
		this.pruneParent = pruneParent;
		FileInputStream fs = new FileInputStream(path);
		DataInputStream ds = new DataInputStream(fs);
		BufferedReader br = new BufferedReader(new InputStreamReader(ds));
		Scanner s = new Scanner(br);
		try {
			height = s.nextInt();
			width = s.nextInt();
			s.nextLine();
			s.nextLine();
			map = new GridCell[height][width];
			for (int i = 0; i < height; i++) {
				String row = s.nextLine();
				for (int j = 0; j < width; j++) {
					STATUS stat;
					char c = row.charAt(j);
					if (c == ' ')
						stat = STATUS.OPEN;
					else if (c == '#')
						stat = STATUS.BLOCKED;
					else {
						throw new ParseException(row, j);
					}
					map[i][j] = new GridCell(stat, j, i, 1.0);
				}
			}
			String cost = s.nextLine();
			if (overrideCost != null)
				this.cost = COST.RANDOM;
			else if (cost.compareTo("Unit") == 0)
				this.cost = COST.UNIT;
			else if (cost.compareTo("Life") == 0)
				this.cost = COST.LIFE;
			else
				this.cost = COST.UNIT;
			String movement = s.nextLine();
			if (movement.compareTo("Four-way") == 0) {
				this.movement = MOVEMENT.FOUR;
			} else if (movement.compareTo("Eight-way") == 0)
				this.movement = MOVEMENT.EIGHT;
			else
				this.movement = MOVEMENT.EIGHT;
			this.start = new GridState(s.nextInt(), height - s.nextInt() - 1,
					this);
			this.goal = new GridState(s.nextInt(), height - s.nextInt() - 1,
					this);
			Dimension preferredSize = new Dimension(defaultCellSize
					* this.height, defaultCellSize * this.width);
			this.setPreferredSize(preferredSize);
			
			if(this.cost == COST.LIFE){
				this.initHValues();
			}
			
			this.c = null;
		} finally {
			br.close();
			s.close();
		}
	}

	private static void drawGrid(GridProblem gp) {
		for (int i = 0; i < gp.map.length; i++) {
			for (int j = 0; j < gp.map[0].length; j++) {
				if (gp.map[i][j] != null)
					gp.map[i][j].hStar = Double.MAX_VALUE;
			}
		}

//		int startX = gp.getGoal().getX();
//		int startY = gp.getGoal().getY();
//		PriorityQueue<GridCell> q = new PriorityQueue<GridCell>();
//		HashSet<GridCell> closed = new HashSet<GridCell>();
//		q.add(gp.map[startY][startX]);
//		gp.map[startY][startX].hStar = 0;
//
//		while (!q.isEmpty()) {
//			GridCell next = q.poll();
//			if (closed.contains(next))
//				continue;
//			closed.add(next);
//			// left
//			if (next.xPos > 0) {
//				GridCell child = gp.map[next.yPos][next.xPos - 1];
//				if (child.s == STATUS.BLOCKED || closed.contains(child))
//					;
//				else {
//					q.add(child);
//					child.hStar = next.hStar + 1;
//				}
//			}
//			// right
//			if (next.xPos < gp.width - 1) {
//				GridCell child = gp.map[next.yPos][next.xPos + 1];
//				if (child.s == STATUS.BLOCKED || closed.contains(child))
//					;
//				else {
//					q.add(child);
//					child.hStar = next.hStar + 1;
//				}
//			}
//			// up
//			if (next.yPos > 0) {
//				GridCell child = gp.map[next.yPos - 1][next.xPos];
//				if (child.s == STATUS.BLOCKED || closed.contains(child))
//					;
//				else {
//					q.add(child);
//					child.hStar = next.hStar + 1;
//				}
//			}
//			// down
//			if (next.yPos < gp.height - 1) {
//				GridCell child = gp.map[next.yPos + 1][next.xPos];
//				if (child.s == STATUS.BLOCKED || closed.contains(child))
//					;
//				else {
//					q.add(child);
//					child.hStar = next.hStar + 1;
//				}
//			}
//		}
//
//		double maxHError = 0;
//		for (int i = 0; i < gp.map.length; i++) {
//			for (int j = 0; j < gp.map[0].length; j++) {
//				if (gp.map[i][j] != null) {
//					if (gp.map[i][j].hStar == Double.MAX_VALUE)
//						continue;
//					double h = Math.abs(i - startY) + Math.abs(j - startX);
//					double hError = gp.map[i][j].hStar - h;
//					gp.map[i][j].hError = hError;
//					if (hError > maxHError)
//						maxHError = hError;
//				}
//			}
//		}
//		gp.maxHError = maxHError;
//
//		int sz = 1500;
//		int[] hist = new int[sz];
//
//		for (GridCell[] r : gp.map) {
//			for (GridCell c : r) {
//				if (c.s == STATUS.BLOCKED || c.hError == -1)
//					continue;
//				int hErr = (int) c.hError;
//				hist[hErr]++;
//			}
//		}
//
//		for (int i = 0; i < sz; i++) {
//			System.err.printf("Index %d: %d\n", i, hist[i]);
//		}

		displayGridProblem(gp);
	}

	private double maxHError;
	private static int defaultCellSize = 12;

	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append(height);
		b.append(" ");
		b.append(width);
		b.append("\nBoard:\n");
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				b.append(map[i][j]);
			}
			b.append("\n");
		}
		switch (cost) {
		case UNIT:
			b.append("Unit\n");
			break;
		case LIFE:
			b.append("Life\n");
			break;
		case RANDOM:
			b.append("Random\n");
		}
		switch (movement) {
		case FOUR:
			b.append("Four-way\n");
			break;
		case EIGHT:
			b.append("Eight-way\n");
			break;
		case MOVINGAI:
			b.append("Eight-way (MovingAI)\n");
			break;
		}

		b.append(this.start);
		b.append("\t");
		b.append(this.goal);
		return b.toString();
	}

	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.black);
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				map[i][j].draw(g2, this.getSize());
			}
		}
	}

	private void exportPNG(String path) throws IOException {

		Dimension size = getSize();
		OutputStream out = new FileOutputStream(path);
		net.sf.epsgraphics.EpsGraphics g2 = new net.sf.epsgraphics.EpsGraphics(
				"output eps", out, 0, 0, size.width, size.height,
				net.sf.epsgraphics.ColorMode.COLOR_RGB);
		paintComponent(g2);
		g2.close();
		out.close();
	}

	private class SaveImage implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JFileChooser c = new JFileChooser();
			// Demonstrate "Open" dialog:
			int rVal = c.showSaveDialog(GridProblem.this);
			if (rVal == JFileChooser.APPROVE_OPTION) {
				String filename = (c.getSelectedFile().getName());
				String directory = (c.getCurrentDirectory().toString());
				String path = directory + "/" + filename;
				try {
					GridProblem.this.exportPNG(path);
				} catch (Exception ex) {// Catch exception if any
					System.err.println("Error: " + ex.getMessage());
				}
			}
			if (rVal == JFileChooser.CANCEL_OPTION) {
			}
		}
	}

	private SaveImage getSaveImage() {
		return new SaveImage();
	}

	public static void displayGridProblem(GridProblem gp) {
		JFrame frame = new JFrame("Grid Visualization");
		frame.setLayout(new GridBagLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		frame.add(gp, c);
		JPanel extraWidgets = new JPanel();
		extraWidgets.setLayout(new GridLayout(0, 1));
		JButton b = new JButton("Quit");
		b.addActionListener(new org.cwilt.search.utils.basic.ExitHandler());
		extraWidgets.add(b);

		JButton saveImage = new JButton("Save Image");
		saveImage.addActionListener(gp.getSaveImage());
		extraWidgets.add(saveImage);

		frame.add(extraWidgets);
		frame.pack();
		frame.setVisible(true);
	}

	public GridState getInitial() {
		return start;
	}

	public COST getCost() {
		return cost;
	}

	public MOVEMENT getMovement() {
		return movement;
	}

	private int revExpCount;

	public void reverseExpanded(int x, int y) {
		map[y][x].reverseExpanded(revExpCount++);
	}

	public GridProblem cloneNewInitial(int seed) {
		return new GridProblem(this, seed);
	}

	public static void main(String[] args) throws IOException,
			ParseException {
		if (args.length == 0) {
			System.err.println("Need to have the file to use as an argument");
			throw new IllegalArgumentException();
		}

		GridProblem g = new GridProblem(args[0], true, false, null);
		// GridProblem g = new CellGridProblem(false);
		SearchAlgorithm a = new org.cwilt.search.algs.basic.Beam(g,
				new Limit(), 5);
		// SearchAlgorithm a = new algs.experimental.GoalBlob(g, new Limit(),
		// 10000);
		@SuppressWarnings("unused")
		List<SearchState> path = a.solve();

		a.printSearchData(System.out);

		drawGrid(g);

		// for (SearchState s : path) {
		// GridState gs = (GridState) s;
		// g.onPath(gs.getX(), gs.getY());
		// System.err.println(s);
		// }
		// displayGridProblem(g);
	}

	@Override
	public ArrayList<SearchState> getGoals() {
		ArrayList<SearchState> toReturn = new ArrayList<SearchState>();
		toReturn.add(getGoal());
		return toReturn;
	}

	@Override
	public void setCalculateD() {
	}

	public void printProblemData(PrintStream p) {

	}

}
