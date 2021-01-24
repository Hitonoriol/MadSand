package hitonoriol.madsand.world.worldgen;

import java.util.ArrayList;

public class OverworldPreset {
	public int defaultTile;
	public ArrayList<RollList> tiles = new ArrayList<>(); // {howManyRolls, <tileList>; ...}
	public ArrayList<RollList> objects = new ArrayList<>();

	public ArrayList<RollList> regenerateObjects = new ArrayList<>();
	public double chanceToRegenerate;

	public float initialFriendlyChance = 0; // probability to roll friendly mob during initial spawn
	public int initialMobSpawn; // how many times to roll spawnlists on location generation
	public RollList hostileMobs = new RollList();
	public RollList friendlyMobs = new RollList();

	public double hostileSpawnChance, friendlySpawnChance;

	public ArrayList<String> structures = new ArrayList<>();
	public int minStructures = 0, maxStructures;
}
