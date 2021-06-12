package hitonoriol.madsand.world;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.inventory.Inventory;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.inventory.item.Tool;
import hitonoriol.madsand.entities.skill.Skill;
import hitonoriol.madsand.enums.TradeCategory;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.object.ResourceObject;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.NpcProp;

public class Settlement {
	private static float WAREHOUSE_DEF_WEIGHT = 50;

	private float BASE_HIRE_COST = 12;
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
		Item upgradeCost = Item.create(Globals.values().currencyId, getSizeUpgradeCost());
		if (!warehouse.itemExists(upgradeCost))
			return false;

		Map overworld = location.getOverworld();
		warehouse.delItem(upgradeCost);
		overworld.setSize(overworld.getWidth() + SIZE_UPGRADE_BY, overworld.getHeight() + SIZE_UPGRADE_BY);

		return true;
	}

	@JsonIgnore
	public int getHireCost() {
		return (int) (Math.sqrt(getPopulation() + 1d) * BASE_HIRE_COST
				* (1d + Math.sqrt(MadSand.player().getEstablishedSettlements()) / 25d));
	}

	public boolean isOccupied(long npcUid) {
		return getOccupation(npcUid) != null;
	}

	public WorkerType getOccupation(long npcUid) {
		for (WorkerType type : WorkerType.workers)
			if (getWorkers(type).isOccupied(npcUid))
				return type;
		return null;
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

	private Pair objectCoords;

	private int skillWorkTick(Skill skill) {
		Map map = location.getOverworld();

		if (skill == Skill.Digging)
			return map.getTile(objectCoords = map.locateDiggableTile()).rollDrop(Tool.Type.Shovel);
		else if (skill.isResourceSkill())
			return map.getObject(objectCoords = map.locateObject(skill))
					.as(ResourceObject.class)
					.map(resourceObj -> resourceObj.rollDrop(Tool.Type.bySkill(skill)))
					.orElse(-1);

		return -1;

	}

	public void timeTick() {
		//Map map = location.getOverworld(); //TODO: Settlements in turn-based time + damage objects when gathering resources
		objectCoords = Pair.nullPair;
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
				producedItem = skillWorkTick(workerSkill);
				if (objectCoords == Pair.nullPair)
					continue;
			} else if (type == WorkerType.Sweeper)
				producedItem = NpcProp.tradeLists.rollId(TradeCategory.Trash);
			else if (type == WorkerType.Hunter)
				producedItem = NpcProp.tradeLists.rollId(TradeCategory.Food);

			if (workers.gatherResources().itemCharge > 1f)
				itemAdded = warehouse.putItem(producedItem, workers.getResourceQuantity());
			else
				continue;

			if (!itemAdded)
				break;
		}
	}

	public void randPopulate() {
		location.getOverworld()
				.pickNpcs(npc -> npc.stats.faction.isHuman())
				.filter(npc -> {
					if (leaderUid == -1) {
						leaderUid = npc.uid;
						return false;
					}
					return true;
				})
				.forEach(npc -> addWorker(WorkerType.roll(), npc.uid));
	}

	public WorkerType recruitWorker(long uid) {
		WorkerType type = WorkerType.roll();
		addWorker(type, uid);
		return type;
	}

	@JsonIgnore
	public int getPopulation() {
		int population = leaderUid > -1 ? 1 : 0;
		for (WorkerContainer worker : workers.values())
			population += worker.getQuantity();
		return population;
	}

	@JsonIgnore
	public void setPlayerOwned() {
		playerOwned = true;
	}

	@JsonIgnore
	public String getLeaderName() {
		return playerOwned ? MadSand.player().stats.name : MadSand.world.getCurLoc().getNpc(leaderUid).stats.name;
	}

	public static class WorkerContainer { // Info about all workers of certain type
		public static float ITEMS_PER_LVL = 0.025f; // Items per lvl per realtimeTick

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

	public static enum Status {
		PlayerOwned, NpcOwned, DoesNotExist, Unknown;

		public boolean exists() {
			return this == PlayerOwned || this == NpcOwned;
		}
	}
}
