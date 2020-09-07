package hitonoriol.madsand.properties;

import java.io.File;
import java.util.HashMap;

import com.fasterxml.jackson.databind.type.MapType;

import hitonoriol.madsand.MadSand;

public class Globals {
	public static String CURRENCY_FIELD = "currencyId";

	private static HashMap<String, String> values = new HashMap<>();

	public static void loadGlobals() throws Exception {
		MapType globalMap = MadSand.typeFactory.constructMapType(HashMap.class, String.class, String.class);
		values = MadSand.mapper.readValue(new File(MadSand.GLOBALSFILE), globalMap);
	}

	public static int getInt(String name) {
		return Integer.parseInt(values.get(name));
	}
	
	public static String getString(String name) {
		return values.get(name);
	}
}
