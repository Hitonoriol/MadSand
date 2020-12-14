package hitonoriol.madsand.world;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.entities.inventory.Inventory;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.enums.WorkerType;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.properties.Globals;

public class Settlement {
	private static float WAREHOUSE_DEF_WEIGHT = 50;

	private int SIZE_UPGRADE_COST = 500;
	private int SIZE_UPGRADE_BY = 25;

	public boolean playerOwned = false;
	public long leaderUid = -1; // If the settlement is not player-owned, it must have an NPC leader
	public int lvl = 1;
	public Inventory warehouse = new Inventory(WAREHOUSE_DEF_WEIGHT);
	public HashMap<WorkerType, Integer> workers = new HashMap<>();
	private Location location;

	@JsonIgnore
	public void setLocation(Location location) {
		this.location = location;
	}

	public boolean upgradeSize() {
		Item upgradeCost = new Item(Globals.getInt(Globals.CURRENCY_FIELD), getSizeUpgradeCost());
		if (!warehouse.itemExists(upgradeCost))
			return false;

		Map overworld = location.getOverworld();
		warehouse.delItem(upgradeCost);
		overworld.setSize(overworld.getWidth() + SIZE_UPGRADE_BY, overworld.getHeight() + SIZE_UPGRADE_BY);

		return true;
	}

	public int getSizeUpgradeCost() {
		return lvl * SIZE_UPGRADE_COST;
	}

	@JsonIgnore
	public Location getLocation() {
		return location;
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
