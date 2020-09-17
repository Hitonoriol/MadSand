package hitonoriol.madsand.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.GameSaver;
import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.Npc;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.world.worldgen.WorldGen;

public class World {
	Map nullLoc = new Map(0, 0);
	public static final int DEFAULT_WORLDSIZE = 10;
	public static final int DUNGEON_LAYER_MAX = 50;
	public static final int TILE_CAVE_EXIT = 25; //TODO move this to cave preset

	private Pair coords = new Pair();
	private int xsz, ysz; // max world size, not really used anywhere (still)

	public int curywpos; // global coords of current sector
	public int curxwpos;

	public int curlayer = 0; // current layer: layer > 0 - underworld | layer == 0 - overworld

	public static final int BORDER = 1;// map border - old shit, not really useful anymore

	public static int DEFAULT_MAPSIZE = Map.MIN_MAPSIZE;

	public static final int LAYER_OVERWORLD = 0;
	public static final int LAYER_BASE_UNDERWORLD = 1;

	@JsonIgnore
	public WorldGen worldGen;

	public static Player player;

	@JsonIgnore
	public WorldMap worldMap; // container for all maps and layers

	public int worldtime = 12; // time (00 - 23)
	int ticksPerHour = 100; // ticks per one hourTick() trigger
	public int tick = 0; // tick counter, resets every <ticksPerHour> ticks
	public long globalTick = 0; // global tick counter, never resets

	public World(int sz) {
		this.xsz = sz;
		this.ysz = sz;
		curxwpos = xsz / 2;
		curywpos = ysz / 2;

		worldMap = new WorldMap();

		if (!createBasicLoc(new Pair(curxwpos, curywpos), DEFAULT_MAPSIZE, DEFAULT_MAPSIZE))
			Utils.die("Could not create new world");

		worldGen = new WorldGen(worldMap);
	}

	public World() {
		this(DEFAULT_WORLDSIZE);
	}

	HashMap<MapID, Map> _getLoc(int wx, int wy, int layer) {
		HashMap<MapID, Map> ret = new HashMap<MapID, Map>();
		ret.put(new MapID(new Pair(wx, wy), layer), getLoc(wx, wy, layer));
		return ret;
	}

	boolean locExists(MapID loc) {
		return worldMap.containsKey(loc);
	}

	boolean createLoc(MapID loc, Map map) {
		if (!locExists(loc)) {
			this.worldMap.put(loc, map);
			return true;
		} else
			return false;
	}

	Map getLoc(Pair wc, int layer, int id) {
		MapID loc = new MapID(wc, layer, id);
		if (locExists(loc)) {
			return worldMap.get(loc);
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

	MapID getCurMapID() {
		return new MapID(new Pair(coords.set(curxwpos, curywpos)), curlayer);
	}

	int getLocBiome() {
		return getCurLoc(LAYER_OVERWORLD).getBiome();
	}

	int getDefaultTile() {
		return getCurLoc().defTile;
	}

	Map putLoc(Pair wc, int layer, int id, Map loc) {
		worldMap.put(new MapID(wc, layer, id, true), loc);
		return loc;
	}

	Map putLoc(int x, int y, int layer, Map loc) {
		return putLoc(coords.set(x, y), layer, 0, loc);
	}

	boolean createBasicLoc(Pair wc, int mx, int my) {
		if (!this.createLoc(new MapID(wc, LAYER_OVERWORLD), new Map(mx, my)))
			return false;
		if (!this.createLoc(new MapID(wc, LAYER_BASE_UNDERWORLD), new Map(mx, my)))
			return false;
		return true;
	}

	boolean createBasicLoc(int wx, int wy) {
		return createBasicLoc(new Pair(wx, wy), DEFAULT_MAPSIZE, DEFAULT_MAPSIZE);
	}

	boolean createBasicLoc(int layer) {
		return createLoc(new MapID(new Pair(curxwpos, curywpos), layer), new Map(DEFAULT_MAPSIZE, DEFAULT_MAPSIZE));
	}

	int biome;

	public void generate(int wx, int wy, int layer) {
		Utils.out("Generating new sector!");

		MapID mapId = new MapID(coords.set(curxwpos, curywpos), layer);

		if (!locExists(mapId.setLayer(LAYER_OVERWORLD)))
			createBasicLoc(wx, wy);
		clearCurLoc();

		worldGen.generate(mapId.setLayer(layer));

		Utils.out("Done generating new sector!");
	}

	public void generate(int layer) {
		if (layer != LAYER_OVERWORLD)
			createBasicLoc(layer);
		generate(curxwpos, curywpos, layer);
		updateLight();
	}

	public void generate() {
		generate(curxwpos, curywpos, LAYER_OVERWORLD);
		updateLight();
	}

	private void jumpToLocation(int x, int y, int layer) {
		curxwpos = x;
		curywpos = y;
		curlayer = layer;
	}

	public boolean switchLocation(int x, int y, int layer) {
		Utils.out("Switching location to " + x + ", " + y + " layer " + layer);
		if (layer > DUNGEON_LAYER_MAX)
			return false;

		jumpToLocation(x, y, layer);
		Utils.out(curxwpos + " " + curywpos + " " + curlayer);
		if (locExists(new MapID(coords.set(x, y), layer))) {
			updateLight();
			return true;
		}
		clearCurLoc();
		if (GameSaver.verifyNextSector(x, y))
			GameSaver.loadLocation();
		else
			generate(layer);
		player.updCoords();
		updateLight();
		return true;
	}

	public boolean switchLocation(Direction dir) {
		Map curLoc = getCurLoc();
		Utils.out("Switch location in direction " + dir);

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
			player.y = curLoc.getHeight() - 2;
			break;
		case LEFT:
			player.x = curLoc.getWidth() - 2;
			break;
		case RIGHT:
			player.x = 0;
			break;
		default:
			Utils.die("Can't switch location in dialonal direction, bruh");
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
		if (curlayer > (worldMap.layers - 1))
			++worldMap.layers;

		Map loc = getCurLoc();
		String place = null;

		Utils.out("In cave : " + (loc.spawnPoint.equals(Pair.nullPair)));

		int x = loc.spawnPoint.x, y = loc.spawnPoint.y;
		int x1 = Pair.nullPair.x, y1 = Pair.nullPair.y;
		if (x1 != x && y1 != y) { // this means we are in the dungeon
			player.teleport(loc.spawnPoint.x, loc.spawnPoint.y);
			place = "dungeon";
		} else { // this means we are in the cave
			delObj(player.x, player.y);
			putMapTile(player.x, player.y, TILE_CAVE_EXIT);
			Gui.overlay.processActionMenu();
			updateLight();
			place = "cave";
		}

		MadSand.print("You descend to " + place + " level " + curlayer);
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
		Gui.overlay.processActionMenu();
		return ret;
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

	public void delNpc(Npc npc) {
		getCurLoc().removeNpc(npc);
	}

	public void clearCurLoc() {
		Utils.out("Clearing current sector...");
		getCurLoc().purge();
	}

	public int getTileOrDefault(int x, int y) {
		if (x >= 0 && y >= 0 && x < getCurLoc().getWidth() && y < getCurLoc().getHeight()) {

			int tile = getCurLoc().getTile(x, y).id;
			if (tile >= 0 && tile <= Resources.tileCount)
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

	private static final int fovDelta = 7;
	private static int TIME_FOV_DECREASE_START = 18;// hour when the fov begins to decrease
	private static int TIME_FOV_DECREASE_END = TIME_FOV_DECREASE_START + fovDelta;
	private static int TIME_FOV_INCREASE_START = 4;// hour when the fov begins to decrease
	private static int TIME_FOV_INCREASE_END = TIME_FOV_INCREASE_START + fovDelta;

	void hourTick() {
		Map curLoc = getCurLoc();

		++worldtime;

		if (worldtime == 24)
			worldtime = 0;

		if (worldtime > TIME_FOV_DECREASE_START && worldtime <= TIME_FOV_DECREASE_END)
			player.setFov(player.fov - 1);

		if (worldtime > TIME_FOV_INCREASE_START && worldtime <= TIME_FOV_INCREASE_END)
			player.setFov(player.fov + 1);

		curLoc.naturalRegeneration();

		MadSand.notice("Another hour passes...");
		MadSand.notice("It's " + worldtime + ":00");
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
		HashMap<Pair, Npc> npcs = loc.getNpcs();
		ArrayList<Npc> queue = new ArrayList<Npc>();

		for (Entry<Pair, Npc> npc : npcs.entrySet())
			queue.add(npc.getValue());

		for (Npc npc : queue)
			npc.act();

	}

	@JsonIgnore
	public boolean isUnderGround() {
		return curlayer != World.LAYER_OVERWORLD;
	}

}