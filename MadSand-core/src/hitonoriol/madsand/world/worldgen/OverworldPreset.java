package hitonoriol.madsand.world.worldgen;

import java.util.ArrayList;

public class OverworldPreset {
	public int defaultTile;
	public ArrayList<RollList> tiles = new ArrayList<>(); // {howManyRolls, <tileList>; ...}
	public ArrayList<RollList> objects = new ArrayList<>();

	public ArrayList<RollList> regenerateObjects = new ArrayList<>();
	public double chanceToRegenerate;

	public RollList hostileMobs = new RollList();
	public RollList friendlyMobs = new RollList();
	
	public double hostileSpawnChance, friendlySpawnChance;
}
