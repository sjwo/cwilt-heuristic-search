package org.cwilt.search.domains.robot;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import org.cwilt.search.algs.experimental.bidirectional.HAddAStar;
import org.cwilt.search.algs.experimental.bidirectional.HAddAStar.HAddSearchNode;
import org.cwilt.search.domains.car.GridCell;
import org.cwilt.search.search.Limit;
import org.cwilt.search.search.SearchAlgorithm;
import org.cwilt.search.search.SearchNode;
import org.cwilt.search.search.SearchState;
public class RobotProblem implements org.cwilt.search.search.SearchProblem {
	/**
	 * 
	 */
	private final RobotState start;
	final RobotState goal;
	private final org.cwilt.search.domains.car.GridCell[][] far;

//	private final void printHeuristic() {
//		for (int i = 0; i < far.length; i++) {
//			for (int j = 0; j < far[0].length; j++) {
//				System.err.printf("%4d", far[i][j].d);
//			}
//			System.err.printf("\n");
//		}
//
//	
//		for (int i = 0; i < far.length; i++) {
//			for (int j = 0; j < far[0].length; j++) {
//				if(far[i][j].distance == Double.MAX_VALUE)
//					System.err.printf("%3.0f ", -1.0);
//				else
//					System.err.printf("%3.0f ", far[i][j].distance);
//			}
//			System.err.printf("\n");
//		}
//
//	}

	final int xSize;

	final int ySize;

	private double[][] allPairs;

	public int indexLocation(GridCell g) {
		return g.x + g.y * xSize;
	}

	private static final double rt2 = Math.sqrt(2);

	private void prepAllPairs() {
		long startTime = System.currentTimeMillis();

		int sz = xSize * ySize;

		this.allPairs = new double[sz][sz];
		for (int i = 0; i < sz; i++) {
			for (int j = 0; j < sz; j++) {
				if (i == j)
					continue;
				allPairs[i][j] = Double.MAX_VALUE;
			}
		}
		// initialize the adjacent ones
		for (int i = 0; i < xSize; i++) {
			for (int j = 0; j < ySize; j++) {
				GridCell here = far[i][j];
				ArrayList<GridCell> cardinal = here.cardinal(far);
				ArrayList<GridCell> diagonal = here.diagonal(far);
				for (GridCell g : cardinal) {
					allPairs[indexLocation(here)][indexLocation(g)] = RobotState.cellSize;
					allPairs[indexLocation(g)][indexLocation(here)] = RobotState.cellSize;
				}
				for (GridCell g : diagonal) {
					allPairs[indexLocation(here)][indexLocation(g)] = RobotState.cellSize
							* rt2;
					allPairs[indexLocation(g)][indexLocation(here)] = RobotState.cellSize
							* rt2;
				}
			}
		}

		for (int k = 0; k < sz; k++) {
			for (int i = 0; i < sz; i++) {
				for (int j = 0; j < sz; j++) {
					if (allPairs[i][k] + allPairs[k][j] < allPairs[i][j])
						allPairs[i][j] = allPairs[i][k] + allPairs[k][j];
				}
			}
		}

		long endTime = System.currentTimeMillis();
		allPairsTime = (endTime - startTime);
	}

	public void printProblemData(PrintStream p) {
		if (allPairs != null) {
			SearchAlgorithm.printPair(p, "AllPairsTime", new Double(
					((double) allPairsTime) / 1000D));
		}
	}

	private long allPairsTime;

	public RobotState getInitial() {
		return start;
	}

	public RobotProblem(String path) throws IOException,
			ParseException {
		FileInputStream fs = new FileInputStream(path);
		DataInputStream ds = new DataInputStream(fs);
		BufferedReader br = new BufferedReader(new InputStreamReader(ds));
		Scanner s = new Scanner(br);
		xSize = s.nextInt();
		ySize = s.nextInt();

		s.nextLine();
		String kind = s.nextLine();

		far = new org.cwilt.search.domains.car.GridCell[xSize][ySize];
		for (int i = 0; i < xSize; i++) {
			String row = s.nextLine();
			for (int j = 0; j < ySize; j++) {
				char next = row.charAt(j);
				boolean blocked = false;
				if (next == '#')
					blocked = true;
				int xPos = xSize - i - 1;
				far[j][xPos] = new org.cwilt.search.domains.car.GridCell(j, xPos, 1, j, xPos, blocked);
			}
		}

		int startX, startY, startH, endX, endY, endH;

		if (kind.equals("Obstacles:")) {

			startX = (s.nextInt() / RobotState.cellSize);
			startY = (s.nextInt() / RobotState.cellSize);
			startH = s.nextInt();
			endX = (s.nextInt() / RobotState.cellSize);
			endY = (s.nextInt() / RobotState.cellSize);
			endH = s.nextInt();

			// System.err.printf("%d %d\n", startX, startY);
			// System.err.printf("%d %d\n", endX, endY);

			this.start = new RobotState(this, startX, startY, startH, 0);
			this.goal = new RobotState(this, endX, endY, endH, 0);

			this.expandObstacles();

		} else if (kind.equals("Board:")) {
			String l1 = s.nextLine();
			assert (l1.equals("Unit"));
			String l2 = s.nextLine();
			assert (l2.equals("Four-way"));

			startX = (s.nextInt());
			startY = (s.nextInt());
			startH = 0;
			endX = (s.nextInt());
			endY = (s.nextInt());
			endH = 0;

			// System.err.printf("%d %d\n", startX, startY);
			// System.err.printf("%d %d\n", endX, endY);

			this.start = new RobotState(this, startX, startY, startH, 0);
			this.goal = new RobotState(this, endX, endY, endH, 0);

		} else {
			br.close();
			s.close();
			throw new ParseException(
					"Trying to parse the robot instance, doesn't look like grid or robot",
					0);
		}

		assert (validLocation(startX, startY));
		assert (validLocation(endX, endY));

		GridCell.initWorld(RobotState.cellSize, far, endX, endY, null);
		// this.printHeuristic();

		// System.err.println(start);
		// System.err.println(goal);
		br.close();
		s.close();
	}

	public RobotProblem(RobotState initial) {
		this.xSize = 5;
		this.ySize = 5;
		this.start = new RobotState(this, initial.getX(), initial.getY(),
				initial.getHeading(), initial.getSpeed());

		int endX = 2;
		int endY = 1;
		this.goal = new RobotState(this, endX, endY, 0, 0);

		this.far = new org.cwilt.search.domains.car.GridCell[xSize][ySize];

		for (int i = 0; i < xSize; i++) {
			for (int j = 0; j < ySize; j++) {
				far[i][j] = new GridCell(i * RobotState.cellSize, j
						* RobotState.cellSize, RobotState.cellSize, i, j, false);

			}
		}

		far[2][2] = new GridCell(2 * RobotState.cellSize,
				2 * RobotState.cellSize, RobotState.cellSize, 2, 2, true);

		GridCell.initWorld(RobotState.cellSize, far, endX, endY, null);

	}

	public RobotProblem() {
		this.xSize = 5;
		this.ySize = 5;

		int startX = 1;
		int startY = 1;
		int endX = 2;
		int endY = 1;
		this.start = new RobotState(this, startX, startY, 0, 0);
		this.goal = new RobotState(this, endX, endY, 0, 0);

		this.far = new org.cwilt.search.domains.car.GridCell[xSize][ySize];

		for (int i = 0; i < xSize; i++) {
			for (int j = 0; j < ySize; j++) {
				far[i][j] = new GridCell(i * RobotState.cellSize, j
						* RobotState.cellSize, RobotState.cellSize, i, j, false);

			}
		}

		far[2][2] = new GridCell(2 * RobotState.cellSize,
				2 * RobotState.cellSize, RobotState.cellSize, 2, 2, true);

		GridCell.initWorld(RobotState.cellSize, far, endX, endY, null);

	}

	public RobotProblem(String path, RobotState initial)
			throws IOException {
		FileInputStream fs = new FileInputStream(path);
		DataInputStream ds = new DataInputStream(fs);
		BufferedReader br = new BufferedReader(new InputStreamReader(ds));
		Scanner s = new Scanner(br);
		xSize = s.nextInt();
		ySize = s.nextInt();

		s.nextLine();
		s.nextLine();

		far = new org.cwilt.search.domains.car.GridCell[xSize][ySize];
		for (int i = 0; i < xSize; i++) {
			String row = s.nextLine();
			for (int j = 0; j < ySize; j++) {
				char next = row.charAt(j);
				boolean blocked = false;
				if (next == '#')
					blocked = true;
				int xPos = xSize - i - 1;
				far[j][xPos] = new org.cwilt.search.domains.car.GridCell(j, xPos, 1, j, xPos, blocked);
			}
		}

		int startX = (s.nextInt() / RobotState.cellSize);
		int startY = (s.nextInt() / RobotState.cellSize);
		s.nextInt();
		int endX = (s.nextInt() / RobotState.cellSize);
		int endY = (s.nextInt() / RobotState.cellSize);
		int endH = s.nextInt();

		// System.err.printf("%d %d\n", startX, startY);
		// System.err.printf("%d %d\n", endX, endY);

		this.start = new RobotState(this, initial.getX(), initial.getY(),
				initial.getHeading(), initial.getSpeed());
		this.goal = new RobotState(this, endX, endY, endH, 0);

		this.expandObstacles();

		assert (validLocation(startX, startY));
		assert (validLocation(endX, endY));

		GridCell.initWorld(RobotState.cellSize, far, endX, endY, null);
		// this.printHeuristic();

		// System.err.println(start);
		// System.err.println(goal);
		br.close();
		s.close();

	}

	public boolean isGoal(RobotState s) {
		return goal.equals(s);
	}

	public double distTo(RobotState start, RobotState end) {
		if (allPairs == null) {
			prepAllPairs();
		}
		return allPairs[start.indexLocation()][end.indexLocation()]
				/ RobotState.maxSpeed;
	}

	public double getH(RobotState s) {
		// return 0;
		return far[s.getX()][s.getY()].distance / RobotState.maxSpeed;
	}

	public int getD(RobotState s) {
		return far[s.getX()][s.getY()].d;
	}

	public boolean validLocation(int x, int y) {
		if (x < 0)
			return false;
		if (y < 0)
			return false;
		if (x >= xSize)
			return false;
		if (y >= ySize)
			return false;

		if (far[x][y].r == null)
			return false;
		return true;
	}

	public static void main(String[] args) throws ParseException, IOException {
		// RobotProblem p = new
		RobotProblem p = new RobotProblem("/home/aifs2/group/data/dyn_robot_instances/instance/liney/200/200/25/10");
//		RobotProblem p = new RobotProblem("/home/aifs2/cmo66/cjava/santa/robotdata/misc/tiny");

		//SearchAlgorithm a;
		// a = new algs.basic.Greedy(p, new Limit());
		// a = new algs.basic.WAStar(p.getStart(), new Limit(), 10);
//		a = new algs.basic.bestfirst.AStar(p, new Limit());
//		System.err.println(SearchAlgorithm.printPath(a.solve()));
//		a.printSearchData(System.err);

		org.cwilt.search.algs.experimental.bidirectional.HAddAStar a = new org.cwilt.search.algs.experimental.bidirectional.HAddAStar(
				p, new Limit(), 1);
//		System.err.println(SearchAlgorithm.printPath(a1.solve()));
//		System.err.println(SearchAlgorithm.printPath(a1.getFinalPath()));
//		a1.printSearchData(System.err);
		a.solve();
		a.printSearchData(System.out);
	    BufferedImage bImg = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
	    Graphics2D cg = bImg.createGraphics();
	    p.draw(cg, a);
	    try {
	            if (ImageIO.write(bImg, "png", new File("/home/aifs2/cmo66/robot2.png")))
	            	System.out.println("-- saved");
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	
	}

	private static boolean nearObstacle(int x, int y, boolean[][] blocked) {
		boolean canLeft = x > 0;
		boolean canUp = y > 0;
		boolean canRight = x < blocked.length - 1;
		boolean canDown = y < blocked[0].length - 1;
		if (canLeft && blocked[x - 1][y])
			return true;
		if (canRight && blocked[x + 1][y])
			return true;
		if (canUp && blocked[x][y - 1])
			return true;
		if (canDown && blocked[x][y + 1])
			return true;
		if (canLeft && canUp && blocked[x - 1][y - 1])
			return true;
		if (canRight && canUp && blocked[x + 1][y - 1])
			return true;
		if (canLeft && canDown && blocked[x - 1][y + 1])
			return true;
		if (canRight && canDown && blocked[x + 1][y + 1])
			return true;
		return false;
	}

	private void expandObstacles() {
		boolean[][] blocked = new boolean[far.length][far[0].length];
		for (int i = 0; i < far.length; i++) {
			for (int j = 0; j < far[0].length; j++) {
				if (far[i][j].r == null)
					blocked[i][j] = true;
				else
					blocked[i][j] = false;
			}
		}
		for (int i = 0; i < far.length; i++) {
			for (int j = 0; j < far[0].length; j++) {
				if (nearObstacle(i, j, blocked))
					far[i][j].r = null;
			}
		}

	}

	@Override
	public SearchState getGoal() {
		return goal;
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

	public void draw(Graphics2D g2d, HAddAStar search) {
		// paint the map
		for (GridCell[] row : far) {
			for (GridCell g : row) {
				// blocked
				if (g.r == null)
					g2d.setColor(Color.black);
				else
					g2d.setColor(Color.white);
				g2d.fill(new Rectangle(g.x * 2, g.y * 2, 2, 2));
			}
		}

		// now paint the cells that got expanded
		HashMap<Object, HAddSearchNode> expanded = search.getExpanded();

		double minForwards = Double.MAX_VALUE;
		double minBackwards = Double.MAX_VALUE;

		PriorityQueue<HAddSearchNode> forwards = new PriorityQueue<HAddSearchNode>(
				10, new SearchNode.FGComparator());
		PriorityQueue<HAddSearchNode> backwards = new PriorityQueue<HAddSearchNode>(
				10, new SearchNode.GComparator());
		HashMap<Integer, AtomicInteger> forwardUsage = new HashMap<Integer, AtomicInteger>();
		HashMap<Integer, AtomicInteger> backwardUsage = new HashMap<Integer, AtomicInteger>();
		
		for (Entry<Object, HAddSearchNode> e : expanded.entrySet()) {
			HAddSearchNode n = e.getValue();
			RobotState s = (RobotState) n.getState();
			
			int here = s.getX() + s.getY() * 1000;
			
			if (n.r == HAddAStar.REASON.EXP) {
				if (n.getG() < minForwards)
					minForwards = n.getG();
				forwards.add(n);
				
				AtomicInteger count = forwardUsage.get(new Integer(here));
				if(count == null){
					forwardUsage.put(here, new AtomicInteger(1));
				} else {
					count.incrementAndGet();
				}
				
			}
			if (n.r == HAddAStar.REASON.CACHE) {
				if (n.getG() < minBackwards)
					minBackwards = n.getG();
				backwards.add(n);

				AtomicInteger count = backwardUsage.get(new Integer(here));
				if(count == null){
					backwardUsage.put(here, new AtomicInteger(1));
				} else {
					count.incrementAndGet();
				}

			}
		}
		
		double maxUsage = 2000;
		
		while(!forwards.isEmpty()){
			HAddSearchNode next = forwards.poll();
			RobotState s = (RobotState) next.getState();

			int here = s.getX() + s.getY() * 1000;
			AtomicInteger count = forwardUsage.get(new Integer(here));

			g2d.setColor(getForwardsColor(count.doubleValue(), maxUsage));
			g2d.fill(new Rectangle(s.getX() * 2, s.getY() * 2, 2, 2));
		}

		while(!backwards.isEmpty()){
			HAddSearchNode next = backwards.poll();
			RobotState s = (RobotState) next.getState();

			int here = s.getX() + s.getY() * 1000;
			AtomicInteger count = backwardUsage.get(new Integer(here));

			g2d.setColor(getBackwardsColor(count.doubleValue(), maxUsage));
			g2d.fill(new Rectangle(s.getX() * 2, s.getY() * 2, 2, 2));
		}
	}

	private static final Color getBackwardsColor(double total, double max) {
		float alpha = (float) (total / max);
		if(alpha > 1){
			alpha = 1;
		}
		return new Color(0.0f, 0.0f, 1.0f, alpha);
	}

	private static final Color getForwardsColor(double total, double max) {
		float alpha = (float) (total / max);
		if(alpha > 1){
			alpha = 1;
		}
		return new Color(1.0f, 0.0f, 0.0f, alpha);
	}

}
