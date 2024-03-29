package hitonoriol.madsand.world.worldgen;

import java.util.ArrayList;
import java.util.List;

import com.github.czyzby.noise4j.map.Grid;
import com.github.czyzby.noise4j.map.generator.noise.NoiseGenerator;
import com.github.czyzby.noise4j.map.generator.util.Generators;

import hitonoriol.madsand.Enumerable;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.Faction;
import hitonoriol.madsand.gamecontent.Tiles;
import hitonoriol.madsand.gamecontent.WorldGenPresets;
import hitonoriol.madsand.lua.Lua;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.util.Utils;
import hitonoriol.madsand.world.Location;
import hitonoriol.madsand.world.WorldMap;

public class WorldGen {
	private WorldMap worldMap;
	private WorldGenPreset curBiome;
	private int curBiomeId;
	private Pair curMapCoords;
	private int curLayer;
	private Location curLocation;
	private Map curMap;
	private int width, height;
	private float scaleBy;
	private boolean friendlyOnly, skipLandPropGen;

	private int dungeonTile = Enumerable.findId(Tiles.all().get(), "Dungeon Trapdoor");

	public WorldGen(WorldMap worldMap) {
		setWorldMap(worldMap);
	}

	public WorldGen() {}

	public void setWorldMap(WorldMap worldMap) {
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

	public WorldGen skipLandPropGen() {
		skipLandPropGen = true;
		return this;
	}

	public WorldGen friendlyOnly() {
		friendlyOnly = true;
		return this;
	}

	public WorldGen setBiome(int biome) {
		curBiomeId = biome;
		curBiome = WorldGenPresets.all().get(biome);
		return this;
	}

	public WorldGen setSize(int w, int h) {
		width = w;
		height = h;
		return this;
	}

	public void reset() {
		setSize(-1, -1).setBiome(-1);
		friendlyOnly = false;
		skipLandPropGen = false;
	}

	public void generate() {
		curLocation = worldMap.getLocation(curMapCoords);
		curMap = curLocation.getLayer(curLayer);

		Utils.out("Generating " + curMapCoords + ": " + curLayer);
		Utils.dbg("Dungeon tile: " + dungeonTile);

		if (width < 1 || height < 1)
			curMap.rollSize();
		else
			curMap.setSize(width, height);

		scaleBy = curMap.getScaleFactor();
		Utils.dbg("Scaling rollLists by: " + scaleBy);

		curMap.purge();

		if (curLayer == Location.LAYER_OVERWORLD) {

			if (curBiome.equals(WorldGenPresets.all().defaultValue()))
				setBiome(chooseRandomBiome());

			Utils.dbg("Biome: " + curBiomeId);
			curLocation.biome = curBiomeId;

			setBiome(curBiomeId);

			genBiomeTerrain();
			genLakes();
			genBiomeObjects();
			genBiomeStructures();
			rollDungeonEntrance();
			initialMobSpawn();
			rollLandProperties();
		} else {

			if (curLayer <= Location.LAYER_MAX_DUNGEON)
				genDungeon();
			else
				genCave();

		}

		if (curBiome.postGenScript != null)
			Lua.execute(curBiome.postGenScript);

		reset();
	}

	private Map currentMap() {
		return worldMap.getLocation(curMapCoords).getLayer(curLayer);
	}

	private void rollLandProperties() {
		if (skipLandPropGen)
			return;

		if (Utils.percentRoll(curBiome.ownedByFactionProbability))
			curLocation.faction = Utils.randElement(Faction.humanFactions);

		if (Utils.percentRoll(curBiome.settlementProbability)) {
			curLocation.createSettlement().randPopulate();
			Utils.dbg("Created an NPC-owned settlement! Population: " + curLocation.settlement.getPopulation());
		}
	}

	static float STRUCTURE_SCALE_FACTOR = 0.35f;

	private void genBiomeStructures() {
		var structures = curBiome.overworld.structures;

		if (structures.isEmpty())
			return;

		int maxStructures = (int) (curBiome.overworld.maxStructures * Math.max(scaleBy * STRUCTURE_SCALE_FACTOR, 1));
		int rolls = Utils.rand(curBiome.overworld.minStructures, maxStructures);
		Utils.dbg("Generating " + rolls + " structures (" + maxStructures + " max)");
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
		Utils.dbg("Default tile: " + defaultTile);

		var tileGenList = curBiome.getBiomeTiles();

		ArrayList<Integer> tileIdList;
		int listSize;
		for (RollList tileRollList : tileGenList) {
			for (int i = 0; i < tileRollList.getRollCount(scaleBy); ++i) {
				tileIdList = tileRollList.idList;
				listSize = tileIdList.size();
				curMap.randPlaceTile(tileIdList.get(Utils.random.nextInt(listSize)));
			}
		}
	}

	private void genBiomeObjects() {
		Utils.out("Generating biome objects!");
		var objectGenList = curBiome.getBiomeObjects();

		for (RollList objectRollList : objectGenList)
			curMap.rollObjects(objectRollList);

		Utils.out("Done generating biome objects!");
	}

	private void genLakes() {
		Utils.out("Generating lakes!");
		int w = curMap.getWidth(), h = curMap.getHeight();
		var lakes = curBiome.getBiomeLake();

		if (lakes.intervals.isEmpty())
			return;

		final var grid = new Grid(w, h);
		final var noiseGenerator = new NoiseGenerator();

		noiseGenerator.setRadius(lakes.lakeRadius);
		noiseGenerator.setModifier(lakes.lakeModifier);
		noiseGenerator.setSeed(Generators.rollSeed());
		noiseGenerator.generate(grid);

		for (LakePreset.Interval lake : lakes.intervals)
			genLake(curMap, grid, lake);

		Utils.out("Done generating lakes!");
	}

	private void genLake(Map map, Grid grid, LakePreset.Interval lake) {
		int tiles = 0;
		for (int x = 0; x < grid.getWidth() - 1; ++x) {
			for (int y = 0; y < grid.getHeight() - 1; ++y) {
				if (grid.get(x, y) >= lake.from && grid.get(x, y) <= lake.to) {
					map.addTile(x, y, lake.tile);
					++tiles;
				}
			}
		}
		Utils.out("Generated " + tiles + " " + Tiles.all().getName(lake.tile) + " tiles");
	}

	private void genCave() {
		Utils.out("Generating underworld...");
		getLocationBiome();
		var cave = curBiome.getBiomeCave();
		var oreList = cave.caveOre;
		int oreListSize = oreList.size();

		int maxOreFieldSize = cave.maxVeinSize;
		int count = cave.maxVeinCount;
		int cdef = cave.caveTile;
		var loc = currentMap();
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

	private void rollDungeonEntrance() {
		if (worldMap.getLocation(curMapCoords).hasDungeon = Utils.percentRoll(curBiome.dungeonProbability)) {
			var coords = currentMap().randPlaceTile(dungeonTile);
			Utils.dbg("Created dungeon entrance @ (%s)", coords);
		} else
			Utils.dbg("No dungeon entrance generated in this location");

	}

	private void genDungeon() {
		var loc = worldMap.getLocation(curMapCoords);

		if (!loc.hasDungeon)
			return;

		var curLoc = loc.getLayer(curLayer);

		getLocationBiome();
		curLoc.rollSize();
		curLoc.defTile = curBiome.overworld.defaultTile;
		curLoc.defObject = curBiome.cave.caveObject;

		Utils.out("Generating dungeon!");

		var dungeon = curBiome.getBiomeDungeon();
		var dungeonGen = new DungeonGen(curLoc);
		dungeonGen.generate(dungeon, curLayer);

		Utils.out("Done generating dungeon!");
	}

	private WorldGenPreset setCurBiome(int biome) {
		curBiome = WorldGenPresets.all().get(biome);
		return curBiome;
	}

	private WorldGenPreset getLocationBiome() { // Sets current generator biome to the overworld biome
		return setCurBiome(MadSand.world().getLocBiome());
	}

	public int chooseRandomBiome() {
		List<Integer> keyList = new ArrayList<>(WorldGenPresets.all().get().keySet());
		return keyList.get(Utils.random.nextInt(keyList.size()));
	}

}
