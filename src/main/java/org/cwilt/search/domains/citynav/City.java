package org.cwilt.search.domains.citynav;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class City {
	private final Place[] places;
	public final Citynav citynav;
	private final double[] neighborCosts;
	final City[] neighbors;
	public final int id;
	public final Point2D.Double loc;
	
	private final ArrayList<Double> revNeighborCosts;
	private final ArrayList<City> revNeighbors;
	
	public Place getPlace(int ix){
		return places[ix];
	}
	
	public List<Place> getAllPlaces(){
		return Collections.unmodifiableList(Arrays.asList(this.places));
	}
	
	public String toString(){
		StringBuffer b = new StringBuffer();
		
		b.append("City: ");
		b.append(id);
		b.append(" with the following places:\n");
		for(Place p : places){
			b.append(p);
			b.append("\n");
		}

		b.append("Connected to the following: \n");
		for(int i = 0; i < neighbors.length; i++){
			City c = neighbors[i];
			b.append("\t");
			if(c != null){
				b.append(c.id);
				b.append(" at a cost of ");
				b.append(neighborCosts[i]);
			}
			else{
				b.append("null");
			}
			b.append("\n");
		}
		
		return b.toString();
	}
	
	private static class PlaceComparator implements Comparator<Place>{

		private final Place here;
		public PlaceComparator(Place here){
			this.here = here;
		}
		
		@Override
		public int compare(Place arg0, Place arg1) {
			if(arg0 == here)
				return 1;
			else if(arg1 == here)
				return -1;
			double dist1 = arg0.distTo(here);
			double dist2 = arg1.distTo(here);
			if(dist1 == dist2)
				return 0;
			else if(dist1 < dist2)
				return -1;
			else
				return 1;
		}
		
	}
	
	public double distTo(City other){
		return loc.distance(other.loc);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		City other = (City) obj;
		if (id != other.id)
			return false;
		return true;
	}

	
	public City(int nPlaces, int nCityNeighbors, int nPlaceNeighbors, int id, Citynav citynav){
		this.citynav = citynav;
		this.id = id;
		this.neighbors = new City[nCityNeighbors + 1];
		this.neighborCosts = new double[nCityNeighbors + 1];
		
		this.revNeighbors = new ArrayList<City>(nCityNeighbors + 1);
		this.revNeighborCosts = new ArrayList<Double>(nCityNeighbors + 1);
		
		double xLoc = citynav.nextDouble() * citynav.worldSize;
		double yLoc = citynav.nextDouble() * citynav.worldSize;
		this.loc = new Point2D.Double(xLoc, yLoc);

		this.places = new Place[nPlaces];
		
		for(int i = 0; i < nPlaces; i++){
			places[i] = new Place(nPlaceNeighbors, i, this);
		}

		for(int i = 0; i < nPlaces; i++){
			Place here = places[i];
			Comparator<Place> pc = new PlaceComparator(here);
			PriorityQueue<Place> p = new PriorityQueue<Place>(nPlaces, pc);
			for(int j = 0; j < nPlaces; j++){
				//don't add the point here
				if(i == j)
					continue;
				Place there = places[j];
				//just add it
				if(p.size() < nPlaceNeighbors){
					p.add(there);
				} else if(pc.compare(there, p.peek()) < 0){
					p.poll();
					p.add(there);
				}
			}
			for(int ix = 0; ix < nPlaceNeighbors; ix++){
				Place there = p.poll();
				double cost = here.distTo(there) * (citynav.nextDouble() * COST_RATIO + 1);
				here.connect(there, cost, ix);
			}
		}
		//TODO make sure this works correctly
		for(int i = 0; i < nPlaces; i++){
			Place here = places[i];
			Place there = places[(i + 1) % nPlaces];
			double cost = here.distTo(there) * (citynav.nextDouble() * COST_RATIO + 1);
			here.connect(there, cost, nPlaceNeighbors);
		}
	}
	private static final double COST_RATIO = 0.1;
	
	public Place getIn(){
		return places[0];
	}
	
	public static class Highway{
		public final Place destination;
		public final double cost;
		public Highway(City start, int ix, double cost, boolean forwards){
			if(forwards)
				this.destination = start.neighbors[ix].getIn();
			else 
				this.destination = start.revNeighbors.get(ix).getIn();
			this.cost = cost;
		}
	}
	
	public Highway[] expandAirport(){
		Highway[] toReturn = new Highway[neighbors.length];
		for(int i = 0; i < neighbors.length; i++){
			toReturn[i] = new Highway(this, i, this.neighborCosts[i], true);
		}
		return toReturn;
	}

	public Highway[] revExpandAirport(){
		Highway[] toReturn = new Highway[revNeighbors.size()];
		for(int i = 0; i < revNeighbors.size(); i++){
			toReturn[i] = new Highway(this, i, this.revNeighborCosts.get(i), false);
		}
		return toReturn;
	}
	
	public void connect(City there, double cost, int ix){
		neighbors[ix] = there;
		neighborCosts[ix] = cost;
		there.revConnect(this, cost);
	}
	
	private void revConnect(City there, double cost){
		revNeighbors.add(there);
		revNeighborCosts.add(cost);
	}
}
