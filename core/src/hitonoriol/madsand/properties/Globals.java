package hitonoriol.madsand.properties;

import java.util.ArrayList;
import java.util.HashMap;

import hitonoriol.madsand.Resources;
import hitonoriol.madsand.containers.rolltable.LootTable;
import hitonoriol.madsand.entities.ability.Ability;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.quest.ProceduralQuest;

public class Globals {
	private static final String DEV_VER_STR = "[Development Build]";
	public static final String VERSION = getVersion();
	public static boolean debugMode = VERSION.equals(DEV_VER_STR), silentMode = false;

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

	private static String getVersion() {
		String version = Globals.class.getPackage().getImplementationVersion();

		if (version == null) {
			version = DEV_VER_STR;
			debugMode = true;
		}

		return version;
	}

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
