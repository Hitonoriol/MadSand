package hitonoriol.madsand.properties;

import java.util.HashMap;

import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.Tile;

public class TileProp {
	public static HashMap<Integer, Tile> tiles = new HashMap<>();
	
	public final static Tile getTileProp(int id) {
		return tiles.getOrDefault(id, Map.nullTile);
	}
	
	public static String getOnInteract(int id) {
		return getTileProp(id).onInteract;
	}
	
	public static String getName(int id) {
		return getTileProp(id).name;
	}
}