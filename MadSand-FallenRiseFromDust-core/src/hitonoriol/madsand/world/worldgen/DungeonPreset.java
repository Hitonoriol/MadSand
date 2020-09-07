package hitonoriol.madsand.world.worldgen;

import java.util.ArrayList;

import com.sun.tools.javac.util.Pair;

public class DungeonPreset {
	public int tolerance;
	public int maxRoomSize, minRoomSize;
	public int wallObject, wallTile; //Dungeon room wall objects & tiles
	public int roomTile;
	public int corridorTile;
	public int doorObject;

	ArrayList<Pair<Integer, DungeonContents>> dungeonContents; // {floorLowerOrEquals, <dungeonContents>; ...}
}
