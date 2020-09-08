package hitonoriol.madsand.world.worldgen;

import java.util.ArrayList;

public class DungeonPreset {
	public int tolerance;
	public int maxRoomSize, minRoomSize;
	public int wallObject, wallTile; //Dungeon room wall objects & tiles
	public int roomTile;
	public int corridorTile;
	public int doorObject;

	public ArrayList<DungeonFloorContents> dungeonContents = new ArrayList<>(); // {floorGreaterOrEquals, <dungeonContents>; ...}
}
