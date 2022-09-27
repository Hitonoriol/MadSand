package hitonoriol.madsand.gamecontent;

import static hitonoriol.madsand.resources.Resources.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

import hitonoriol.madsand.containers.rolltable.LootTable;
import hitonoriol.madsand.entities.ability.Ability;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.inventory.item.ScriptedConsumable.ScriptMap;
import hitonoriol.madsand.entities.quest.ProceduralQuest;
import hitonoriol.madsand.resources.GameAssetManager;
import hitonoriol.madsand.resources.loaders.JsonLoader;

public class Globals implements Loadable {
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

	private void createScriptMap(String name, Consumer<ScriptMap> populator) {
		ScriptMap map = new ScriptMap();
		scriptMaps.put(name, map);
		populator.accept(map);
	}

	public static Globals values() {
		return instance;
	}

	public static Item getCurrency() {
		return Items.all().get(instance.currencyId);
	}

	@Override
	public void registerLoader(GameAssetManager manager) {
		manager.setLoader(Globals.class, new JsonLoader<>(manager, Globals.class) {
			@Override
			protected void load(Globals globals) {
				instance = globals;
				globals.abilities.entrySet().stream().forEach(entry -> entry.getValue().id = entry.getKey());
				globals.tips = loader().loadList("tips.json", String.class);
				globals.createScriptMap("pills", map -> {
					globals.abilities.forEach((id, ability) -> map.put(ability.name, "player:addAbility(" + id + ")"));
				});

				Items.deferInit(() -> {
					globals.createScriptMap("craft_recipes", map -> {
						Items.all().craftRequirements().keySet()
								.forEach(
										id -> map.put(Items.all().getName(id), "player:unlockCraftRecipe(" + id + ")"));
					});
				});
			}
		});
	}
}
