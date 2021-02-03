package hitonoriol.madsand.properties;

import java.util.HashMap;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.ItemProducer;
import hitonoriol.madsand.map.object.MapObject;

public class ObjectProp {
	public static HashMap<Integer, MapObject> objects = new HashMap<>();
	public static HashMap<Integer, ItemProducer> productionStations = new HashMap<>();
	public static HashMap<Integer, String> buildRecipes = new HashMap<>();

	public final static MapObject getObject(int id) {
		return objects.getOrDefault(id, Map.nullObject);
	}
	
	public static String getOnInteract(int id) {
		return getObject(id).onInteract;
	}
	
	public static String getName(int id) {
		return getObject(id).name;
	}
}
