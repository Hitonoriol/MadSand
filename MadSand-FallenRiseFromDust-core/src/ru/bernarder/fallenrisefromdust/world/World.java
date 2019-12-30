package ru.bernarder.fallenrisefromdust.world;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.czyzby.noise4j.map.Grid;
import com.github.czyzby.noise4j.map.generator.noise.NoiseGenerator;
import com.github.czyzby.noise4j.map.generator.room.dungeon.DungeonGenerator;
import com.github.czyzby.noise4j.map.generator.util.Generators;

import ru.bernarder.fallenrisefromdust.GameSaver;
import ru.bernarder.fallenrisefromdust.MadSand;
import ru.bernarder.fallenrisefromdust.Utils;
import ru.bernarder.fallenrisefromdust.containers.Pair;
import ru.bernarder.fallenrisefromdust.entities.Npc;
import ru.bernarder.fallenrisefromdust.entities.Player;
import ru.bernarder.fallenrisefromdust.enums.Direction;
import ru.bernarder.fallenrisefromdust.map.Map;
import ru.bernarder.fallenrisefromdust.properties.WorldGenProp;

public class World {
	Map nullLoc = new Map(0, 0);
	public static final int DEFAULT_WORLDSIZE = 10;
	public static final int DUNGEON_LAYER_MAX = 20;

	private Pair coords = new Pair();
	private int xsz, ysz; // max world size, not really used anywhere (still)

	public int curywpos; // global coords of current sector
	public int curxwpos;

	public int curlayer = 0; // current layer: layer > 0 - underworld | layer == 0 - overworld

	public static final int BORDER = 1;// map border (old shit, not really useful anymore, but i'm too afraid to delete
										// it)
	public static int MAPSIZE = 100; // default location size

	public static final int LAYER_OVERWORLD = 0;
	static final int LAYER_BASE_UNDERWORLD = 1;

	public static Player player;

	@JsonIgnore
	public Location WorldLoc; // container for all the maps and layers

	public int worldtime = 12; // time (00 - 23)
	int ticksPerHour = 100; // ticks per one hourTick() trigger
	public int tick = 0; // tick counter, resets every <ticksPerHour> ticks
	public long globalTick = 0; // global tick counter, never resets

	public World(int sz) {
		this.xsz = sz;
		this.ysz = sz;
		curxwpos = xsz / 2;
		curywpos = ysz / 2;

		WorldLoc = new Location();
		if (!createBasicLoc(new Pair(curxwpos, curywpos), MAPSIZE, MAPSIZE))
			Utils.die("World constructor fucked up");
	}

	public World() {
		this(DEFAULT_WORLDSIZE);
	}

	int randBiome() {
		return Utils.random.nextInt(MadSand.BIOMES - 1); // Biome numbers start from -1
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
		return getLoc(coords.set(x, y), layer, 0);
	}

	@JsonIgnore
	public Map getCurLoc() {
		return getLoc(curxwpos, curywpos, curlayer);
	}

	Map getCurLoc(int layer) {
		return getLoc(curxwpos, curywpos, layer);
	}

	int getLocBiome() {
		return getCurLoc(LAYER_OVERWORLD).getBiome();
	}

	int getDefaultTile() {
		return getCurLoc().defTile;
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

	boolean createBasicLoc(int layer) {
		return createLoc(new MapID(new Pair(curxwpos, curywpos), layer), new Map(MAPSIZE, MAPSIZE));
	}

	int biome;

	public void Generate(int wx, int wy, int layer) {
		int underworld = LAYER_BASE_UNDERWORLD;
		if (layer == LAYER_OVERWORLD) {
			Utils.out("Generating new sector!");
			if (!locExists(new MapID(coords.set(curxwpos, curywpos), 0)))
				createBasicLoc(wx, wy);
			clearCurLoc();
			if ((wx == 5) && (wy == 5))
				biome = 0;
			else
				biome = randBiome();
			Utils.out("Biome: " + biome);
			getCurLoc().setBiome(biome);
			genTerrain();
			genObjByTemplate();
			genUnderworld(underworld);
		} else
			underworld = layer;
		genDungeon(underworld);
		Utils.out("Done generating new sector!");
	}

	public void Generate(int layer) {
		createBasicLoc(layer);
		Generate(curxwpos, curywpos, layer);
		updateLight();
	}

	public void Generate() {
		Generate(curxwpos, curywpos, LAYER_OVERWORLD);
		updateLight();
	}

	private void jumpToLocation(int x, int y, int layer) {
		curxwpos = x;
		curywpos = y;
		curlayer = layer;
	}

	public boolean switchLocation(int x, int y, int layer) {
		if (layer > DUNGEON_LAYER_MAX)
			return false;

		jumpToLocation(x, y, layer);
		if (locExists(new MapID(coords.set(x, y), layer))) {
			updateLight();
			return true;
		}
		clearCurLoc();
		if (GameSaver.verifyNextSector(x, y))
			GameSaver.loadLocation();
		else
			Generate(layer);
		player.updCoords();
		updateLight();
		return true;
	}

	public boolean switchLocation(Direction dir) {
		if (curlayer != LAYER_OVERWORLD)
			return false;
		coords.set(curxwpos, curywpos).addDirection(dir);
		MadSand.print("You travel to sector (" + coords.x + ", " + coords.y + ")");
		if (!switchLocation(coords.x, coords.y, LAYER_OVERWORLD))
			return false;
		switch (dir) {
		case UP:
			player.y = 0;
			break;
		case DOWN:
			player.y = MAPSIZE - 2;
			break;
		case LEFT:
			player.x = MAPSIZE - 2;
			break;
		case RIGHT:
			player.x = 0;
			break;
		}
		player.updCoords();
		updateLight();
		return true;
	}

	public boolean switchLocation(int layer) {
		return switchLocation(curxwpos, curywpos, layer);
	}

	public boolean descend() {
		if (curlayer == DUNGEON_LAYER_MAX)
			return false;
		boolean ret = switchLocation(curlayer + 1);
		if (curlayer > (WorldLoc.layers - 1))
			++WorldLoc.layers;
		Map loc = getCurLoc();
		if (loc.spawnPoint != Pair.nullPair)
			player.teleport(loc.spawnPoint.x, loc.spawnPoint.y);
		MadSand.print("You descend to dungeon level " + curlayer);
		return ret;
	}

	public boolean ascend() {
		if (curlayer == LAYER_OVERWORLD)
			return false;
		boolean ret = switchLocation(curlayer - 1);
		if (curlayer == LAYER_OVERWORLD)
			MadSand.print("You get back to surface level");
		else
			MadSand.print("You get back to dungeon level " + curlayer);
		return ret;
	}

	private final String LAKE_TID = "tid";
	private final String LAKE_RADIUS = "radius";
	private final String LAKE_MODIFIER = "modifier";
	private final String LAKE_FROM = "from";
	private final String LAKE_TO = "to";

	private void genTerrain() {
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

	private float DUNGEON_CORRIDOR_LEVEL = 0.0f;
	private float DUNGEON_WALL_LEVEL = 1.0f;
	private float DUNGEON_ROOM_LEVEL = 0.5f;

	private static String DUNGEON_PROBABILITY = "probability";
	private static String DUNGEON_MAXROOMSIZE = "maxroomsize";
	private static String DUNGEON_MINROOMSIZE = "minroomsize";
	private static String DUNGEON_TOLERANCE = "tolerance";

	private static String DUNGEON_WALL_OBJ = "wall_object";
	private static String DUNGEON_WALL_TILE = "wall_tile";
	private static String DUNGEON_ROOM_TILE = "room_tile";
	private static String DUNGEON_CORRIDOR_TILE = "corridor_tile";
	private static String DUNGEON_DOOR = "door_object";

	private void genDungeon(int layer) {
		boolean force = true;
		Utils.out("Generating dungeon!");
		HashMap<String, Integer> dungeon = WorldGenProp.getBiomedungeon(biome);
		int prob = dungeon.get(DUNGEON_PROBABILITY);
		Utils.out("Probability: " + prob);

		if (prob == 0)
			return;
		if (layer == LAYER_BASE_UNDERWORLD && Utils.randPercent() > prob)
			return;

		final Grid grid = new Grid(World.MAPSIZE);
		final DungeonGenerator dungeonGenerator = new DungeonGenerator();
		dungeonGenerator.setRoomGenerationAttempts(World.MAPSIZE);
		dungeonGenerator.setMaxRoomSize(dungeon.get(DUNGEON_MAXROOMSIZE));
		dungeonGenerator.setTolerance(dungeon.get(DUNGEON_TOLERANCE)); // Max difference between width and height.
		dungeonGenerator.setMinRoomSize(dungeon.get(DUNGEON_MINROOMSIZE));
		dungeonGenerator.generate(grid);

		int wallObj = dungeon.get(DUNGEON_WALL_OBJ);
		int wallTile = dungeon.get(DUNGEON_WALL_TILE);
		int roomTile = dungeon.get(DUNGEON_ROOM_TILE);
		int corridorTile = dungeon.get(DUNGEON_CORRIDOR_TILE);
		int door = dungeon.get(DUNGEON_DOOR);

		int y = 0, x = 0;
		Map loc = getCurLoc(layer);

		while (y < World.MAPSIZE) {
			while (x < World.MAPSIZE) {
				if (grid.get(x, y) == DUNGEON_CORRIDOR_LEVEL) { // corridors
					loc.delObject(x, y);
					loc.addTile(x, y, corridorTile, force);
				}

				if (grid.get(x, y) == DUNGEON_WALL_LEVEL) { // walls
					loc.addObject(x, y, wallObj);
					loc.addTile(x, y, wallTile);
				}

				if (grid.get(x, y) == DUNGEON_ROOM_LEVEL) { // rooms
					loc.delObject(x, y);
					loc.addTile(x, y, roomTile, force);
					if (loc.spawnPoint == Pair.nullPair)
						loc.spawnPoint = new Pair(x, y);
				}

				if (isDoorway(grid, x, y, loc.getWidth(), loc.getHeight())) // door
					loc.addObject(x, y, door);

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

	final int CAVE_TILE = 0;
	final int CAVE_OBJECT = 1;

	private void genUnderworld(int layer) {
		Utils.out("Generating underworld...");
		Vector<Integer> underworld = WorldGenProp.getBiomeUnderworld(biome);
		int usz = underworld.size();
		int maxOreFieldSize = underworld.get(usz - 2);
		int count = underworld.get(usz - 1);
		int cdef = underworld.get(CAVE_TILE);
		Map loc = getCurLoc(layer);

		loc.fillTile(cdef);
		loc.defObject = underworld.get(CAVE_OBJECT);
		loc.defTile = cdef;
		loc.fillObject();
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
							loc.addObject(x + kk, y + k, id);
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
		Utils.out("Done generating underworld!");
	}

	private void genBiomeTerrain() {
		Vector<Vector<Integer>> terrainBlock = WorldGenProp.getBiomeTiles(biome);
		int def = terrainBlock.get(0).get(0); // getting default tile
		getCurLoc().defTile = def;
		Utils.out("Default tile: " + def);
		getCurLoc().fillTile();
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

	public int getTileId(int x, int y) {
		return getCurLoc().getTile(x, y).id;
	}

	int getObjID(int x, int y) {
		return getCurLoc().getObject(x, y).id;
	}

	public int getObjID(int x, int y, Direction dir) {
		return getCurLoc().getObject(x, y, dir).id;
	}

	public void putMapTile(int x, int y, int id) {
		getCurLoc().addTile(x, y, id);
	}

	void putMapTile(int x, int y, int layer, int id) {
		getCurLoc(layer).addTile(x, y, id);
	}

	void addObj(int x, int y, int id) {
		getCurLoc().addObject(x, y, id);
	}

	public void delObj(int x, int y) {
		addObj(x, y, 0);
	}

	void addObj(int x, int y, int layer, int id) {
		getCurLoc(layer).addObject(x, y, id);
	}

	void delObj(int x, int y, int layer) {
		addObj(x, y, layer, 0);
	}

	public void clearCurLoc() {
		getCurLoc().purge();
	}

	private void genObjByTemplate() {
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

	private static final int fovDelta = 5;
	private static int TIME_FOV_DECREASE_START = 18;// hour when the fov begins to decrease
	private static int TIME_FOV_DECREASE_END = TIME_FOV_DECREASE_START + fovDelta;
	private static int TIME_FOV_INCREASE_START = 4;// hour when the fov begins to decrease
	private static int TIME_FOV_INCREASE_END = TIME_FOV_INCREASE_START + fovDelta;

	void hourTick() {
		++worldtime;

		if (worldtime == 24)
			worldtime = 0;

		if (worldtime > TIME_FOV_DECREASE_START && worldtime <= TIME_FOV_DECREASE_END) {
			player.setFov(player.fov - 1);
		}

		if (worldtime > TIME_FOV_INCREASE_START && worldtime <= TIME_FOV_INCREASE_END) {
			player.setFov(player.fov + 1);
		}
	}

	private void tick() {
		player.stats.perTickCheck();
		player.tileDmg();
		getCurLoc().update();
		++globalTick;

		if (++tick >= ticksPerHour - 1) {
			tick = 0;
			hourTick();
		}

	}

	public void updateLight() {
		getCurLoc().updateLight(player.x, player.y, player.fov);
	}

	public void ticks(int n) {
		for (int i = n; i > 0; --i) {
			tick();
		}

		Map loc = getCurLoc();
		Npc npc;

		for (Entry<Pair, Npc> entry : loc.getNpcs().entrySet()) {
			npc = entry.getValue();
			npc.act();
		}
	}
}