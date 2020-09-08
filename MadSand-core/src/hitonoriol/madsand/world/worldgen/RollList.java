package hitonoriol.madsand.world.worldgen;

import java.util.ArrayList;

public class RollList {
	public int rollCount; // Total number of rolls to do from list
	public ArrayList<Integer> idList;	// List of ids to choose from
	
	public RollList(int rollCount, ArrayList<Integer> idList) {
		this.idList = idList;
		this.rollCount = rollCount;
	}
	
	public RollList() {
		this(0, null);
	}
}
