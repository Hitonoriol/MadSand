package hitonoriol.madsand.world;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.inventory.Inventory;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.enums.ItemType;
import hitonoriol.madsand.enums.Skill;
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
	public HashMap<WorkerType, WorkerContainer> workers = new HashMap<>();
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

	public WorkerContainer getWorkers(WorkerType type) {
		return workers.getOrDefault(type, new WorkerContainer());
	}

	public void addWorker(WorkerType type) {
		WorkerContainer allWorkers = getWorkers(type);
		++allWorkers.quantity;

		if (!workers.containsKey(type))
			workers.put(type, allWorkers);
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
		 */
		Map map = location.getOverworld();
		Pair objCoords;
		Skill workerSkill;
		WorkerContainer allWorkers;
		for (WorkerType type : WorkerType.values()) {
			workerSkill = type.getSkill();
			allWorkers = getWorkers(type);

			if (allWorkers.quantity == 0 || workerSkill == Skill.None)
				continue;

			objCoords = map.locateObject(type.getSkill());
			if (objCoords == Pair.nullPair)
				continue;

			if (allWorkers.gatherResources().itemCharge > 1f)
				warehouse.putItem(map.getObject(objCoords.x, objCoords.y).rollDrop(ItemType.bySkill(workerSkill)),
						allWorkers.getResourceQuantity());

		}
	}

	public static class WorkerContainer { // Info about all workers of certain type
		public static float ITEMS_PER_LVL = 0.15f; // Items per lvl per realtimeTick

		int lvl = 1;
		int quantity = 0;
		float itemCharge = 0;

		public WorkerContainer gatherResources() {
			itemCharge += (float) lvl * (float) quantity * ITEMS_PER_LVL;
			return this;
		}

		public int getResourceQuantity() {
			int quantity = (int) Math.floor(itemCharge);
			itemCharge -= quantity;
			return quantity;
		}
	}
}
