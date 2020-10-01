package hitonoriol.madsand.world;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.badlogic.gdx.utils.Timer;
import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.GameSaver;
import hitonoriol.madsand.Gui;
import hitonoriol.madsand.LuaUtils;
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
	private MapID mapID = new MapID();

	private int MAX_PREVIOUS_LOCATIONS = 2; // Max amount of maps allowed to be in WorldMap at the same time
	private ArrayDeque<MapID> previousLocations = new ArrayDeque<>(); // Maps that are currently loaded in WorldMap

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

	@JsonIgnore
	public static Player player;

	@JsonIgnore
	public WorldMap worldMap; // container for all maps and layers

	private Timer realTimeRefresher;
	public int realtimeTickRate = 5; // seconds per 1 tick
	public long globalRealtimeTick = 0; // global realtime tick counter, never resets

	private int ticksPerHour = 175; // ticks per one hourTick() trigger
	public int worldtime = 12; // time (00 - 23)
	public int tick = 0; // tick counter, resets every <ticksPerHour> ticks
	public long globalTick = 0; // global tick counter, never resets

	private long logoutTimeStamp;

	public World(int sz) {
		this.xsz = sz;
		this.ysz = sz;
		curxwpos = xsz / 2;
		curywpos = ysz / 2;

		player = new Player();
		worldMap = new WorldMap();
		worldGen = new WorldGen(worldMap);
		initRealtimeRefresher();
	}

	public World() {
		this(DEFAULT_WORLDSIZE);
	}

	public void enter() {
		realTimeRefresher.start();
	}

	private void initRealtimeRefresher() {
		realTimeRefresher = new Timer();
		realTimeRefresher.scheduleTask(new Timer.Task() {
			@Override
			public void run() {
				realtimeTick();
			}
		}, realtimeTickRate, realtimeTickRate);
	}

	public long getLogoutTimeStamp() {
		return logoutTimeStamp;
	}

	public void setLogoutTimeStamp(long logoutTimeStamp) {
		this.logoutTimeStamp = logoutTimeStamp;
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

	@JsonIgnore
	public MapID getCurMapID() {
		return mapID;
	}

	Map getLoc(Pair wc, int layer, int id) {
		MapID loc = mapID.set(wc, layer, id);
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

		mapID.set(new Pair(coords.set(curxwpos, curywpos)), layer);

		if (!locExists(mapID.setLayer(LAYER_OVERWORLD)))
			createBasicLoc(wx, wy);

		clearCurLoc();
		mapID.setLayer(layer);

		if (!executeLocationScript())
			worldGen.generate(mapID);

		Utils.out("Done generating new sector!");
	}

	private boolean executeLocationScript() {

		if (mapID.layer != LAYER_OVERWORLD)
			return false;

		Pair coords = mapID.worldxy;
		String locationScriptPath = LuaUtils.getSectorScriptPath(coords.x, coords.y);
		File locationScript = new File(MadSand.SCRIPTDIR + locationScriptPath);

		if (!locationScript.exists())
			return false;

		LuaUtils.executeScript(locationScriptPath);

		return true;
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
		Utils.out("Switching location to " + x + ", " + y + " layer: " + layer);
		if (layer > DUNGEON_LAYER_MAX)
			return false;

		jumpToLocation(x, y, layer);

		if (locExists(new MapID(coords.set(x, y), layer))) {
			Utils.out("This sector already exists! Noice.");
			updateLight();
			return true;
		}

		clearCurLoc();

		if (GameSaver.verifyNextSector(x, y))
			GameSaver.loadLocation();
		else
			generate(layer);

		cleanUpPreviousLocations();

		player.updCoords();
		updateLight();
		return true;
	}

	public boolean switchLocation(Direction dir) {
		Utils.out("Switch location in direction " + dir);

		if (!inEncounter)
			GameSaver.saveWorld();
		else
			removeLocation(getCurMapID());

		MadSand.switchScreen(Gui.travelScreen);

		if (player.stats.rollEncounter() && !inEncounter) {
			switchToEncounter();
			return true;
		}

		if (inEncounter)
			inEncounter = false;

		if (curlayer != LAYER_OVERWORLD)
			return false;

		coords.set(curxwpos, curywpos).addDirection(dir);
		MadSand.print("You travel to sector (" + coords.x + ", " + coords.y + ")");

		if (!switchLocation(coords.x, coords.y, LAYER_OVERWORLD)) {
			Utils.out("Switch location oopsie :c");
			return false;
		}

		Map curLoc = getCurLoc();
		int mapWidth = curLoc.getWidth(), mapHeight = curLoc.getHeight();

		if (player.x > mapWidth)
			player.x = mapWidth - 1;
		if (player.y > mapHeight)
			player.y = mapHeight - 1;

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

	public void travel() {
		Map map = getCurLoc();
		int x = player.x, y = player.y;
		Direction direction = player.stats.look;
		boolean canTravel = false;

		canTravel = (x == map.getWidth() - 1 && direction == Direction.RIGHT);
		canTravel |= (y == map.getHeight() - 1 && direction == Direction.UP);
		canTravel |= (x < 1 && direction == Direction.LEFT);
		canTravel |= (y < 1 && direction == Direction.DOWN);

		if (canTravel)
			switchLocation(direction);

	}

	public boolean inEncounter = false;

	private void switchToEncounter() {
		inEncounter = true;
		clearCurLoc();
		File encounterDir = new File(MadSand.SCRIPTDIR + MadSand.ENCOUNTERDIR);
		File[] files = encounterDir.listFiles();
		File encounterScript = files[Utils.rand(files.length)];

		LuaUtils.executeScript(MadSand.ENCOUNTERDIR + encounterScript.getName());
	}

	public boolean switchLocation(int layer) {
		return switchLocation(curxwpos, curywpos, layer);
	}

	public boolean descend() {
		if (curlayer >= DUNGEON_LAYER_MAX)
			return false;

		boolean ret = switchLocation(curlayer + 1);
		if (curlayer > (worldMap.getLayerCount(curxwpos, curywpos) - 1))
			worldMap.increaseLayerCount(curxwpos, curywpos);

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

	public boolean ascend(int layer) {
		if (layer <= LAYER_OVERWORLD)
			return false;
		boolean ret = switchLocation(layer);
		if (curlayer == LAYER_OVERWORLD)
			MadSand.print("You get back to surface level");
		else
			MadSand.print("You get back to dungeon level " + curlayer);
		Gui.overlay.processActionMenu();
		return ret;
	}

	public boolean ascend() {
		return ascend(curlayer - 1);
	}

	private void cleanUpPreviousLocations() {
		if (curlayer != LAYER_OVERWORLD)
			return;

		previousLocations.add(new MapID(new Pair(curxwpos, curywpos), LAYER_OVERWORLD));

		if (previousLocations.size() < MAX_PREVIOUS_LOCATIONS)
			return;

		Utils.out("Removing the oldest loaded sector...");
		MapID rootLocation = previousLocations.poll();
		removeLocation(rootLocation);
	}

	public void removeLocation(MapID mapId) {
		int layers = worldMap.getLayerCount(curxwpos, curywpos);
		for (int i = layers - 1; i >= 0; --i)
			worldMap.remove(mapId.setLayer(i));
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

	private static int TIME_NIGHT_START = 22;
	private static int TIME_NIGHT_END = 6;

	@JsonIgnore
	public boolean isNight() {
		boolean beforeMidnight = worldtime >= TIME_NIGHT_START && worldtime < TIME_MIDNIGHT;
		boolean afterMidnight = worldtime >= 0 && worldtime < TIME_NIGHT_END;

		return beforeMidnight || afterMidnight;
	}

	@JsonIgnore
	public boolean isDay() {
		return !isNight();
	}

	private static final int fovDelta = 5;
	private static int TIME_FOV_DECREASE_START = 18;// hour when the fov begins to decrease
	private static int TIME_FOV_DECREASE_END = TIME_FOV_DECREASE_START + fovDelta;
	private static int TIME_FOV_INCREASE_START = 4;// hour when the fov begins to decrease
	private static int TIME_FOV_INCREASE_END = TIME_FOV_INCREASE_START + fovDelta;
	private static int TIME_MIDNIGHT = 24;

	public void hourTick() {
		Map curLoc = getCurLoc();

		++worldtime;

		if (worldtime == TIME_MIDNIGHT)
			worldtime = 0;

		if (worldtime > TIME_FOV_DECREASE_START && worldtime <= TIME_FOV_DECREASE_END)
			player.setFov(player.fov - 1);

		if (worldtime > TIME_FOV_INCREASE_START && worldtime <= TIME_FOV_INCREASE_END)
			player.setFov(player.fov + 1);

		if (!inEncounter)
			curLoc.naturalRegeneration();

		curLoc.spawnMobs(!isNight()); // Autospawn hostile mobs at night / friendly mobs during the day

		MadSand.notice("Another hour passes...");
		MadSand.notice("It's " + worldtime + ":00");
	}

	private void tick() {
		player.stats.perTickCheck();
		player.tileDmg();
		getCurLoc().updateCrops();
		++globalTick;

		if (++tick >= ticksPerHour - 1) {
			tick = 0;
			hourTick();
		}

		updateNpcs();
	}

	private void updateNpcs() {
		Map loc = getCurLoc();
		HashMap<Pair, Npc> npcs = loc.getNpcs();
		ArrayList<Npc> queue = new ArrayList<Npc>();

		for (Entry<Pair, Npc> npc : npcs.entrySet())
			queue.add(npc.getValue());

		for (Npc npc : queue)
			npc.act();
	}

	public void updateLight() {
		getCurLoc().updateLight(player.x, player.y, player.fov);
	}

	public void ticks(int n) {
		for (int i = n; i > 0; --i)
			tick();
	}

	private void realtimeRefresh() {
		Map map = getCurLoc(LAYER_OVERWORLD);
		map.updateCrops();
		map.updateProductionStations();
	}

	private void realtimeTick() {
		++globalRealtimeTick;
		realtimeRefresh();
	}

	private void offlineReward(long offlineTime) {
		long offlineTicks = offlineTime / realtimeTickRate;
		globalRealtimeTick += offlineTicks;
		LuaUtils.executeScript(LuaUtils.offlineRewardScript, offlineTime);

		for (; offlineTicks > 0; --offlineTicks)
			realtimeRefresh();
	}

	private float HOUR = 3600;
	public void calcOfflineTime() {
		long offlineTime = Utils.now() - logoutTimeStamp;
		float offlineHours = offlineTime / HOUR;
		int maxHours = player.stats.skills.getLvl() + 1;

		if (offlineHours > maxHours)
			offlineTime = (long) (maxHours * HOUR);

		String offlineString = "You've been away for ";

		offlineString += Utils.timeString(offlineTime) + "." + Resources.LINEBREAK;
		offlineString += "Your maximum offline bonus is " + maxHours + " hours.";

		Gui.drawOkDialog(offlineString, Gui.overlay);

		offlineReward(offlineTime);
	}

	public void logout() {
		setLogoutTimeStamp(Utils.now());
	}

	@JsonIgnore
	public boolean isUnderGround() {
		return curlayer != World.LAYER_OVERWORLD;
	}

}