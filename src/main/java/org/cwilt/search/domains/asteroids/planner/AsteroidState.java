package org.cwilt.search.domains.asteroids.planner;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.cwilt.search.domains.asteroids.Asteroid;
import org.cwilt.search.domains.asteroids.BasicShip;
import org.cwilt.search.domains.asteroids.PhaserPulse;
import org.cwilt.search.domains.asteroids.Ship;
import org.cwilt.search.search.SearchState;
public class AsteroidState extends SearchState {
	public double getScore(){
		return score;
	}
	
	public double getValue(){
		double thetaTo = Double.MAX_VALUE;
		
		for(Asteroid a : asteroids){
			double headingTo = ship.angleTo(a);
			if(thetaTo > headingTo)
				thetaTo = headingTo;
		}
		

		assert(score - thetaTo < Double.MAX_VALUE);
		return score - thetaTo;
	}
	
	private final double score;
	private final double maxScore;
	
	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("AsteroidState [ship=");
		b.append(ship);
		b.append(", time=");
		b.append(time);
		b.append("]\n");
//		b.append(asteroids);
//		b.append("\n");
//		b.append(pulses);
		return b.toString();
	}

	public Ship getShip(){
		return ship;
	}
	private final AsteroidState parent;
	public AsteroidState getParent(){
		return parent;
	}
	
	public List<Asteroid> getAsteroids() {
		return asteroids;
	}

	public List<PhaserPulse> getPulses() {
		return pulses;
	}



	private final List<Asteroid> asteroids;
	private final List<PhaserPulse> pulses;
	private final Ship ship;
	private final int time;
	private static final double SCORE_PER_ASTEROID = 100;
	
	public AsteroidState(List<Asteroid> asteroids, List<PhaserPulse> pulses, Ship ship){
		assert(ship != null);
		assert(asteroids != null);
		assert(pulses != null);
		this.score = 0;
		double nAsteroids = asteroids.size();
		this.maxScore = nAsteroids * 15 * SCORE_PER_ASTEROID;
		this.asteroids = asteroids;
		this.pulses = pulses;
		this.ship = ship;
		this.time = 0;
		this.parent = null;
	}

	private AsteroidState(Ship s, int time){
		this.ship = s;
		this.time = time;
		this.score = 0;
		this.pulses = null;
		this.parent = null;
		this.asteroids = null;
		this.maxScore = 0;
	}
	
	private AsteroidState(List<Asteroid> asteroids, List<PhaserPulse> pulses, Ship s, AsteroidState parent, double score){
		assert(s != null);
		assert(asteroids != null);
		assert(pulses != null);
		assert(parent != null);
		this.score = score;
		this.asteroids = asteroids;
		this.pulses = pulses;
		this.ship = s;
		this.time = parent.time + 1;
		this.maxScore = parent.maxScore;
		this.parent = parent;
	}
	
	@Override
	public ArrayList<Child> expand() {
		assert(ship != null);
		ArrayList<Set<Integer>> actions = ship.getActions();
		assert(actions != null);
		ArrayList<Child> children = new ArrayList<Child>(actions.size());
		for(Set<Integer> action : actions){
			AsteroidState next = this.transition(action);
			if(next == null)
				continue;
			children.add(new Child(next, 1.0));
		}
		return children;
	}

	public ArrayList<AsteroidState> expandRaw() {
		assert(ship != null);
		assert(!ship.isTerminal());
		ArrayList<Set<Integer>> actions = ship.getActions();
		ArrayList<AsteroidState>  children= new ArrayList<AsteroidState>();
		assert(actions != null);
		for(Set<Integer> action : actions){
			AsteroidState next = this.transition(action);
			if(next == null)
				continue;
			children.add(next);
		}
		return children;
	}

	
	@Override
	public ArrayList<Child> reverseExpand() {
		throw new UnsupportedOperationException();
	}

	@Override
	public double h() {
		return maxScore - score;
	}

	@Override
	public int d() {
		return 0;
	}

	@Override
	public boolean isGoal() {
		return asteroids.size() == 0;
	}

	@Override
	public Object getKey() {
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((asteroids == null) ? 0 : asteroids.hashCode());
		result = prime * result + ((pulses == null) ? 0 : pulses.hashCode());
		result = prime * result + ((ship == null) ? 0 : ship.hashCode());
		result = prime * result + time;
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
		AsteroidState other = (AsteroidState) obj;
		if (asteroids == null) {
			if (other.asteroids != null)
				return false;
		} else if (!asteroids.equals(other.asteroids))
			return false;
		if (pulses == null) {
			if (other.pulses != null)
				return false;
		} else if (!pulses.equals(other.pulses))
			return false;
		if (ship == null) {
			if (other.ship != null)
				return false;
		} else if (!ship.equals(other.ship))
			return false;
		if (time != other.time)
			return false;
		return true;
	}

	@Override
	public int lexOrder(SearchState s) {
		if(s == this)
			return 0;
		else
			throw new RuntimeException("found a duplicate?");
	}

	public AsteroidState transition(Set<Integer> controls) {
		//advance the asteroids
		LinkedList<Asteroid> nextAsteroids = new LinkedList<Asteroid>();
		for(Asteroid a : asteroids){
			Asteroid next = Asteroid.advanceTime(a);
			nextAsteroids.add(next);
		}
		//advance the phaser pulses
		LinkedList<PhaserPulse> newPulses = new LinkedList<PhaserPulse>();
		for(PhaserPulse p : pulses){
			PhaserPulse next = PhaserPulse.advanceTime(p);
			if(next != null && !next.offScreen())
				newPulses.add(next);
		}

		//advance the ship
		Ship newShip = ship.expand(controls);
		if(newShip.isTerminal()){
			return new AsteroidState(nextAsteroids, newPulses, newShip, this, this.score);
		}
		
		double pointsScored = 0;
		
		if (controls.contains(KeyEvent.VK_SPACE) && ship.canShoot()) {
			newPulses.add(ship.shootGun());
		}
		ListIterator<Asteroid> aIter = nextAsteroids.listIterator();
		while(aIter.hasNext()){
			Asteroid a = aIter.next();
			if(newShip.collidesWith(a)){
				Ship deadShip = newShip.explode();
				return new AsteroidState(nextAsteroids, newPulses, deadShip, this, this.score + pointsScored);
			}
			
			ListIterator<PhaserPulse> i = newPulses.listIterator();
			while(i.hasNext()){
				PhaserPulse p = i.next();
				//the pulse hit the asteroid
				if(a.asteroid.intersects(p.shot.getBounds2D())){
					i.remove();
					aIter.remove();
					//add the new asteroids, but the pulse has been consumed.
					a.split(aIter,  p);
					pointsScored += SCORE_PER_ASTEROID;
					break;
				}
			}
			
		}

		
		return new AsteroidState(nextAsteroids, newPulses, newShip, this, this.score + pointsScored);
	}
	
	public static AsteroidState dummyState(double x, double y, double thetaSample, int time){
		Ship s = new BasicShip(thetaSample, x, y, null);
		return new AsteroidState(s, time);
	}

	public int getTime() {
		return time;
	}
	
	public boolean isTerminal(){
		return ship.isTerminal();
	}
	
	private static final int[] polyX = {-6, 0, 6, 0};
	private static final int[] polyY = {-4, 8, -4, 0};
	
	
	public void drawRRTVisualization(Graphics2D g, int minTime, int horizon) {
		AffineTransform t = g.getTransform();
		g.translate(ship.x, ship.y);
		g.rotate(ship.theta);
		int deltaT = this.time - minTime;
		int prop = 255 - (horizon*6) + (deltaT * 6);
		Color c = new Color(255, 0, 0, prop);
		g.setColor(c);
		g.fillPolygon(polyX, polyY, 4);
		g.setTransform(t);
	}
	public void drawRRTTerminal(Graphics2D g) {
		AffineTransform t = g.getTransform();
		g.translate(ship.x, ship.y);
		g.rotate(ship.theta);
		Color c = new Color(0, 255, 0);
		g.setColor(c);
		g.fillPolygon(polyX, polyY, 4);
		g.setTransform(t);
	}

}
