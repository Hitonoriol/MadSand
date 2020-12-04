package hitonoriol.madsand.world.worldgen;

import java.util.ArrayList;

import com.github.czyzby.noise4j.map.Grid;
import com.github.czyzby.noise4j.map.generator.noise.NoiseGenerator;
import com.github.czyzby.noise4j.map.generator.util.Generators;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.properties.WorldGenProp;
import hitonoriol.madsand.world.WorldMap;
import hitonoriol.madsand.world.Location;

public class WorldGen {
	private WorldMap worldMap;
	private WorldGenPreset curBiome;
	private int curBiomeId;
	private Pair curMapCoords;
	private int curLayer;
	private Location curLocation;
	private Map curMap;
	private int width, height;
	private boolean friendlyOnly = false;

	public WorldGen(WorldMap worldMap) {
		this.worldMap = worldMap;
		reset();
	}

	public WorldGen initPosition(Pair coords, int layer) {
		curMapCoords = coords;
		curLayer = layer;
		return this;
	}

	public WorldGen initPosition() {
		return initPosition(worldMap.curWorldPos, worldMap.curLayer);
	}

	public WorldGen friendlyOnly() {
		friendlyOnly = true;
		return this;
	}

	public WorldGen setBiome(int biome) {
		this.curBiomeId = biome;
		this.curBiome = WorldGenProp.getBiome(biome);
		return this;
	}

	public WorldGen setSize(int w, int h) {
		this.width = w;
		this.height = h;
		return this;
	}

	public void reset() {
		this.setSize(-1, -1).setBiome(-1);
		friendlyOnly = false;
	}

	public void generate() {
		curLocation = worldMap.getLocation(curMapCoords);
		this.curMap = curLocation.getLayer(curLayer);

		Utils.out("Generating " + curMapCoords + " : " + curLayer);

		if (width < 1 || height < 1)
			curMap.rollSize();
		else
			curMap.setSize(width, height);

		curMap.purge();

		if (curLayer == Location.LAYER_OVERWORLD) {

			if (curBiome.equals(WorldGenProp.nullPreset))
				setBiome(chooseRandomBiome());

			Utils.out("Biome: " + curBiomeId);
			curLocation.biome = curBiomeId;

			setBiome(curBiomeId);

			genBiomeTerrain();
			genLakes();
			genBiomeObjects();
			genBiomeStructures();
			rollDungeon();
			initialMobSpawn();

		} else {

			if (curLayer <= Location.LAYER_MAX_DUNGEON)
				genDungeon();
			else
				genCave();

		}

		reset();
	}

	private void genBiomeStructures() {
		ArrayList<String> structures = curBiome.overworld.structures;

		if (structures.isEmpty())
			return;

		int rolls = Utils.rand(curBiome.overworld.maxStructures + 1);
		for (int i = 0; i < rolls; ++i)
			curMap.addStructure(Utils.randElement(structures));
	}

	private void initialMobSpawn() {
		for (int i = 0; i < curBiome.overworld.initialMobSpawn; ++i)
			curMap.spawnMobs(Utils.percentRoll(curBiome.overworld.initialFriendlyChance) || friendlyOnly, true);
	}

	private void genBiomeTerrain() {
		int defaultTile = curBiome.getDefaultTile();
		curMap.defTile = defaultTile;
		curMap.fillTile();
		Utils.out("Default tile: " + defaultTile);

		ArrayList<RollList> tileGenList = curBiome.getBiomeTiles();

		ArrayList<Integer> tileIdList;
		int listSize;

		for (RollList tileRollList : tileGenList) {
			for (int i = 0; i < tileRollList.rollCount; ++i) {
				tileIdList = tileRollList.idList;
				listSize = tileIdList.size();
				curMap.randPlaceTile(tileIdList.get(Utils.random.nextInt(listSize)));
			}
		}
	}

	private void genBiomeObjects() {
		Utils.out("Generating biome objects!");
		ArrayList<RollList> objectGenList = curBiome.getBiomeObjects();

		for (RollList objectRollList : objectGenList)
			curMap.rollObjects(objectRollList);

		Utils.out("Done generating biome objects!");
	}

	private void genLakes() {
		Utils.out("Generating lakes!");
		Map curLoc = worldMap.getLocation(curMapCoords).getLayer(curLayer);
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

		int x = 0;
		int y = 0;
		float from = lake.lakeFrom;
		float to = lake.lakeTo;

		while (x < w - 1) {
			while (y < h - 1) {

				if (grid.get(x, y) >= from && grid.get(x, y) <= to)
					curLoc.addTile(x, y, lake.lakeTile);

				y++;
			}
			x++;
			y = 0;
		}

		Utils.out("Done generating lakes!");
	}

	private void genCave() {
		Utils.out("Generating underworld...");
		getLocationBiome();
		CavePreset cave = curBiome.getBiomeCave();
		ArrayList<Integer> oreList = cave.caveOre;
		int oreListSize = oreList.size();

		int maxOreFieldSize = cave.maxVeinSize;
		int count = cave.maxVeinCount;
		int cdef = cave.caveTile;
		Map loc = worldMap.getLocation(curMapCoords).getLayer(curLayer);
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

	private void rollDungeon() {
		worldMap.getLocation(curMapCoords).hasDungeon = Utils.percentRoll(curBiome.dungeonProbability);
	}

	private void genDungeon() {
		Location loc = worldMap.getLocation(curMapCoords);

		if (!loc.hasDungeon)
			return;

		Map curLoc = loc.getLayer(curLayer);

		getLocationBiome();
		curLoc.rollSize();

		Utils.out("Generating dungeon!");

		DungeonPreset dungeon = curBiome.getBiomeDungeon();
		DungeonGen dungeonGen = new DungeonGen(curLoc);
		dungeonGen.generate(dungeon, curLayer);

		Utils.out("Done generating dungeon!");
	}

	private WorldGenPreset setCurBiome(int biome) {
		curBiome = WorldGenProp.getBiome(biome);
		return curBiome;
	}

	private WorldGenPreset getLocationBiome() { // Sets current generator biome to the overworld biome
		return setCurBiome(MadSand.world.getLocBiome());
	}

	public int chooseRandomBiome() {
		ArrayList<Integer> keyList = new ArrayList<Integer>(WorldGenProp.biomes.keySet());
		return keyList.get(Utils.random.nextInt(keyList.size()));
	}

}
