package hitonoriol.madsand.world.worldgen;

import java.io.File;
import java.util.ArrayList;

import com.github.czyzby.noise4j.map.Grid;
import com.github.czyzby.noise4j.map.generator.noise.NoiseGenerator;
import com.github.czyzby.noise4j.map.generator.util.Generators;

import hitonoriol.madsand.LuaUtils;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.properties.WorldGenProp;
import hitonoriol.madsand.world.WorldMap;
import hitonoriol.madsand.world.MapID;
import hitonoriol.madsand.world.World;

public class WorldGen {
	private WorldMap worldMap;
	private WorldGenPreset curBiome;
	private MapID curMapId;
	private Map curLoc;

	public WorldGen(WorldMap worldMap) {
		this.worldMap = worldMap;
	}

	public void generate(MapID mapId) {
		this.curMapId = mapId;
		this.curLoc = worldMap.get(mapId);
		curLoc.rollSize();
		curLoc.purge();

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

		executeLocationScript();
	}

	private void executeLocationScript() {
		Pair coords = curMapId.worldxy;
		String locationScriptPath = LuaUtils.getSectorScriptPath(coords.x, coords.y);
		File locationScript = new File(MadSand.SCRIPTDIR + locationScriptPath);
		
		if (!locationScript.exists())
			return;

		LuaUtils.executeScript(locationScriptPath);
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
			Utils.out("RollList: " + tileRollList.rollCount);
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

		for (RollList objectRollList : objectGenList)
			curLoc.rollObjects(objectRollList);

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

		int x = 0;
		int y = 0;
		float from = ((float) lake.lakeFrom) / 10f;
		float to = ((float) lake.lakeTo) / 100f;

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

	private void genDungeon(int layer) {
		MapID dungeonId = new MapID(curMapId).setLayer(layer);
		Map curLoc = worldMap.get(dungeonId);
		curLoc.rollSize();

		Utils.out("Generating dungeon!");

		int prob = curBiome.dungeonProbability;
		Utils.out("Probability: " + prob + "%");

		if (layer == World.LAYER_BASE_UNDERWORLD && !Utils.percentRoll(prob)) {
			Utils.out("Well... Decided not to.");
			return;
		}

		DungeonPreset dungeon = curBiome.getBiomeDungeon();
		DungeonGen dungeonGen = new DungeonGen(curLoc);
		dungeonGen.generate(dungeon, layer);

		Utils.out("Done generating dungeon!");
	}

	public int chooseRandomBiome() {
		ArrayList<Integer> keyList = new ArrayList<Integer>(WorldGenProp.biomes.keySet());
		return keyList.get(Utils.random.nextInt(keyList.size()));
	}

}
