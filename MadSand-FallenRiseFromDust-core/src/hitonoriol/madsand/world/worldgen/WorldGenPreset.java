package hitonoriol.madsand.world.worldgen;

import java.util.ArrayList;

import com.sun.tools.javac.util.Pair;

public class WorldGenPreset {
	// Biome tile & object sets
	public int defaultTile;
	public ArrayList<Pair<Integer, ArrayList<Integer>>> tiles; // {howManyRolls, <tileList>; ...}
	public ArrayList<Pair<Integer, ArrayList<Integer>>> objects;

	// Lake generator params & lake tiles
	public int lakeTile;
	public double lakeModifier, lakeFrom, lakeTo;

	// default cave tile & object
	public int caveTile, caveObject;

	//Ore ids, max ore vein size, vein count
	public ArrayList<Integer> caveOre;
	public int maxVeinSize, maxVeinCount;
	
	//Dungeon params
	public int dungeonProbability; // Probability to generate dungeon instead of normal cave
	public DungeonPreset dungeon;
	
}
