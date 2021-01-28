package hitonoriol.madsand.properties;

import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonSetter;

import hitonoriol.madsand.Resources;
import hitonoriol.madsand.entities.LootTable;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.entities.quest.ProceduralQuest;

public class Globals {
	public static final String VERSION = "Alpha v0.49.4";

	public static String TRAVEL_ITEM = "travelItem", TIMESKIP_ITEM = "timeSkipItem";
	public static String CURRENCY = "currencyId";

	private static Globals instance = new Globals();

	public ArrayList<String> traderGreetings;
	public ArrayList<Integer> fetchQuestItems;
	public ArrayList<Integer> huntQuestItems;
	public ArrayList<String> idleNpcText;
	public LootTable proceduralQuestRewards;
	public HashMap<ProceduralQuest.Type, ArrayList<String>> proceduralQuestText;

	public HashMap<String, String> values;

	public static void loadGlobals() throws Exception {
		instance = Resources.mapper.readValue(Resources.readInternal(Resources.GLOBALS_FILE), Globals.class);
	}

	public static Globals instance() {
		return instance;
	}

	public static int getInt(String name) {
		return Integer.parseInt(instance.values.get(name));
	}

	public static String getString(String name) {
		return instance.values.get(name);
	}

	public static Item getCurrency() {
		return ItemProp.getItem(getInt(CURRENCY));
	}

	@JsonSetter("proceduralQuestRewards")
	public void setProceduralQuestRewards(String loot) {
		if (loot != null)
			this.proceduralQuestRewards = LootTable.parse(loot);
	}
}
