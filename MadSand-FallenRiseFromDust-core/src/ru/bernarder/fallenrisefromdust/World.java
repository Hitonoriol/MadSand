package ru.bernarder.fallenrisefromdust;

import java.util.HashMap;
import java.util.Vector;

import com.github.czyzby.noise4j.map.Grid;
import com.github.czyzby.noise4j.map.generator.noise.NoiseGenerator;
import com.github.czyzby.noise4j.map.generator.room.dungeon.DungeonGenerator;
import com.github.czyzby.noise4j.map.generator.util.Generators;

import ru.bernarder.fallenrisefromdust.enums.Direction;
import ru.bernarder.fallenrisefromdust.properties.WorldGenProp;

public class World {
	Map nullLoc = new Map(0, 0);
	private int xsz, ysz; // max world size, not really used anywhere
	public int curywpos = 5; // current location's global coordinates
	public int curxwpos = 5;
	public int curlayer = 0; // current layer: layer>0 = underworld | layer<0 overworld
	static final int BORDER = 1;// map border(old shit, not really useful anymore)
	static int MAPSIZE = 100; // default location size

	static final int LAYER_OVERWORLD = 0;
	static final int LAYER_UNDERWORLD = 1;

	static Player player;
	Location WorldLoc;

	public int worldtime = 12; // time (00 - 23)
	int ticksPerHour = 100; // ticks per one hourTick() trigger
	public int tick = 0; // tick counter, resets every <ticksPerHour> ticks
	public long globalTick = 0; // global tick counter, never resets

	public World(int sz) {
		this.xsz = sz;
		this.ysz = sz;
		WorldLoc = new Location();
		if (!createBasicLoc(new Pair(curxwpos, curywpos), MAPSIZE, MAPSIZE))
			System.exit(-1);
	}

	public World() {
		this(10);
		// Quite empty here...
	}

	int randBiome() {
		return Utils.random.nextInt(MadSand.BIOMES);
	}

	HashMap<MapID, Map> _getLoc(int wx, int wy, int layer) {
		HashMap<MapID, Map> ret = new HashMap<MapID, Map>();
		ret.put(new MapID(new Pair(wx, wy), layer), getLoc(wx, wy, layer));
		return ret;
	}

	boolean locExists(MapID loc) {
		return WorldLoc.containsKey(loc);
	}

	boolean createLoc(MapID loc, Map map) {
		if (!locExists(loc)) {
			this.WorldLoc.put(loc, map);
			return true;
		} else
			return false;
	}

	Map getLoc(Pair wc, int layer, int id) {
		MapID loc = new MapID(wc, layer, id);
		if (locExists(loc)) {
			return WorldLoc.get(loc);
		} else
			return nullLoc;
	}

	Map getLoc(int x, int y, int layer) {
		return getLoc(new Pair(x, y), layer, 0);
	}

	Map getCurLoc() {
		return getLoc(curxwpos, curywpos, curlayer);
	}

	Map getCurLoc(int layer) {
		return getLoc(curxwpos, curywpos, layer);
	}

	int getLocBiome() {
		return getCurLoc(LAYER_OVERWORLD).getBiome();
	}

	int getDefaultTile() {
		return getCurLoc().getDefTile();
	}

	Map putLoc(Pair wc, int layer, int id, Map loc) {
		WorldLoc.put(new MapID(wc, layer, id), loc);
		return loc;
	}

	Map putLoc(int x, int y, int layer, Map loc) {
		return putLoc(new Pair(x, y), layer, 0, loc);
	}

	boolean createBasicLoc(Pair wc, int mx, int my) {
		if (!this.createLoc(new MapID(wc, 0), new Map(mx, my)))
			return false;
		if (!this.createLoc(new MapID(wc, 1), new Map(mx, my)))
			return false;
		return true;
	}

	boolean createBasicLoc(int wx, int wy) {
		return createBasicLoc(new Pair(wx, wy), MAPSIZE, MAPSIZE);
	}

	int biome;

	public void Generate() {
		Utils.out("WorldGen start!");
		if (!locExists(new MapID(new Pair(curxwpos, curywpos), 0)))
			createBasicLoc(curxwpos, curywpos);
		try {
			clearCurLoc();
			if ((curxwpos == 5) && (curywpos == 5))
				biome = 0;
			else
				biome = randBiome();
			getCurLoc().setBiome(biome);
			genTerrain();
			genObjByTemplate();
			genUnderworld();
			genDungeon();
			curlayer = 0;
			Utils.out("End of WorldGen!");
		} catch (Exception e) {
			Utils.out("Whoops, fatal error during worldgen... See MadSandCritical.log and/or MadSandErrors.log files.");
			e.printStackTrace();
		}
	}

	final String LAKE_TID = "tid";
	final String LAKE_RADIUS = "radius";
	final String LAKE_MODIFIER = "modifier";
	final String LAKE_FROM = "from";
	final String LAKE_TO = "to";

	public void genTerrain() {
		Utils.out("Generating terrain!");
		genBiomeTerrain();
		HashMap<String, Integer> lake = WorldGenProp.getBiomeLake(biome);
		if (lake.get(LAKE_TID) != -1) {
			final Grid grid = new Grid(World.MAPSIZE + World.BORDER);
			final NoiseGenerator noiseGenerator = new NoiseGenerator();
			noiseGenerator.setRadius(lake.get(LAKE_RADIUS));
			noiseGenerator.setModifier(lake.get(LAKE_MODIFIER));
			noiseGenerator.setSeed(Generators.rollSeed());
			noiseGenerator.generate(grid);

			int i = 0;
			int ii = 0;
			float from = ((float) lake.get(LAKE_FROM)) / 10f;
			float to = ((float) lake.get(LAKE_TO)) / 100f;

			Utils.out("lakes from: " + from + " to: " + to);

			while (i < World.MAPSIZE + World.BORDER) {
				while (ii < World.MAPSIZE + World.BORDER) {
					if (grid.get(ii, i) >= from && grid.get(ii, i) <= to) {
						getCurLoc().addTile(ii, i, lake.get(LAKE_TID));
					}
					ii++;
				}
				i++;
				ii = 0;
			}
		}
		Utils.out("Done generating terrain!");
	}

	private static String DUNGEON_PROBABILITY = "probability";
	private static String DUNGEON_MAXROOMSIZE = "maxroomsize";
	private static String DUNGEON_MINROOMSIZE = "minroomsize";
	private static String DUNGEON_TOLERANCE = "tolerance";
	private static int PROB_DIVISOR = 2;

	public void genDungeon() {
		Utils.out("Generating dungeon!");
		HashMap<String, Integer> dungeon = WorldGenProp.getBiomedungeon(biome);
		int prob = dungeon.get(DUNGEON_PROBABILITY);
		if (prob > 0 && Utils.random.nextInt(prob) == prob / PROB_DIVISOR) {
			Utils.out("Nope... Not feeling like generating your sheet");
			return;
		}
		final Grid grid = new Grid(World.MAPSIZE);
		final DungeonGenerator dungeonGenerator = new DungeonGenerator();
		dungeonGenerator.setRoomGenerationAttempts(World.MAPSIZE);
		dungeonGenerator.setMaxRoomSize(dungeon.get(DUNGEON_MAXROOMSIZE));
		dungeonGenerator.setTolerance(dungeon.get(DUNGEON_TOLERANCE)); // Max difference between width and height.
		dungeonGenerator.setMinRoomSize(dungeon.get(DUNGEON_MINROOMSIZE));
		dungeonGenerator.generate(grid);
		int it = 0, iit = 0;
		while (it < World.MAPSIZE) {
			while (iit < World.MAPSIZE) {
				if (grid.get(iit, it) == 0.0f) {
					getCurLoc(1).addObject(iit, it, 0);
					getCurLoc(1).addTile(it, iit, 5);
				}
				if (grid.get(iit, it) == 1.0f)
					getCurLoc(1).addObject(iit, it, 13);
				if (grid.get(iit, it) == 0.5f) {
					getCurLoc(1).addObject(iit, it, 0);
					getCurLoc(1).addTile(it, iit, 12);
				}

				iit++;
			}
			iit = 0;
			it++;
		}
		Utils.out("Done dungeon generating!");
	}

	final int CAVE_TILE = 0;
	final int CAVE_OBJECT = 1;

	void genUnderworld() {
		Utils.out("Generating underworld...");
		Vector<Integer> underworld = WorldGenProp.getBiomeUnderworld(biome);
		int usz = underworld.size();
		int maxOreFieldSize = underworld.get(usz - 2);
		int count = underworld.get(usz - 1);
		int cdef = underworld.get(CAVE_TILE);
		getCurLoc(LAYER_UNDERWORLD).fillTile(cdef);
		getCurLoc(LAYER_UNDERWORLD).fillObject(underworld.get(CAVE_OBJECT));
		getCurLoc(LAYER_UNDERWORLD).setDefTile(cdef);
		int a = Utils.random.nextInt(count) + 1;
		while (a > 0) {
			try {
				int x = Utils.random.nextInt(World.MAPSIZE);
				int y = Utils.random.nextInt(World.MAPSIZE);
				int w = Utils.random.nextInt(maxOreFieldSize) + 1;
				int h = Utils.random.nextInt(maxOreFieldSize) + 1;
				if ((x + w < World.MAPSIZE) && (y + h < World.MAPSIZE)) {
					int id = underworld.get(Utils.rand(1, usz - 3));
					int k = 0;
					int kk = 0;
					while (kk < w) {
						while (k < h) {
							getCurLoc(LAYER_UNDERWORLD).addObject(x + kk, y + k, id);
							k++;
						}
						k = 0;
						kk++;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			a--;
		}
		Utils.out("Underworld generated!");
	}

	public void genBiomeTerrain() {
		Vector<Vector<Integer>> terrainBlock = WorldGenProp.getBiomeTiles(biome);
		int def = terrainBlock.get(0).get(0); // getting default tile
		getCurLoc().setDefTile(def);
		Utils.out("Default tile: " + def);
		getCurLoc().fillTile(def);
		int quantity, gsz;
		Vector<Integer> group;
		for (int i = 1; i < terrainBlock.size() - 1; ++i) {
			group = terrainBlock.get(i);
			gsz = group.size();
			quantity = group.get(--gsz); // last value of every group is the total quantity of objects from group to
											// generate
			for (int j = 0; j < quantity - 1; ++j) {
				getCurLoc().randPlaceTile(group.get(Utils.rand(0, gsz))); // we don't check whether there are tiles
																			// already on rand()'d place, but who gives
																			// a 5h17?
			}
		}
	}

	int getTileId(int x, int y) {
		return getCurLoc().getTile(x, y).id;
	}

	int getObjID(int x, int y) {
		return getCurLoc().getObject(x, y).id;
	}

	int getObjID(int x, int y, Direction dir) {
		return getCurLoc().getObject(x, y, dir).id;
	}

	void putMapTile(int x, int y, int id) {
		getCurLoc().addTile(x, y, id);
	}

	void putMapTile(int x, int y, int layer, int id) {
		getCurLoc(layer).addTile(x, y, id);
	}

	void addObj(int x, int y, int id) {
		getCurLoc().addObject(x, y, id);
	}

	void delObj(int x, int y) {
		addObj(x, y, 0);
	}

	void addObj(int x, int y, int layer, int id) {
		getCurLoc(layer).addObject(x, y, id);
	}

	void delObj(int x, int y, int layer) {
		addObj(x, y, layer, 0);
	}

	public void clearCurLoc() {
		Utils.out("clearCurLoc");
		Map curLoc = getCurLoc().purge();
		int xsz = curLoc.getWidth(), ysz = curLoc.getHeight();
		biome = 0;
		int i = 0;
		int ii = 0;
		while (i < ysz + World.BORDER) {
			while (ii < xsz + World.BORDER) {
				putMapTile(ii, i, 1, 5);
				addObj(ii, i, 1, 13);
				addObj(ii, i, 0);
				putMapTile(ii, i, 0);
				ii++;
			}
			i++;
			ii = 0;
		}
		Utils.out("End of clearCurLoc!");
	}

	void genObjByTemplate() {
		Utils.out("Generating biome objects!");
		Vector<Vector<Integer>> object = WorldGenProp.getBiomeObjects(biome);
		Vector<Integer> block;
		int count;
		int range;
		for (int i = 0; i < object.size(); ++i) {
			block = object.get(i);
			range = block.size() - 1;
			count = block.get(range);
			for (int j = 0; j < count; ++j) {
				getCurLoc().randPlaceObject(block, range);
			}
		}
		Utils.out("Done generating biome objects!");
	}

	public int getTileOrDefault(int x, int y) {
		if (x >= 0 && y >= 0 && x < getCurLoc().getWidth() && y < getCurLoc().getHeight()) {
			int tile = getCurLoc().getTile(x, y).id;
			if (tile >= 0 && tile <= MadSand.LASTTILEID)
				return tile;
			else
				return getDefaultTile();
		} else
			return getDefaultTile();
	}

	public static int worldCoord(int q) {
		return q * MadSand.TILESIZE;
	}

	int getWidth() {
		return xsz;
	}

	int getHeight() {
		return ysz;
	}

	void hourTick() {
		++worldtime;
		if (MadSand.world.worldtime == 24)
			MadSand.world.worldtime = 0;
		if (((MadSand.world.worldtime >= 0) && (MadSand.world.worldtime <= 5))
				|| ((MadSand.world.worldtime >= 21) && (MadSand.world.worldtime <= 23))) {
			Gui.darkness.setVisible(true);
		} else {
			Gui.darkness.setVisible(false);
		}
	}

	void tick() {
		Utils.tileDmg();
		getCurLoc().update();
		player.stats.perTickCheck();
		++globalTick;
		if (++tick >= ticksPerHour - 1) {
			tick = 0;
			hourTick();
		}
	}

	void ticks(int n) {
		for (int i = n; i > 0; --i)
			tick();
	}
}