package org.cwilt.search.domains.citynav;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.cwilt.search.domains.citynav.City.Highway;
import org.cwilt.search.domains.citynav.Citynav.HEURISTIC_TYPE;
import org.cwilt.search.search.SearchState;
public class Place extends SearchState {
	private final City city;
	private final Point2D.Double location;
	public final int id;
	private final Place[] neighbors;
	private final double[] neighborCosts;
	private final ArrayList<Place> revNeighbors;
	private final ArrayList<Double> revNeighborCosts;

	public void connect(Place p, double cost, int ix) {
		neighbors[ix] = p;
		neighborCosts[ix] = cost;
		p.revConnect(this, cost);
	}

	private void revConnect(Place p, double cost) {
		revNeighbors.add(p);
		revNeighborCosts.add(cost);
	}

	public String toString() {
		StringBuffer b = new StringBuffer();

		b.append("city: ");
		b.append(city.id);
		b.append(" place: ");
		b.append(this.id);
		b.append(" at ");
		b.append(location);
		b.append(" connected to:\n");

		for (int i = 0; i < neighbors.length; i++) {
			Place p = neighbors[i];
			b.append("\t");
			if (p != null) {
				b.append(p.id);
				b.append(" at a cost of ");
				b.append(neighborCosts[i]);
			} else {
				b.append("null");
			}
			b.append("\n");

		}

		return b.toString();
	}

	@Override
	public double distTo(SearchState s) {
		return distTo((Place) s);
	}

	public double distTo(Place p) {
		if (this.city.citynav.heuristic == HEURISTIC_TYPE.EUCLIDEAN) {
			return this.location.distance(p.location);
		} else {
			return city.citynav.getPortalHeuristic().calculateHeuristic(this, p);
		}
	}

	@Override
	public double h() {
		if (this.city.citynav.heuristic == HEURISTIC_TYPE.EUCLIDEAN) {
			return this.distTo(city.citynav.getEnd());
		} else {
			return city.citynav.getPortalHeuristic().calculateHeuristic(this, city.citynav.getEnd());
		}
	}

	public Place(int nct, int id, City city) {
		this.city = city;
		this.neighbors = new Place[nct + 1];
		this.neighborCosts = new double[nct + 1];
		this.revNeighbors = new ArrayList<Place>(nct + 1);
		this.revNeighborCosts = new ArrayList<Double>(nct + 1);
		this.id = id;
		assert (city != null);
		assert (city.citynav != null);
		double xLoc = city.citynav.nextDouble() + city.loc.x;
		double yLoc = city.citynav.nextDouble() + city.loc.y;
		this.location = new Point2D.Double(xLoc, yLoc);
	}

	@Override
	public ArrayList<Child> expand() {
		ArrayList<Child> toReturn = new ArrayList<Child>();

		for (int i = 0; i < neighbors.length; i++) {
			toReturn.add(new Child(neighbors[i], neighborCosts[i]));
		}
		if (this.isOutnode()) {
			Highway[] out = city.expandAirport();
			for (Highway h : out) {
				toReturn.add(new Child(h.destination, h.cost));
			}
		}
		return toReturn;
	}

	private boolean isOutnode() {
		return this.id == 0;
	}

	@Override
	public ArrayList<Child> reverseExpand() {
		ArrayList<Child> toReturn = new ArrayList<Child>();

		for (int i = 0; i < revNeighbors.size(); i++) {
			toReturn.add(new Child(revNeighbors.get(i), revNeighborCosts.get(i)));
		}
		if (this.isOutnode()) {
			Highway[] out = city.revExpandAirport();
			for (Highway h : out) {
				toReturn.add(new Child(h.destination, h.cost));
			}
		}
		return toReturn;
	}

	@Override
	public boolean isGoal() {
		return this.equals(city.citynav.getEnd());
	}

	private static class PlaceKey {
		private final int placeID;
		private final int cityID;

		public PlaceKey(Place p) {
			this.placeID = p.id;
			this.cityID = p.city.id;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + cityID;
			result = prime * result + placeID;
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
			PlaceKey other = (PlaceKey) obj;
			if (cityID != other.cityID)
				return false;
			if (placeID != other.placeID)
				return false;
			return true;
		}

	}

	@Override
	public Object getKey() {
		return new PlaceKey(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result + id;
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
		Place other = (Place) obj;
		if (city == null) {
			if (other.city != null)
				return false;
		} else if (!city.equals(other.city))
			return false;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public int lexOrder(SearchState s) {
		throw new org.cwilt.search.utils.basic.NotImplementedException();
	}

	@Override
	public int d() {
		if (this.isGoal())
			return 0;
		else
			return 1;
	}

}
