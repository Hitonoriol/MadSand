package hitonoriol.madsand.world;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.entities.inventory.Inventory;
import hitonoriol.madsand.enums.WorkerType;

public class Settlement {
	private static float DEF_MAX_WEIGHT = 50;

	public Inventory warehouse = new Inventory(DEF_MAX_WEIGHT);
	public HashMap<WorkerType, Integer> workers = new HashMap<>();
	private Location location;

	@JsonIgnore
	public void setLocation(Location location) {
		this.location = location;
	}

	public void timeTick() {
		/*
		 * TODO(?)
		 * 
		 * Gather materials / manufacture items:
		 * (?) Locate objects with needed resources and damage them
		 * the same way as during the real resource gathering.
		 * 
		 * Simulate Ageing of workers (?) -- using spawnTime
		 */
	}
}
