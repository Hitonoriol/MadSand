package hitonoriol.madsand.properties;

import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.Tile;

public class TileProp {
	public static java.util.Map<Integer, Tile> tiles;

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