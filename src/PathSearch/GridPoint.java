package PathSearch;

import java.awt.Point;

public class GridPoint extends Point{
	private static final long serialVersionUID = -657474009882885518L;

	GridPoint parent;
	int movementCost;
	int heuristicCost;
	int totalCost;
	
	public GridPoint(int x,int y) {
		super(x,y);
		movementCost = 0;
		heuristicCost = 0;
		totalCost = 0;
	}
	
	public void setParent(GridPoint parent) {
		this.parent = parent;
	}
	public GridPoint getParent() {
		return parent;
	}
	
	public void setMovementCost(int cost) {
		this.movementCost = cost;
	}
	
	public int getMovementCost() {
		return movementCost;
	}
	
	public void setHeuristicCost(int cost) {
		this.heuristicCost = cost;
	}
	
	public int getHeuristicCost() {
		return heuristicCost;
	}
	
	public void setTotalCost(int cost) {
		this.totalCost = cost;
	}
	
	public int getTotalCost() {
		return totalCost;
	}
}