package hitonoriol.madsand.world.worldgen;

import java.util.ArrayList;

public class OverworldPreset {
	public int defaultTile;
	public ArrayList<RollList> tiles = new ArrayList<>(); // {howManyRolls, <tileList>; ...}
	public ArrayList<RollList> objects = new ArrayList<>();
}
