package hitonoriol.madsand.entities.quest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.dialog.DialogChainGenerator;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.enums.Faction;
import hitonoriol.madsand.enums.TradeCategory;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.properties.NpcContainer;
import hitonoriol.madsand.properties.NpcProp;
import hitonoriol.madsand.properties.WorldGenProp;
import hitonoriol.madsand.world.World;
import hitonoriol.madsand.world.worldgen.OverworldPreset;

public class ProceduralQuest extends Quest {

	public static final int QUEST_TIMEOUT = 500;

	private final int EXP_MIN = 10;

	private final int MAX_KILL_REQS = 3;
	private final int MAX_KILL_Q = 5;

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
		type = Type.values()[Utils.rand(Type.values().length)];
		super.reqItems = super.reqKills = "";
		super.startMsg = super.endMsg = "#" + getNpc().stats.name + "#";
		super.startMsg += getStartMsg();

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

		super.name = type.name() + " Quest";
		super.exp = Utils.rand(EXP_MIN, World.player.stats.skills.getExp() / 10);
		super.reqMsg = super.startMsg;
		createEndMsg();
	}

	private void createEndMsg() {
		super.endMsg += "Thank you, {PLAYER}!" + Resources.LINEBREAK + Resources.LINEBREAK;
		super.endMsg += DialogChainGenerator.LBRACKET + "+" + super.exp + " EXP" + DialogChainGenerator.RBRACKET
				+ Resources.LINEBREAK;
		if (!super.rewardItems.equals(Item.EMPTY_ITEM))
			super.endMsg += DialogChainGenerator.LBRACKET + "+" + Item.queryToName(super.rewardItems)
					+ DialogChainGenerator.RBRACKET;
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
	 * Returns quest startMessage and requirementString in HashMap
	 */
	private HashMap<Property, String> randomQuest(List<Integer> list, int maxRolls, int maxQuantity, NameGetter name) {
		HashMap<Property, String> ret = new HashMap<>();
		String startMsg = "";
		String reqStr = "";

		HashSet<Integer> usedItems = new HashSet<>();
		int rolls = Utils.rand(1, maxRolls);
		int quantity;
		int item;

		for (int i = 0; i < rolls; ++i) {

			while (!usedItems.add(item = Utils.randElement(list)))
				if (usedItems.size() == list.size())
					break;

			quantity = Utils.rand(1, maxQuantity);
			startMsg += quantity + " " + name.get(item);

			if (i == rolls - 2)
				startMsg += " and ";
			else if (i < rolls - 1)
				startMsg += ", ";
			else
				startMsg += " ";

			reqStr += item + Item.ITEM_DELIM + quantity + Item.BLOCK_DELIM;
		}
		ret.put(Property.StartMessage, startMsg);
		ret.put(Property.RequirementString, reqStr);
		return ret;
	}

	private void randomItemQuest(List<Integer> items) {
		HashMap<Property, String> quest = randomQuest(items, MAX_RESOURCE_REQS, MAX_ITEM_Q,
				(int id) -> ItemProp.getItemName(id));
		super.reqItems += quest.get(Property.RequirementString);
		super.startMsg += quest.get(Property.StartMessage);
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
		HashMap<Property, String> quest = randomQuest(fetchItem, 1, 1, (int id) -> ItemProp.getItemName(id));
		Pair coords = MadSand.world.getCurLoc().randPlaceLoot(fetchItem.get(0));
		super.reqItems += quest.get(Property.RequirementString);
		super.startMsg += quest.get(Property.StartMessage).trim() + ". I think it's somewhere near (" + coords + ").";
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

		HashMap<Property, String> quest = randomQuest(mobs, MAX_KILL_REQS, MAX_KILL_Q,
				(int id) -> NpcProp.npcs.get(id).name);
		super.reqKills += quest.get(Property.RequirementString);
		super.startMsg += quest.get(Property.StartMessage);
	}

	private String getStartMsg() {
		return Utils.randElement(Globals.instance().proceduralQuestText.get(type));
	}

	private interface NameGetter {
		String get(int id);
	}

	private enum Property {
		StartMessage, RequirementString
	}

	public enum Type {
		Craft, // Request random craftable item
		Fetch, // Place random item somewhere in the world and ask player to bring it to NPC
		Resource,
		Kill // kill quests
	}

}
