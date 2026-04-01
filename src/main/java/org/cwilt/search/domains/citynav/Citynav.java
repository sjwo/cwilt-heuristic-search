package org.cwilt.search.domains.citynav;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.cwilt.search.search.Limit;
import org.cwilt.search.search.SearchAlgorithm;
import org.cwilt.search.search.SearchProblem;
import org.cwilt.search.search.SearchState;
import org.cwilt.search.search.SearchState.Child;
public class Citynav extends JPanel implements SearchProblem {

	private double[][] allPairs;
	private Map<Place, Integer> indexes;

	public void initFloydWarshall() {
		this.indexes = new HashMap<Place, Integer>();
		int currentIndex = 0;
		for (City c : cities) {
			for (Place p : c.getAllPlaces()) {
				indexes.put(p, currentIndex);
				currentIndex++;
			}
		}
		allPairs = new double[indexes.size()][];
		for (int i = 0; i < indexes.size(); i++) {
			allPairs[i] = new double[indexes.size()];
			for (int j = 0; j < indexes.size(); j++) {
				allPairs[i][j] = Double.MAX_VALUE;
			}
		}

		// do floyd warshall

		// init cost from each vertex to itself to be 0
		for (int i = 0; i < allPairs.length; i++) {
			allPairs[i][i] = 0;
		}

		// init the price of the edges
		for (City c : cities) {
			for (Place p : c.getAllPlaces()) {
				ArrayList<Child> children = p.expand();
				for (Child child : children) {
					allPairs[indexes.get(p)][indexes.get(child.child)] = child.transitionCost;
				}
			}
		}

		// do the algorithm
		for (int k = 0; k < allPairs.length; k++) {
			for (int i = 0; i < allPairs.length; i++) {
				for (int j = 0; j < allPairs.length; j++) {
					if(allPairs[i][j] > allPairs[i][k] + allPairs[k][j]){
						allPairs[i][j] = allPairs[i][k] + allPairs[k][j];
					}
				}
			}
		}
	}

	public double getHStar(Place p1, Place p2) {
		if (allPairs == null) {
			initFloydWarshall();
		}

		int p1Index = indexes.get(p1);
		int p2Index = indexes.get(p2);
		return allPairs[p1Index][p2Index];
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 3108350994925880298L;

	public String toString() {
		StringBuffer b = new StringBuffer();

		for (City c : cities) {
			b.append(c);
		}

		return b.toString();
	}

	public enum HEURISTIC_TYPE {
		EUCLIDEAN, PORTAL
	}

	HEURISTIC_TYPE heuristic = HEURISTIC_TYPE.EUCLIDEAN;

	public void setPortalHeuristic(CitynavPortalHeuristic cph) {
		this.heuristic = HEURISTIC_TYPE.PORTAL;
		this.portal = cph;
	}

	private CitynavPortalHeuristic portal = null;

	public CitynavPortalHeuristic getPortalHeuristic() {
		return this.portal;
	}

	public List<City> getAllCities() {

		return Collections.unmodifiableList(Arrays.asList(this.cities));
	}

	private final City[] cities;
	private final Random r;
	private final int seed;
	public final double worldSize;

	public Place getEnd() {
		return this.end;
	}

	public void setStart(Place p) {
		this.start = p;
	}
	public void setEnd(Place p){
		this.end = p;
	}
	
	private Place start;
	private Place end;

	public double nextDouble() {
		return r.nextDouble();
	}

	private static class CityComparator implements Comparator<City> {

		private final City here;

		public CityComparator(City here) {
			this.here = here;
		}

		@Override
		public int compare(City arg0, City arg1) {
			if (arg0 == here)
				throw new RuntimeException();
			else if (arg1 == here)
				throw new RuntimeException();
			double dist1 = arg0.distTo(here);
			double dist2 = arg1.distTo(here);
			if (dist1 == dist2)
				return 0;
			else if (dist1 < dist2)
				return 1;
			else
				return -1;
		}

	}

	public Citynav(int nCities, int nPlaces, int nCityNeighbors, int nPlaceNeighbors, double worldSize, int seed) {
		this.seed = seed;
		this.worldSize = worldSize;
		this.r = new Random(this.seed);
		this.cities = new City[nCities];

		for (int i = 0; i < nCities; i++) {
			cities[i] = new City(nPlaces, nCityNeighbors, nPlaceNeighbors, i, this);
		}

		for (int i = 0; i < nCities; i++) {
			City here = cities[i];
			Comparator<City> pc = new CityComparator(here);
			PriorityQueue<City> p = new PriorityQueue<City>(nPlaces, pc);
			for (int j = 0; j < nCities; j++) {
				// don't add the point here
				if (i == j)
					continue;
				City there = cities[j];
				// just add it
				if (p.size() < nCityNeighbors) {
					p.add(there);
				} else if (pc.compare(there, p.peek()) > 0) {
					p.poll();
					p.add(there);
				}
			}
			for (int ix = 0; ix < nCityNeighbors; ix++) {
				City there = p.poll();
				double cost = here.distTo(there) + 2;
				here.connect(there, cost, ix);
			}
		}

		for (int i = 0; i < nCities; i++) {
			City here = cities[i];
			City there = cities[(i + 1) % nCities];
			double cost = here.distTo(there) + 2;
			here.connect(there, cost, nCityNeighbors);
		}

		int startIX = r.nextInt(nPlaces);
		int endIX = r.nextInt(nPlaces);

		// TODO fix this
		this.start = cities[0].getPlace(startIX);
		this.end = cities[cities.length - 1].getPlace(endIX);

		scale = 500 / worldSize;

		Dimension preferredSize = new Dimension(500, 500);
		this.setPreferredSize(preferredSize);

	}

	private final double scale;

	@Override
	public SearchState getInitial() {
		return start;
	}

	@Override
	public SearchState getGoal() {
		return end;
	}

	@Override
	public ArrayList<SearchState> getGoals() {
		ArrayList<SearchState> toReturn = new ArrayList<SearchState>();
		toReturn.add(getGoal());
		return toReturn;
	}

	public static void main(String[] args) {
		Citynav test = new Citynav(15, 15, 3, 3, 100.0, 66);
		// System.out.println(test);

		SearchAlgorithm a = new org.cwilt.search.algs.basic.bestfirst.Greedy(test, new Limit());
		a.solve();
		a.printSearchData(System.out);

		displayProblem(test);
	}

	public static void displayProblem(Citynav cp) {
		JFrame frame = new JFrame("City Navigation Visualization");
		frame.setLayout(new GridBagLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		frame.add(cp, c);
		JPanel extraWidgets = new JPanel();
		extraWidgets.setLayout(new GridLayout(0, 1));
		JButton b = new JButton("Quit");
		b.addActionListener(new org.cwilt.search.utils.basic.ExitHandler());
		extraWidgets.add(b);

		frame.add(extraWidgets);
		frame.pack();
		frame.setVisible(true);
	}

	private static final double CITY_SIZE = 10;

	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.black);
		for (int i = 0; i < cities.length; i++) {
			City here = cities[i];
			Ellipse2D e = new Ellipse2D.Double((here.loc.x) * scale - CITY_SIZE / 2,
					(here.loc.y) * scale - CITY_SIZE / 2, CITY_SIZE, CITY_SIZE);
			g2.draw(e);
			for (City c : here.neighbors) {
				Line2D.Double arc = new Line2D.Double(c.loc.x * scale, c.loc.y * scale, here.loc.x * scale,
						here.loc.y * scale);
				g2.draw(arc);
			}
		}
	}

	@Override
	public void setCalculateD() {
	}

	public void printProblemData(PrintStream p) {

	}

}
