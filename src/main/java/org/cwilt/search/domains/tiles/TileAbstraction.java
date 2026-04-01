package org.cwilt.search.domains.tiles;
import java.io.Serializable;
import java.util.BitSet;

public class TileAbstraction implements Cloneable, Serializable{
	/**
     * 
     */
    private static final long serialVersionUID = 5069513435893400291L;
    /**
	 * 
	 */
    
	private final double[] costs;
	private final BitSet abstractedTiles;
	private TileAbstraction(double[] costs){
		this.costs = costs;
		this.abstractedTiles = new BitSet();
		abstractedTiles.clear(0, costs.length);
	}
	
	public static TileAbstraction makeInverse(int length){
		double[] ary = new double[length];
		for(double i = 1; i < length; i++){
			ary[(int) i] = 1.0/ i;
		}
		return new TileAbstraction(ary);
	}

	public static TileAbstraction makeInverseSquare(int length){
		double[] ary = new double[length];
		for(double i = 1; i < length; i++){
			ary[(int) i] = 1.0/ (i * i);
		}
		return new TileAbstraction(ary);
	}

	public static TileAbstraction makeSqrt(int length){
		double[] ary = new double[length];
		for(double i = 1; i < length; i++){
			ary[(int) i] = Math.sqrt(i);
		}
		return new TileAbstraction(ary);
	}

	public static TileAbstraction reverseInverse(int length){
		double[] ary = new double[length];
		for(double i = length - 1; i >= 1; i--){
			ary[length - (int) i] = 1.0/ i;
		}
		return new TileAbstraction(ary);
	}

	public static TileAbstraction reverseInverseSquare(int length){
		double[] ary = new double[length];
		for(double i = length - 1; i >= 1; i--){
			ary[length - (int) i] = 1.0/ (i * i);
		}
		return new TileAbstraction(ary);
	}

	
	public static TileAbstraction makeUnit(int length){
		double[] ary = new double[length];
		for(int i =1; i < length; i++){
			ary[i] = 1;
		}
		return new TileAbstraction(ary);
	}
	
	/**
	 * 
	 * @param tile id of the tile whose cost to get
	 * @return the cost of moving the tile whose ID is id.
	 */
	public double getCost(int tile){
	    if(tile == 255)
	        return 0;
		return costs[tile];
	}
	
	public boolean isAbstracted(int id){
		return abstractedTiles.get(id);
	}
	
	public void abstractTile(int id){
		abstractedTiles.set(id);
	}
	
	public void refineTile(int id){
		abstractedTiles.clear(id);
	}
	
	public void setCost(int id, double cost){
		costs[id] = cost;
	}
	
	public TileAbstraction clone(){
		return new TileAbstraction(costs.clone());
	}
}
