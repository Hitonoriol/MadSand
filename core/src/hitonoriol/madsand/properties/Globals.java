package hitonoriol.madsand.properties;

import java.util.ArrayList;
import java.util.HashMap;

import hitonoriol.madsand.Resources;
import hitonoriol.madsand.entities.LootTable;
import hitonoriol.madsand.entities.ability.Ability;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.quest.ProceduralQuest;

public class Globals {
	public static final String VERSION = "Alpha v0.49.5-pre6";

	public static String TRAVEL_ITEM = "travelItem", TIMESKIP_ITEM = "timeSkipItem";

	private static Globals instance = new Globals();

	public int currencyId;

	public ArrayList<String> idleNpcText;
	public ArrayList<String> traderGreetings;

	public ArrayList<Integer> fetchQuestItems;
	public ArrayList<Integer> huntQuestItems;

	public LootTable proceduralQuestRewards;
	public HashMap<ProceduralQuest.Type, ArrayList<String>> proceduralQuestText;

	public HashMap<String, LootTable> lootTables;
	public HashMap<Integer, Ability> abilities;

	public int baseScrollId;
	public HashMap<String, String> scrolls;

	public int basePillId;
	public HashMap<String, String> pills = new HashMap<>();

	public HashMap<String, String> values;

	public static void loadGlobals() throws Exception {
		instance = Resources.mapper.readValue(Resources.readInternal(Resources.GLOBALS_FILE), Globals.class);
		instance.abilities.entrySet().stream().forEach(entry -> entry.getValue().id = entry.getKey());
		initPillScripts();
	}

	private static void initPillScripts() {
		HashMap<String, String> scripts = values().pills;
		values().abilities
				.forEach((id, ability) -> scripts.put(ability.name, "player:addAbility(" + id + ")"));
	}

	public static Globals values() {
		return instance;
	}

	public static int getInt(String name) {
		return Integer.parseInt(instance.values.get(name));
	}

	public static String getString(String name) {
		return instance.values.get(name);
	}

	public static Item getCurrency() {
		return ItemProp.getItem(instance.currencyId);
	}
}
