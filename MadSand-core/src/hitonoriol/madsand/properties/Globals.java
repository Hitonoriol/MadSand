package hitonoriol.madsand.properties;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.entities.quest.ProceduralQuest;

public class Globals {
	public static String TRAVEL_ITEM = "travelItem";
	public static String CURRENCY_FIELD = "currencyId";

	private static Globals instance = new Globals();

	public ArrayList<String> traderGreetings;

	public ArrayList<Integer> fetchQuestItems;
	public HashMap<String, String> values;
	public ArrayList<String> idleNpcText;
	public HashMap<ProceduralQuest.Type, ArrayList<String>> proceduralQuestText;

	public static void loadGlobals() throws Exception {
		instance = Resources.mapper.readValue(new File(MadSand.GLOBALSFILE), Globals.class);
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
}
