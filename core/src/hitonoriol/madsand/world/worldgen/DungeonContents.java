package hitonoriol.madsand.world.worldgen;

import java.util.ArrayList;

import hitonoriol.madsand.containers.rolltable.LootTable;
import hitonoriol.madsand.world.World;

public class DungeonContents {
	public int maxSize = World.DEFAULT_MAPSIZE;
	public int minSize = maxSize / 2;

	public int doorObject;

	public ArrayList<Integer> mobs;
	public int maxMobs;

	public LootTable loot;

	public int mobProbability;
	public int mobCorridorProbability;
	public int lootProbability;
	public int lootCorridorProbability;

	public ArrayList<Integer> specialRoomTiles;
	public ArrayList<Integer> specialRoomWalls;
	public ArrayList<Integer> specialMobs;
	public int specialMobsMax;
}
