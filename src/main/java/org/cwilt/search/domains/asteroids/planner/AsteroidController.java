package org.cwilt.search.domains.asteroids.planner;
import org.cwilt.search.domains.asteroids.AsteroidProblem;
import org.cwilt.search.domains.asteroids.Asteroids;
import org.cwilt.search.domains.asteroids.planner.AsteroidPlanner.CrashedShip;
import org.cwilt.search.domains.asteroids.planner.AsteroidPlanner.SolverTimeout;
public class AsteroidController {
	
	public static void exitFail(){
		System.err.println("Usage:");
		System.err.println("<alg> <alg options>");
		System.exit(1);
	}
	
	public static void main(String[] args){
		
		int nAsteroids = Integer.parseInt(args[9]);
		AsteroidProblem g = new AsteroidProblem(nAsteroids);
		
		AsteroidPlanner planner = null;
		
		if(args.length < 1){
			exitFail();
		}
		if(args[0].equals("vettingRRT"))
			planner = new VettingRRTPlanner(g, 50000, args);
		else if(args[0].equals("RRT"))
			planner = new RRTPlanner(g, 50000, args);
		else if(args[0].equals("seededRRT"))
			planner = new SeededRRTPlanner(g, 50000, args);
		else if(args[0].equals("kasra"))
			planner = new org.cwilt.search.domains.asteroids.planner.kasra.RealTimeController(g, 50000, args);
		else if(args[0].equals("graftingRRT"))
			planner = new GraftingRRTPlanner(g, 50000, args);
		else if(args[0].equals("parallelRRT"))
			planner = new ParallelRRTPlanner(g, 50000, args);
		else if(args[0].equals("rtastar"))
			planner = new RTAStarPlanner(g, 50000, args);
		else if(args[0].equals("random"))
			planner = new RandomController(g, 33, args);
		else if(args[0].equals("threadRRT"))
			planner = new MultithreadedVettingRRTPlanner(g, 50000, args);
		else {
			AsteroidController.exitFail();
		}
		boolean display = false;
		
		if(args[1].equals("false"))
			display = false;
		else if(args[1].equals("true"))
			display = true;
		else {
			AsteroidController.exitFail();
		}
			
		try {
			planner.solve();
		} catch (SolverTimeout e) {
			System.err.println(e.getMessage());
		} catch (CrashedShip e) {
			System.err.println(e.getMessage());
		}
		double points = planner.getPlan().get(planner.getPlan().size() - 1).getScore();
		System.out.println("Scored " + points + " points");
		System.out.println("Lasted " + planner.getPlan().size() + " steps");
		System.out.println("Solving took " + planner.getSolveTime() + " milliseconds");
		System.out.println("Longest step took " + planner.getMaxStepTime() + " milliseconds");
		if(display){
			Asteroids a = new Asteroids(g);
			a.setPath(planner.getPlan());
			Asteroids.runGUI(a);
		}
	}
}
