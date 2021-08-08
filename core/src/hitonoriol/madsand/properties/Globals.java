package hitonoriol.madsand.properties;

import static hitonoriol.madsand.resources.Resources.GLOBALS_FILE;
import static hitonoriol.madsand.resources.Resources.getMapper;
import static hitonoriol.madsand.resources.Resources.loadList;
import static hitonoriol.madsand.resources.Resources.readInternal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

import hitonoriol.madsand.containers.rolltable.LootTable;
import hitonoriol.madsand.entities.ability.Ability;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.inventory.item.ScriptedConsumable.ScriptMap;
import hitonoriol.madsand.entities.quest.ProceduralQuest;

public class Globals {
	private static final String DEV_VER_STR = "[Development Build]";
	public static final String VERSION = getVersion();
	public static boolean HEADLESS = false;
	public static boolean debugMode = VERSION.equals(DEV_VER_STR), silentMode = false;

	private static Globals instance = new Globals();

	public int currencyId;
	public int travelItem, timeSkipItem;

	public ArrayList<String> tips;
	public ArrayList<String> idleNpcText;
	public ArrayList<String> traderGreetings;

	public ArrayList<Integer> fetchQuestItems;
	public ArrayList<Integer> huntQuestItems;

	public LootTable proceduralQuestRewards;
	public HashMap<ProceduralQuest.Type, ArrayList<String>> proceduralQuestText;

	public HashMap<String, LootTable> lootTables;
	public HashMap<Integer, Ability> abilities;

	public HashMap<String, ScriptMap> scriptMaps;

	public static boolean headless() {
		return HEADLESS;
	}

	public static boolean isDevBuild() {
		return VERSION == DEV_VER_STR;
	}

	public static void debug(Runnable action) {
		if (isDevBuild())
			action.run();
	}

	private static String getVersion() {
		String version = Globals.class.getPackage().getImplementationVersion();

		if (version == null) {
			version = DEV_VER_STR;
			debugMode = true;
		}

		return version;
	}

	public static void loadGlobals() throws Exception {
		instance = getMapper().readValue(readInternal(GLOBALS_FILE), Globals.class);
		instance.abilities.entrySet().stream().forEach(entry -> entry.getValue().id = entry.getKey());
	}

	public void loadMisc() {
		tips = loadList("tips.json", String.class);

		createScriptMap("pills", map -> {
			abilities.forEach((id, ability) -> map.put(ability.name, "player:addAbility(" + id + ")"));
		});
		createScriptMap("craft_recipes", map -> {
			ItemProp.craftReq.keySet()
					.forEach(id -> map.put(ItemProp.getItemName(id), "player:unlockCraftRecipe(" + id + ")"));
		});
	}

	private void createScriptMap(String name, Consumer<ScriptMap> populator) {
		ScriptMap map = new ScriptMap();
		scriptMaps.put(name, map);
		populator.accept(map);
	}

	public static Globals values() {
		return instance;
	}

	public static Item getCurrency() {
		return ItemProp.getItem(instance.currencyId);
	}
}
