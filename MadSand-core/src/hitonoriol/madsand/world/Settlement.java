package hitonoriol.madsand.world;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.inventory.Inventory;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.enums.ItemType;
import hitonoriol.madsand.enums.Skill;
import hitonoriol.madsand.enums.TradeCategory;
import hitonoriol.madsand.enums.WorkerType;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.NpcProp;

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
		if (this.location == null)
			this.location = location;
	}

	public boolean upgradeSize() {
		Item upgradeCost = new Item(Globals.getInt(Globals.CURRENCY), getSizeUpgradeCost());
		if (!warehouse.itemExists(upgradeCost))
			return false;

		Map overworld = location.getOverworld();
		warehouse.delItem(upgradeCost);
		overworld.setSize(overworld.getWidth() + SIZE_UPGRADE_BY, overworld.getHeight() + SIZE_UPGRADE_BY);

		return true;
	}

	public boolean isOccupied(long npcUid) {
		for (WorkerType type : WorkerType.workers)
			if (getWorkers(type).isOccupied(npcUid))
				return true;
		return false;
	}

	public WorkerContainer getWorkers(WorkerType type) {
		return workers.getOrDefault(type, new WorkerContainer());
	}

	public void addWorker(WorkerType type, long uid) {
		WorkerContainer allWorkers = getWorkers(type);
		allWorkers.add(uid);

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
		Map map = location.getOverworld();
		Pair objCoords;
		Skill workerSkill;
		WorkerContainer workers;
		boolean itemAdded;
		int producedItem;
		for (WorkerType type : WorkerType.values()) {
			workerSkill = type.getSkill();
			workers = getWorkers(type);
			producedItem = 0;

			if (workers.getQuantity() == 0)
				continue;

			if (workerSkill != Skill.None) {
				objCoords = map.locateObject(type.getSkill());
				if (objCoords == Pair.nullPair)
					continue;
				producedItem = map.getObject(objCoords.x, objCoords.y).rollDrop(ItemType.bySkill(workerSkill));
			} else if (type == WorkerType.Sweeper)
				producedItem = NpcProp.tradeLists.rollId(TradeCategory.Trash);

			if (workers.gatherResources().itemCharge > 1f)
				itemAdded = warehouse.putItem(producedItem, workers.getResourceQuantity());
			else
				continue;

			if (!itemAdded)
				break;
		}
	}

	public void randPopulate() {	// TODO
		for (WorkerType type : WorkerType.values())
			addWorker(type, 0);
	}

	public WorkerType recruitWorker(long uid) {
		WorkerType type = WorkerType.roll();
		addWorker(type, uid);
		return type;
	}

	@JsonIgnore
	public void setPlayerOwned() {
		playerOwned = true;
	}

	@JsonIgnore
	public String getLeaderName() {
		return playerOwned ? World.player.stats.name : MadSand.world.getCurLoc().getNpc(leaderUid).stats.name;
	}

	public static class WorkerContainer { // Info about all workers of certain type
		public static float ITEMS_PER_LVL = 0.15f; // Items per lvl per realtimeTick

		public int lvl = 1;
		public float itemCharge = 0;
		public Set<Long> npcs = new HashSet<>();

		public void add(long npcUid) {
			npcs.add(npcUid);
		}

		public void remove(long npcUid) {
			npcs.remove(npcUid);
		}

		public boolean isOccupied(long npcUid) {
			return npcs.contains(npcUid);
		}

		@JsonIgnore
		public int getQuantity() {
			return npcs.size();
		}

		public WorkerContainer gatherResources() {
			itemCharge += getGatheringRate();
			return this;
		}

		@JsonIgnore
		public float getGatheringRate() {
			return (float) lvl * (float) getQuantity() * ITEMS_PER_LVL;
		}

		@JsonIgnore
		public int getResourceQuantity() {
			int quantity = (int) Math.floor(itemCharge);
			itemCharge -= quantity;
			return quantity;
		}
	}
}
