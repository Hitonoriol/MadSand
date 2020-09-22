package hitonoriol.madsand.world.worldgen;

import java.util.ArrayList;

import hitonoriol.madsand.world.World;

public class DungeonContents {
	public int maxSize = World.DEFAULT_MAPSIZE;
	public int minSize = maxSize / 2;

	public ArrayList<Integer> mobs;
	public int maxMobs;
	
	public ArrayList<String> loot;

	public int mobProbability;
	public int mobCorridorProbability;
	public int lootProbability;
	public int lootCorridorProbability;

	public ArrayList<Integer> specialMobs;
	public int specialMobsMax;
}
