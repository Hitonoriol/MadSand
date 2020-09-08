package hitonoriol.madsand.world.worldgen;

import java.util.ArrayList;

import com.github.czyzby.noise4j.map.Grid;
import com.github.czyzby.noise4j.map.generator.noise.NoiseGenerator;
import com.github.czyzby.noise4j.map.generator.room.dungeon.DungeonGenerator;
import com.github.czyzby.noise4j.map.generator.util.Generators;

import hitonoriol.madsand.Utils;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.properties.WorldGenProp;
import hitonoriol.madsand.world.Location;
import hitonoriol.madsand.world.MapID;
import hitonoriol.madsand.world.World;

public class WorldGen {
	private Location worldMap;
	private WorldGenPreset curBiome;
	private MapID curMapId;
	private Map curLoc;

	public WorldGen(Location worldMap) {
		this.worldMap = worldMap;
	}

	public void generate(MapID mapId) {
		this.curMapId = mapId;
		this.curLoc = worldMap.get(mapId);

		int caveDepth = World.LAYER_BASE_UNDERWORLD;
		int layer = mapId.layer;
		int wx = mapId.worldxy.x, wy = mapId.worldxy.y;
		int biome;

		if (layer == World.LAYER_OVERWORLD) {

			if ((wx == 5) && (wy == 5)) // TODO: persistent locations & move this shit somewhere
				biome = 0;
			else
				biome = chooseRandomBiome();

			Utils.out("Biome: " + biome);
			curLoc.setBiome(biome);

			curBiome = WorldGenProp.getBiome(biome);
			genBiomeTerrain(); //TODO pass map to these things?
			genLakes();
			genBiomeObjects();
			genCave(caveDepth);
		} else
			caveDepth = layer;

		genDungeon(caveDepth);
	}

	private void genBiomeTerrain() {
		int defaultTile = curBiome.getDefaultTile();
		curLoc.defTile = defaultTile;
		curLoc.fillTile();
		Utils.out("Default tile: " + defaultTile);

		ArrayList<RollList> tileGenList = curBiome.getBiomeTiles();

		ArrayList<Integer> tileIdList;
		int listSize;

		for (RollList tileRollList : tileGenList) {
			for (int i = 0; i < tileRollList.rollCount; ++i) {
				tileIdList = tileRollList.idList;
				listSize = tileIdList.size();
				curLoc.randPlaceTile(tileIdList.get(Utils.random.nextInt(listSize)));
			}
		}
	}

	private void genBiomeObjects() {
		Utils.out("Generating biome objects!");
		ArrayList<RollList> objectGenList = curBiome.getBiomeObjects();

		ArrayList<Integer> objectIdList;
		int listSize;

		for (RollList objectRollList : objectGenList) {
			for (int i = 0; i < objectRollList.rollCount; ++i) {
				objectIdList = objectRollList.idList;
				listSize = objectIdList.size();
				curLoc.randPlaceObject(objectIdList.get(Utils.random.nextInt(listSize)));
			}
		}

		Utils.out("Done generating biome objects!");
	}

	private void genLakes() {
		Utils.out("Generating lakes!");
		Map curLoc = worldMap.get(curMapId);
		int w = curLoc.getWidth(), h = curLoc.getHeight();
		LakePreset lake = curBiome.getBiomeLake();

		if (lake.lakeTile <= 0)
			return;

		final Grid grid = new Grid(w, h);
		final NoiseGenerator noiseGenerator = new NoiseGenerator();

		noiseGenerator.setRadius(lake.lakeRadius);
		noiseGenerator.setModifier(lake.lakeModifier);
		noiseGenerator.setSeed(Generators.rollSeed());
		noiseGenerator.generate(grid);

		int i = 0;
		int ii = 0;
		float from = ((float) lake.lakeFrom) / 10f;
		float to = ((float) lake.lakeTo) / 100f;

		Utils.out("lakes from: " + from + " to: " + to);

		while (i < w) {
			while (ii < h) {

				if (grid.get(ii, i) >= from && grid.get(ii, i) <= to)
					curLoc.addTile(ii, i, lake.lakeTile);

				ii++;
			}
			i++;
			ii = 0;
		}

		Utils.out("Done generating lakes!");
	}

	private void genCave(int layer) {
		Utils.out("Generating underworld...");
		MapID caveId = new MapID(curMapId).setLayer(layer);
		CavePreset cave = curBiome.getBiomeCave();
		ArrayList<Integer> oreList = cave.caveOre;
		int oreListSize = oreList.size();

		int maxOreFieldSize = cave.maxVeinSize;
		int count = cave.maxVeinCount;
		int cdef = cave.caveTile;
		Map loc = worldMap.get(caveId);
		loc.purge();
		loc.fillTile(cdef);
		loc.defObject = cave.caveObject;
		loc.defTile = cdef;
		loc.fillObject();

		int a = Utils.rand(1, count);
		int locWidth = loc.getWidth(), locHeight = loc.getHeight();

		while (a > 0) {
			int x = Utils.random.nextInt(locWidth);
			int y = Utils.random.nextInt(locHeight);
			int w = Utils.random.nextInt(maxOreFieldSize) + 1;
			int h = Utils.random.nextInt(maxOreFieldSize) + 1;

			int id = oreList.get(Utils.random.nextInt(oreListSize));
			int k = 0;
			int kk = 0;
			while (kk < w) {
				while (k < h) {
					loc.addObject(x + kk, y + k, id);
					k++;
				}
				k = 0;
				kk++;
			}

			a--;
		}
		Utils.out("Done generating underworld!");
	}

	private float DUNGEON_CORRIDOR_LEVEL = 0.0f;
	private float DUNGEON_WALL_LEVEL = 1.0f;
	private float DUNGEON_ROOM_LEVEL = 0.5f;

	private void genDungeon(int layer) {
		boolean force = true;
		MapID dungeonId = new MapID(curMapId).setLayer(layer);
		Map curLoc = worldMap.get(dungeonId);

		Utils.out("Generating dungeon!");
		DungeonPreset dungeon = curBiome.getBiomeDungeon();
		ArrayList<DungeonFloorContents> contents = dungeon.dungeonContents;

		int prob = curBiome.dungeonProbability;
		Utils.out("Probability: " + prob + "%");

		if (prob == 0)
			return;

		if (layer == World.LAYER_BASE_UNDERWORLD && Utils.randPercent() > prob) {
			Utils.out("Well... Decided not to.");
			return;
		}

		int floorNumber = layer;
		for (DungeonFloorContents floor : contents) {
			if (floor.fromFloor < floorNumber)
				floorNumber = floor.fromFloor;
		}

		DungeonContents curDungeonFloor = contents.get(floorNumber).contents;

		ArrayList<Integer> mobs = curDungeonFloor.mobs;
		ArrayList<String> loot = curDungeonFloor.loot;
		int mobProb = curDungeonFloor.mobProbability;
		int mobCorridorProb = curDungeonFloor.mobCorridorProbability;
		int lootProb = curDungeonFloor.lootProbability; //TODO
		int lootCorridorProb = curDungeonFloor.lootCorridorProbability;

		int w = curLoc.getWidth(), h = curLoc.getHeight();
		final Grid grid = new Grid(w, h);
		final DungeonGenerator dungeonGenerator = new DungeonGenerator();

		dungeonGenerator.setRoomGenerationAttempts(World.MAPSIZE);
		dungeonGenerator.setMaxRoomSize(dungeon.maxRoomSize);
		dungeonGenerator.setTolerance(dungeon.tolerance); // Max difference between width and height.
		dungeonGenerator.setMinRoomSize(dungeon.minRoomSize);
		dungeonGenerator.generate(grid);

		int wallObj = dungeon.wallObject;
		int wallTile = dungeon.wallTile;
		int roomTile = dungeon.roomTile;
		int corridorTile = dungeon.corridorTile;
		int door = dungeon.doorObject;

		int y = 0, x = 0;
		curLoc.editable = false;

		while (y < World.MAPSIZE) {
			while (x < World.MAPSIZE) {
				if (grid.get(x, y) == DUNGEON_CORRIDOR_LEVEL) { // corridors
					curLoc.delObject(x, y);
					curLoc.addTile(x, y, corridorTile, force);
				}

				if (grid.get(x, y) == DUNGEON_WALL_LEVEL) { // walls
					curLoc.addObject(x, y, wallObj);
					curLoc.addTile(x, y, wallTile);
				}

				if (grid.get(x, y) == DUNGEON_ROOM_LEVEL) { // rooms
					curLoc.delObject(x, y);
					curLoc.addTile(x, y, roomTile, force);

					if (Utils.percentRoll(mobProb))
						curLoc.spawnNpc(Utils.randElement(mobs), x, y);

					if (curLoc.spawnPoint.equals(Pair.nullPair))
						curLoc.spawnPoint = new Pair(x, y);
				}

				if (isDoorway(grid, x, y, w, h)) // door
					curLoc.addObject(x, y, door);

				x++;
			}
			x = 0;
			y++;
		}

		Utils.out("Done generating dungeon!");
	}

	private boolean isDoorway(Grid grid, int x, int y, int xsz, int ysz) {
		float up, down, left, right;
		up = down = left = right = -1;
		float current = grid.get(x, y);

		if (y + 1 < ysz)
			up = grid.get(x, y + 1);
		if (y - 1 >= 0)
			down = grid.get(x, y - 1);
		if (x - 1 >= 0)
			left = grid.get(x - 1, y);
		if (x + 1 < xsz)
			right = grid.get(x + 1, y);

		return (current == DUNGEON_CORRIDOR_LEVEL) && (up == DUNGEON_ROOM_LEVEL || down == DUNGEON_ROOM_LEVEL
				|| left == DUNGEON_ROOM_LEVEL || right == DUNGEON_ROOM_LEVEL);
	}

	public int chooseRandomBiome() {
		ArrayList<Integer> keyList = new ArrayList<Integer>(WorldGenProp.biomes.keySet());
		return keyList.get(Utils.random.nextInt(keyList.size()));
	}
}
