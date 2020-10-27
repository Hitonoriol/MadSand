package hitonoriol.madsand.world;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

import com.badlogic.gdx.utils.Timer;
import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.GameSaver;
import hitonoriol.madsand.Gui;
import hitonoriol.madsand.Keyboard;
import hitonoriol.madsand.LuaUtils;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.Entity;
import hitonoriol.madsand.entities.Npc;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.enums.NpcState;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.world.worldgen.WorldGen;

public class World {
	Map nullLoc = new Map(0, 0);
	private static final float ACT_DELAY = 0.1f;
	public static final int DEFAULT_WORLDSIZE = 10;
	public static final int TILE_CAVE_EXIT = 25; //TODO move this to cave preset

	private Pair coords = new Pair();

	private int MAX_PREVIOUS_LOCATIONS = 2; // Max amount of maps allowed to be in WorldMap at the same time
	private ArrayDeque<Pair> previousLocations = new ArrayDeque<>(); // Maps that are currently loaded in WorldMap

	public static final int BORDER = 1;// map border - old shit, not really useful anymore

	public static int DEFAULT_MAPSIZE = Map.MIN_MAPSIZE;

	@JsonIgnore
	public WorldGen worldGen;
	@JsonIgnore
	public WorldMapSaver worldMapSaver;

	@JsonIgnore
	public static Player player;

	public WorldMap worldMap; // container of "Locations": maps grouped by world coords

	private Timer actDelayTimer;
	private Timer realTimeRefresher;
	public int realtimeTickRate = 5; // seconds per 1 tick
	public long globalRealtimeTick = 0; // global realtime tick counter, never resets

	public int ticksPerHour = 150; // ticks per one hourTick() trigger
	public int worldtime = 6; // time (00 - 23)
	public int tick = 0; // tick counter, resets every <ticksPerHour> ticks
	public long globalTick = 0; // global tick counter, never resets
	public long npcCounter = 0;

	private long logoutTimeStamp;
	public HashMap<String, String> luaStorage = new HashMap<>();

	public World(int sz) {
		player = new Player();
		worldMap = new WorldMap(sz);
		initWorld();

		initRealtimeRefresher();
	}

	public World() {
		this(DEFAULT_WORLDSIZE);
	}

	public void initWorld() {
		worldMapSaver = new WorldMapSaver(worldMap);
		worldGen = new WorldGen(worldMap);
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

		actDelayTimer = new Timer();
	}

	public long getLogoutTimeStamp() {
		return logoutTimeStamp;
	}

	public void setLogoutTimeStamp(long logoutTimeStamp) {
		this.logoutTimeStamp = logoutTimeStamp;
	}

	boolean locExists(Pair loc) {
		return worldMap.hasLocation(loc);
	}

	boolean locExists(Pair loc, int layer) {
		if (!locExists(loc))
			return false;

		return worldMap.getLocation(loc).hasLayer(layer);
	}

	boolean createLoc(Pair loc, int layer, Map map) {
		if (!locExists(loc, layer)) {
			this.worldMap.addMap(loc, layer, map);
			return true;
		} else
			return false;
	}

	Map getLoc(Pair wc, int layer) {
		if (locExists(wc, layer)) {
			return worldMap.getLocation(wc).getLayer(layer);
		} else
			return nullLoc;
	}

	Map getLoc(int x, int y, int layer) {
		return getLoc(coords.set(x, y), layer);
	}

	public int wx() {
		return worldMap.wx();
	}

	public int wy() {
		return worldMap.wy();
	}

	public int curLayer() {
		return worldMap.curLayer;
	}

	public void setLayer(int layer) {
		worldMap.curLayer = layer;
	}

	@JsonIgnore
	public Pair getCurWPos() {
		return worldMap.curWorldPos;
	}

	@JsonIgnore
	public Location getLocation() {
		return worldMap.getLocation();
	}

	@JsonIgnore
	public Map getCurLoc() {
		return getLoc(worldMap.curWorldPos, worldMap.curLayer);
	}

	@JsonIgnore
	public Map getOverworld() {
		return getLoc(worldMap.curWorldPos, Location.LAYER_OVERWORLD);
	}

	Map getCurLoc(int layer) {
		return getLoc(worldMap.curWorldPos, layer);
	}

	int getLocBiome() {
		return getCurLoc(Location.LAYER_OVERWORLD).getBiome();
	}

	int getDefaultTile() {
		return getCurLoc().defTile;
	}

	Map putLoc(Pair wc, int layer, Map loc) {
		worldMap.addMap(wc, layer, loc);
		return loc;
	}

	Map putLoc(int x, int y, int layer, Map loc) {
		return putLoc(new Pair(x, y), layer, loc);
	}

	boolean createBasicLoc(Pair wc, int layer) {
		return this.createLoc(wc, layer, new Map(DEFAULT_MAPSIZE, DEFAULT_MAPSIZE));
	}

	boolean createBasicLoc(Pair wc, int mx, int my) {
		return this.createLoc(wc, Location.LAYER_OVERWORLD, new Map(mx, my));
	}

	boolean createBasicLoc(int wx, int wy) {
		return createBasicLoc(new Pair(wx, wy), DEFAULT_MAPSIZE, DEFAULT_MAPSIZE);
	}

	boolean createBasicLoc(int layer) {
		return createLoc(new Pair(worldMap.curWorldPos), layer, new Map(DEFAULT_MAPSIZE, DEFAULT_MAPSIZE));
	}

	int biome;

	public void generate(int wx, int wy, int layer) {
		Utils.out("Generating new sector!");

		if (!locExists(coords.set(wx, wy), layer))
			createBasicLoc(new Pair(wx, wy), layer);

		clearCurLoc();

		if (!executeLocationScript())
			worldGen.initPosition(coords.set(wx, wy), layer).generate();

		Utils.out("Done generating new sector!");
	}

	private boolean executeLocationScript() {

		if (worldMap.curLayer != Location.LAYER_OVERWORLD)
			return false;

		Pair coords = worldMap.curWorldPos;
		String locationScriptPath = LuaUtils.getSectorScriptPath(coords.x, coords.y);
		File locationScript = new File(MadSand.SCRIPTDIR + locationScriptPath);

		if (!locationScript.exists())
			return false;

		LuaUtils.executeScript(locationScriptPath);

		return true;
	}

	public void generate(int layer) {
		if (layer != Location.LAYER_OVERWORLD)
			createBasicLoc(layer);
		generate(worldMap.wx(), worldMap.wx(), layer);
		updateLight();
	}

	public void generate() {
		generate(worldMap.wx(), worldMap.wy(), Location.LAYER_OVERWORLD);
		updateLight();
	}

	public boolean switchLocation(int x, int y, int layer) {
		int prevX = worldMap.wx(), prevY = worldMap.wy();
		Utils.out("Switching location to " + x + ", " + y + " layer: " + layer);
		if (layer > Location.LAYER_MAX)
			return false;

		worldMap.jumpToLocation(x, y, layer);

		if (locExists(coords.set(x, y), layer)) {
			Utils.out("This sector already exists! Noice.");
			updateLight();
			return true;
		}

		clearCurLoc();

		if (x == prevX && y == prevY && worldMap.curLayer != Location.LAYER_OVERWORLD)
			generate(layer);
		else if (GameSaver.verifyNextSector(x, y))
			GameSaver.loadLocation();
		else
			generate();

		cleanUpPreviousLocations();

		player.updCoords();
		updateLight();
		return true;
	}

	public boolean switchLocation(Direction dir) {
		Utils.out("Switch location in direction " + dir);

		if (!inEncounter)
			GameSaver.saveWorld();
		else {
			Utils.out("Removing encounter location");
			worldMap.remove();
		}

		MadSand.switchScreen(Gui.travelScreen);

		if (player.stats.rollEncounter() && !inEncounter) {
			switchToEncounter();
			return true;
		}

		if (inEncounter)
			inEncounter = false;

		if (worldMap.curLayer != Location.LAYER_OVERWORLD)
			return false;

		player.inventory.delItem(Globals.getInt(Globals.TRAVEL_ITEM));

		coords.set(worldMap.wx(), worldMap.wy()).addDirection(dir);
		MadSand.print("You travel to sector (" + coords.x + ", " + coords.y + ")");

		if (!switchLocation(coords.x, coords.y, Location.LAYER_OVERWORLD)) {
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
		Direction direction = player.stats.look;

		if (!player.canTravel())
			return;

		int travelItem = Globals.getInt(Globals.TRAVEL_ITEM);
		Pair nextSector = coords.set(worldMap.wx(), worldMap.wy()).addDirection(direction);

		if (!GameSaver.verifyNextSector(nextSector.x, nextSector.y))
			if (!player.hasItem(travelItem)) {
				Gui.drawOkDialog(
						"You need at least 1 " + ItemProp.getItemName(travelItem)
								+ " to travel to the next sector.",
						Gui.overlay);
				return;
			}

		switchLocation(direction);
		Gui.refreshOverlay();
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
		return switchLocation(worldMap.wx(), worldMap.wy(), layer);
	}

	public boolean descend(int layer) {
		if (worldMap.curLayer >= Location.LAYER_MAX)
			return false;

		boolean ret = switchLocation(layer);

		Map loc = getCurLoc();
		String place = null;

		if (!loc.spawnPoint.equals(Pair.nullPair)) { // this means we are in the dungeon
			player.teleport(loc.spawnPoint.x, loc.spawnPoint.y);
			place = "dungeon";
		} else { // this means we are in the cave
			delObj(player.x, player.y);
			putMapTile(player.x, player.y, TILE_CAVE_EXIT);
			Gui.overlay.processActionMenu();
			updateLight();
			place = "cave";
		}

		MadSand.print("You descend to " + place + " level " + worldMap.getCurLocationLayer());
		return ret;
	}

	public boolean descend() {
		return descend(worldMap.curLayer + 1);
	}

	public boolean ascend(int layer) {
		if (layer < Location.LAYER_OVERWORLD)
			return false;

		Utils.out("Ascending to " + layer);

		boolean ret = switchLocation(layer);

		if (worldMap.curLayer == Location.LAYER_OVERWORLD)
			MadSand.print("You get back to surface level");
		else
			MadSand.print("You get back to dungeon level " + worldMap.curLayer);

		Gui.overlay.processActionMenu();
		return ret;
	}

	public boolean ascend() {
		return ascend(worldMap.curLayer - 1);
	}

	private void cleanUpPreviousLocations() {
		if (worldMap.curLayer != Location.LAYER_OVERWORLD)
			return;

		previousLocations.add(new Pair(worldMap.curWorldPos));

		if (previousLocations.size() < MAX_PREVIOUS_LOCATIONS)
			return;

		Utils.out("Removing the oldest loaded location...");
		Pair rootLocation = previousLocations.poll();
		worldMap.remove(rootLocation);
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
		getCurLoc().delObject(x, y);
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

	private float M_HOUR = 60;
	private int H_DAY = 24;

	@JsonIgnore
	public int getWorldTimeMinute() {
		return (int) (M_HOUR * ((float) tick / (float) ticksPerHour));
	}

	@JsonIgnore
	public int getWorldTimeDay() {
		return (int) (globalTick / ticksPerHour) / H_DAY;
	}

	public static int worldCoord(int q) {
		return q * MadSand.TILESIZE;
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

		curLoc.spawnMobs(!player.stats.luckRoll() || isNight());

		MadSand.notice("Another hour passes...");
		MadSand.notice("It's " + worldtime + ":00");
	}

	public void timeTick(int n) {
		for (int i = n; i > 0; --i)
			timeTick();
	}

	private void timeTick() { // Called every whole tick player spends
		player.stats.perTickCheck();
		player.tileDmg();
		++globalTick;

		if (++tick >= ticksPerHour - 1) {
			tick = 0;
			hourTick();
		}
	}

	public void timeSubtick(float time) { // Gets called on every action player does, time = % of max AP(speed) 
		Map loc = getCurLoc();
		HashMap<Pair, Npc> npcs = loc.getNpcs();
		ArrayList<Entity> queue = new ArrayList<Entity>();

		for (Entry<Pair, Npc> npc : npcs.entrySet())
			queue.add(npc.getValue());

		queue.add(player);

		Collections.sort(queue, Entity.speedComparator);

		boolean pausePlayer = false;	// if a hostile mob acts before player, we pause player until the action is completed
		boolean hostile;
		for (Entity entity : queue) {

			if ((player.canSee(entity) && entity != player) || (entity == player && pausePlayer)) {
				hostile = false;
				if (entity != player) {
					hostile = ((Npc) entity).state == NpcState.FollowPlayer;
					pausePlayer |= hostile;
					
					if (pausePlayer)
						Keyboard.stopInput();
				}

				actDelayTimer.scheduleTask(new Timer.Task() {
					@Override
					public void run() {
						entity.act(time);
						if (!entity.isStepping())
							Keyboard.resumeInput();
					}
				}, (hostile && pausePlayer) ? 0 : ACT_DELAY);
			} else
				entity.act(time);
		}
	}

	public void updateLight() {
		getCurLoc().updateLight(player.x, player.y, player.fov);
	}

	private void realtimeRefresh() {
		Map map = getCurLoc(Location.LAYER_OVERWORLD);
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
		return worldMap.curLayer != Location.LAYER_OVERWORLD;
	}

	@JsonIgnore
	public void setStorageValue(String name, String value) {
		luaStorage.remove(name);
		luaStorage.put(name, value);
	}

	@JsonIgnore
	public String getStorageValue(String name) {
		return luaStorage.getOrDefault(name, "");
	}

}