package hitonoriol.madsand.world.worldgen;

import java.util.ArrayList;

import hitonoriol.madsand.world.Location.Layer;

public class DungeonPreset {
	public static final int EXIT_FLOOR_BASE = 2, EXIT_FLOOR_MAX = Layer.Dungeon.count();

	public int tolerance;
	public int maxRoomSize, minRoomSize;
	public int wallObject, wallTile; //Dungeon room wall objects & tiles
	public int roomTile;
	public int corridorTile;
	public int staircaseDownObject;
	public int staircaseUpObject;
	public int exitObject;

	public ArrayList<DungeonFloorContents> dungeonContents = new ArrayList<>(); // {floorGreaterOrEquals, <dungeonContents>; ...}
}
