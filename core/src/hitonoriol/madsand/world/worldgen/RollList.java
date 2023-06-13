package hitonoriol.madsand.world.worldgen;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class RollList {
	public int rollCount; // Total number of rolls to do from list
	public ArrayList<Integer> idList; // List of ids to choose from
	public boolean scalable;

	public RollList(int rollCount, ArrayList<Integer> idList) {
		this.idList = idList;
		this.rollCount = rollCount;
		scalable = false;
	}

	@JsonIgnore
	public int getRollCount(float scaleFactor) {
		return scalable ? (int) (rollCount * scaleFactor) : rollCount;
	}

	public RollList() {
		this(0, null);
		scalable = true;
	}
}
