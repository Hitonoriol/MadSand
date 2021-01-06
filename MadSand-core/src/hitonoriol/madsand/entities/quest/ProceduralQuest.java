package hitonoriol.madsand.entities.quest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.dialog.GameTextSubstitutor;
import hitonoriol.madsand.entities.Faction;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.enums.TradeCategory;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.NpcContainer;
import hitonoriol.madsand.properties.NpcProp;
import hitonoriol.madsand.properties.WorldGenProp;
import hitonoriol.madsand.world.World;
import hitonoriol.madsand.world.worldgen.OverworldPreset;

public class ProceduralQuest extends Quest {

	public static final int QUEST_TIMEOUT = 500;

	private final int EXP_MIN = 15, EXP_BASE_MAX = 40;

	private final float MIN_Q_FACTOR = 0.5f;
	private final int MAX_KILL_REQS = 3;
	private final int MAX_KILL_Q = 5;
	private final int MAX_HUNT_Q = 12;

	private final int MAX_RESOURCE_REQS = 3;
	private final int MAX_ITEM_Q = 10;

	public static ProceduralQuest timeoutQuest = new ProceduralQuest();

	public Type type;

	public ProceduralQuest(int id, long npcUID) {
		super(id);
		super.npcUID = npcUID;

		if (id != 0)
			generate();
	}

	public ProceduralQuest() {
		super();
	}

	public void generate() {
		type = Utils.randElement(Type.values);
		super.reqItems = super.reqKills = "";
		super.startMsg = super.reqMsg = getStartMsg();

		switch (type) {
		case Fetch:
			randomFetchQuest();
			break;
		case Kill:
			randomKillQuest();
			break;
		case Resource:
			randomResourceQuest();
			break;
		case Hunt:
			randomHuntQuest();
			break;
		case Craft:
			if (World.player.craftRecipes.isEmpty()) {
				generate();
				return;
			}
			randomCraftQuest();
			break;
		default:
			break;
		}

		addRewardItems();
		super.name = type.name() + " Quest";
		super.exp = rollRewardAmount();
	}

	private void addRewardItems() {
		super.rewardItems = "";
		List<Item> rewards = Globals.instance().proceduralQuestRewards.roll();
		for (Item item : rewards) {
			if (item.id == Globals.getInt(Globals.CURRENCY))
				item.quantity = (int) (rollRewardAmount() * 1.125);

			super.rewardItems += item.getString() + Item.BLOCK_DELIM;
		}
	}

	@JsonIgnore
	private double getLvlFactor() {
		double lvl = World.player.getLvl() + 3d;
		return (Math.sqrt(Utils.log(lvl, 3)) - 1d) / 10d;
	}

	private int rollRewardAmount() {
		return Utils.rand(EXP_MIN,
				(int) (EXP_BASE_MAX + (EXP_BASE_MAX * getLvlFactor())));
	}

	private int rollMaxObjective(int baseObjective) {
		return baseObjective + (int) (baseObjective * getLvlFactor());
	}

	protected void createEndMsg() {
		super.endMsg = "Thank you, " + Utils.subsName(GameTextSubstitutor.PLAYER_NAME) + "!";
		super.createEndMsg();
	}

	public long timeSinceCreated() {
		return MadSand.world.globalRealtimeTick - startTime;
	}

	/*
	 * Any procedural quest requirement generator:
	 * list - list of possible requirements
	 * maxRolls - max requirements to roll from list
	 * maxQuantity - max quantity of requirement
	 * name.get(int) - name getter function which must return requirement's name by
	 * its id
	 * 
	 * Returns quest requirementString
	 */
	private String randomQuest(List<Integer> list, int maxRolls, int maxQuantity) {
		String reqStr = "";

		HashSet<Integer> usedItems = new HashSet<>();
		int rolls = Utils.rand(1, maxRolls);
		int quantity;
		int item;

		for (int i = 0; i < rolls; ++i) {
			while (!usedItems.add(item = Utils.randElement(list)))
				if (usedItems.size() == list.size())
					break;

			quantity = Math.max(Utils.rand((int) (maxQuantity * MIN_Q_FACTOR), maxQuantity), 1);
			reqStr += item + Item.ITEM_DELIM + quantity + Item.BLOCK_DELIM;
		}
		return reqStr;
	}

	private void randomItemQuest(List<Integer> items) {
		String quest = randomQuest(items, MAX_RESOURCE_REQS, rollMaxObjective(MAX_ITEM_Q));
		super.reqItems += quest;
	}

	private void randomResourceQuest() {
		List<Item> items = NpcProp.tradeLists.roll(TradeCategory.Materials, 0);
		List<Integer> reqs = new ArrayList<>();

		for (Item item : items)
			if (!reqs.contains(item.id))
				reqs.add(item.id);

		randomItemQuest(reqs);
	}

	private void randomCraftQuest() {
		randomItemQuest(World.player.craftRecipes);
	}

	private void randomFetchQuest() {
		List<Integer> fetchItem = new ArrayList<>();
		fetchItem.add(Utils.randElement(Globals.instance().fetchQuestItems));
		Pair coords = MadSand.world.getCurLoc().randPlaceLoot(fetchItem.get(0));
		super.reqItems += randomQuest(fetchItem, 1, 1);
		super.startMsg += " I think the lost item is somewhere near (" + coords + ").";
	}

	private void randomHuntQuest() {
		super.reqItems += randomQuest(Globals.instance().huntQuestItems, 2, rollMaxObjective(MAX_HUNT_Q));
	}

	private void randomKillQuest() {
		OverworldPreset biome = WorldGenProp.getBiome(MadSand.world.getLocBiome()).overworld;
		ArrayList<Integer> mobs = new ArrayList<>();
		mobs.addAll(biome.friendlyMobs.idList);
		mobs.addAll(biome.hostileMobs.idList);

		Iterator<Integer> i = mobs.iterator();
		NpcContainer mob;
		Faction faction = getNpc().stats.faction;
		while (i.hasNext()) {
			mob = NpcProp.npcs.get(i.next());
			if (mob.faction == faction) // Npc can't request to kill npcs from his faction
				i.remove();
		}

		String quest = randomQuest(mobs, MAX_KILL_REQS, rollMaxObjective(MAX_KILL_Q));
		super.reqKills += quest;
	}

	private String getStartMsg() {
		return Utils.randElement(Globals.instance().proceduralQuestText.get(type));
	}

	public enum Type {
		Craft, // Request random craftable item
		Fetch, // Place random item somewhere in the world and ask player to bring it to NPC
		Resource,
		Kill, // kill quests
		Hunt;

		public static Type values[] = values();
	}

}
