package hitonoriol.madsand.world;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.function.Consumer;

import org.apache.commons.lang3.mutable.MutableLong;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Timer;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.GameSaver;
import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.Entity;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.npc.AbstractNpc;
import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.input.Mouse;
import hitonoriol.madsand.lua.Lua;
import hitonoriol.madsand.map.LightEngine;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.MapEntity;
import hitonoriol.madsand.pathfinding.Graph;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.properties.WorldGenProp;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.TimeUtils;
import hitonoriol.madsand.util.Utils;
import hitonoriol.madsand.world.time.EntityTimeProcessor;
import hitonoriol.madsand.world.worldgen.WorldGen;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class World {
	public static int DEFAULT_MAPSIZE = Map.MIN_MAPSIZE;
	private static final float MAX_SIM_DISTANCE_COEF = 2.5f;
	public static final int DEFAULT_WORLDSIZE = 10;
	public static final int TILE_CAVE_EXIT = 25; //TODO move this to cave preset
	private final static int MAX_PREVIOUS_LOCATIONS = 3; // Max amount of maps allowed to be in WorldMap at the same time
	private static final float realtimeTickRate = 0.1f;

	private Pair coords = new Pair();

	@JsonIgnore
	private ArrayDeque<Pair> previousLocations = new ArrayDeque<>(); // Maps that are currently loaded in WorldMap

	@JsonIgnore
	private WorldGen worldGen = new WorldGen();
	@JsonIgnore
	private WorldMapSaver worldMapSaver = new WorldMapSaver();

	@JsonIgnore
	private EntityTimeProcessor entityEvents = new EntityTimeProcessor(this);

	private Player player;
	private WorldMap worldMap; // container of "Locations": maps grouped by world coords

	@JsonIgnore
	private Timer realTimeRefresher;
	private boolean timeSkip = false;
	private long realtimeActionPeriod = 10; // realtime ticks per 1 action tick
	private long globalRealtimeTick = 0; // global action tick counter, never resets

	private int ticksPerHour = 150; // ticks per one hourTick() trigger
	private int worldtime = 6; // time (00 - 23)
	private int tick = 0; // tick counter, resets every <ticksPerHour> ticks
	private long globalTick = 0; // global tick counter, never resets

	private boolean inEncounter = false;

	private MutableLong entityCounter = new MutableLong(Entity.PLAYER_UID);
	private MutableLong itemCounter = new MutableLong(0);

	private long logoutTimeStamp;
	private HashMap<String, String> luaStorage = new HashMap<>();

	public World(int sz) {
		player = new Player();
		setWorldMap(new WorldMap(sz));
	}

	public World() {
		this(DEFAULT_WORLDSIZE);
	}

	public void enter() {
		getCurLoc().refreshPathfindingGraph();
		initRealtimeRefresher();
		if (!player.uninitialized())
			calcOfflineTime();
	}

	public void close() {
		if (realTimeRefresher != null) {
			realTimeRefresher.clear();
			realTimeRefresher.stop();
		}
		getCurLoc().close();
	}

	private void initRealtimeRefresher() {
		if (realTimeRefresher != null) {
			realTimeRefresher.clear();
			realTimeRefresher.stop();
		}

		realTimeRefresher = new Timer();
		realtimeSchedule(() -> actionTick());
	}

	public void realtimeSchedule(Runnable task, long ticks) {
		TimeUtils.scheduleRepeatingTask(realTimeRefresher, task, actionTicksToTime(ticks));
	}

	public void realtimeSchedule(Runnable task) {
		realtimeSchedule(task, realtimeActionPeriod);
	}

	public float actionTicksToTime(long realtimeTicks) {
		return (float) realtimeTicks * realtimeTickRate;
	}

	public long timeToActionTicks(long seconds) {
		return (long) (seconds / actionTicksToTime(realtimeActionPeriod));
	}

	public int ticksPerHour() {
		return ticksPerHour;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
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
			return Map.nullMap;
	}

	Map getLoc(int x, int y, int layer) {
		return getLoc(coords.set(x, y), layer);
	}

	public void setWorldMap(WorldMap worldMap) {
		this.worldMap = worldMap;
		worldGen.setWorldMap(worldMap);
		worldMapSaver.setWorldMap(worldMap);
	}

	public WorldMap getWorldMap() {
		return worldMap;
	}

	public long currentTick() {
		return globalTick;
	}

	public int currentTimeTick() {
		return tick;
	}

	public int getWorldTimeHour() {
		return worldtime;
	}

	public long currentActionTick() {
		return globalRealtimeTick;
	}

	@JsonIgnore
	public float getRealtimeActionSeconds() {
		return actionTicksToTime(realtimeActionPeriod);
	}

	public long getRealtimeActionPeriod() {
		return realtimeActionPeriod;
	}

	public void setRealtimeActionPeriod(long ticks) {
		realtimeActionPeriod = ticks;
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

	public void exec(Consumer<Map> action) {
		action.accept(getCurLoc());
	}

	@JsonIgnore
	public Map getOverworld() {
		return getLoc(worldMap.curWorldPos, Location.LAYER_OVERWORLD);
	}

	Map getCurLoc(int layer) {
		return getLoc(worldMap.curWorldPos, layer);
	}

	@JsonIgnore
	public int getLocBiome() {
		return getLocation().biome;
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
		return createLoc(wc, layer, new Map(DEFAULT_MAPSIZE, DEFAULT_MAPSIZE));
	}

	boolean createBasicLoc(Pair wc, int mx, int my) {
		return createLoc(wc, Location.LAYER_OVERWORLD, new Map(mx, my));
	}

	boolean createBasicLoc(int wx, int wy) {
		return createBasicLoc(new Pair(wx, wy), DEFAULT_MAPSIZE, DEFAULT_MAPSIZE);
	}

	boolean createBasicLoc(int layer) {
		return createLoc(new Pair(worldMap.curWorldPos), layer, new Map(DEFAULT_MAPSIZE, DEFAULT_MAPSIZE));
	}

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
		String locationScriptPath = Lua.getSectorScriptPath(coords.x, coords.y);

		if (!Gdx.files.internal(Resources.SCRIPT_DIR + locationScriptPath).exists())
			return false;

		Lua.executeScript(locationScriptPath);

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
			getCurLoc().getTimeScheduler().resume();
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
		getCurLoc().refreshPathfindingGraph();
		return true;
	}

	public boolean switchLocation(Direction dir) {
		Utils.out("Switch location in direction " + dir);

		if (!inEncounter)
			GameSaver.save();
		else {
			Utils.dbg("Removing encounter location");
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

		player.inventory.delItem(Globals.values().travelItem);

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
		if (!player.canTravel())
			return;

		Direction direction = player.stats.look;
		int travelItem = Globals.values().travelItem;
		Pair nextSector = coords.set(worldMap.wx(), worldMap.wy()).addDirection(direction);

		if (!GameSaver.verifyNextSector(nextSector.x, nextSector.y))
			if (!player.hasItem(travelItem)) {
				Gui.drawOkDialog(
						"You need at least 1 " + ItemProp.getItemName(travelItem)
								+ " to travel to the next sector.");
				return;
			}

		getCurLoc().close();
		switchLocation(direction);
		Gui.refreshOverlay();
	}

	private void switchToEncounter() {
		inEncounter = true;
		clearCurLoc();

		Lua.executeScript(Resources.ENCOUNTER_DIR + Utils.randElement(WorldGenProp.encounters) + ".lua");
		getCurLoc().refreshPathfindingGraph();
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
			Gui.overlay.refreshActionButton();
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

		Gui.overlay.refreshActionButton();
		return ret;
	}

	public boolean ascend() {
		return ascend(worldMap.curLayer - 1);
	}

	public void returnToSurface() {
		if (worldMap.getLayerType() == Location.Layer.Dungeon) {
			if (player.completeDungeon()) {
				MadSand.notice("Total dungeons completed: %d", player.dungeonsCompleted());
				MadSand.notice("Congratulations! You completed this dungeon for the first time!");
			}
		}

		ascend(Location.LAYER_OVERWORLD);
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
		return getCurLoc().getTile(x, y).id();
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

	public void delNpc(AbstractNpc npc) {
		getCurLoc().removeNpc(npc);
	}

	public void clearCurLoc() {
		Utils.dbg("Clearing current sector...");
		getCurLoc().purge();
	}

	public TextureRegion getTileOrDefault(int x, int y) {
		Map map = getCurLoc();
		return Resources.getTile(map.validCoords(x, y)
				? map.getTile(x, y).id()
				: getDefaultTile());
	}

	private static int H_DAY = 24;

	public int toWorldTimeSeconds(long ticks) {
		return (int) (((float) ticks / (float) ticksPerHour) * Utils.S_HOUR);
	}

	@JsonIgnore
	public int getWorldTimeMinute() {
		return toWorldTimeSeconds(tick) / Utils.S_MINUTE;
	}

	@JsonIgnore
	public int getWorldTimeDay() {
		return (int) (globalTick / ticksPerHour) / H_DAY;
	}

	public static int worldCoord(int q) {
		return q * MadSand.TILESIZE;
	}

	private static int TIME_MIDNIGHT = 24;
	private static int NIGHT_START = 22, NIGHT_END = 6, SUNRISE_START = 3;
	/* Period in minutes with which sky light level increases by one during the sunrise */
	private static final double SUNRISE_STEP = 25d;

	@JsonIgnore
	public boolean isNight() {
		boolean beforeMidnight = worldtime >= NIGHT_START && worldtime < TIME_MIDNIGHT;
		boolean afterMidnight = worldtime >= 0 && worldtime < NIGHT_END;

		return beforeMidnight || afterMidnight;
	}

	@JsonIgnore
	public boolean isDay() {
		return !isNight();
	}

	private final static int UNDERGROUND_LIGHT = -5;

	public int getSkyLight() {
		if (isUnderGround())
			return UNDERGROUND_LIGHT;

		int hour = getWorldTimeHour();
		if (isDay())
			return (hour - H_DAY) - 1;
		else {
			int light = NIGHT_START - H_DAY;

			/* Light level doesn't change until sunrise */
			if (hour < NIGHT_START && hour >= SUNRISE_START) {
				int minsSinceSunrise = ((hour - SUNRISE_START) * Utils.M_HOUR) + getWorldTimeMinute();
				light = (int) Math.floor(light - (minsSinceSunrise / SUNRISE_STEP));
			}
			return light;
		}
	}

	public void hourTick() {
		Map curLoc = getCurLoc();

		++worldtime;

		if (worldtime == TIME_MIDNIGHT)
			worldtime = 0;

		if (!inEncounter)
			curLoc.naturalRegeneration();

		curLoc.spawnMobs(!player.stats.luckRoll() || isNight());

		Utils.dbg("Sky light is now %d", getSkyLight());
		MadSand.notice("Another hour passes...");
		MadSand.notice("It's " + worldtime + ":00");
	}

	public void skipToNextHour() {
		timeTick(ticksPerHour() - currentTimeTick());
	}

	public void timeTick(int n) {
		for (int i = n; i > 0; --i)
			timeTick();
	}

	/* Main world time processor (for "whole" time ticks)
	 * Gets called once for every 100% of AP spent by player
	 */
	private void timeTick() {
		Graph graph = getCurLoc().getPathfindingGraph();
		graph.reIndex();
		player.stats.perTickCheck();
		player.tileDmg();
		++globalTick;

		if (++tick >= ticksPerHour - 1) {
			tick = 0;
			hourTick();
		}

		graph.reIndex();
		showTips();
	}

	private long lastTip = 0;

	private void showTips() {
		long ticksPassed = globalTick - lastTip;
		if (ticksPassed < ticksPerHour / 3 || !Utils.percentRoll((double) ticksPassed / 50d))
			return;

		lastTip = globalTick;
		MadSand.warn("[Tip] " + Utils.randElement(Globals.values().tips));
	}

	/* Main time processor for all living creatures
	 *   `time` - % of max AP player spent on their latest action
	 */
	public void timeSubtick(float time) {
		Map loc = getCurLoc();
		Graph graph = loc.getPathfindingGraph();
		graph.reIndex();
		entityEvents.processEntityActions(time);
		graph.reIndex();
	}

	public void updateLight() {
		Map map = getCurLoc();
		LightEngine light = map.getLightEngine();
		Pair coords = new Pair();
		light.begin();
		light.update(player);
		player.forEachInFov((x, y) -> {
			MapEntity entity = map.getMapEntity(coords.set(x, y));
			if (entity.isEmpty() || !entity.emitsLight())
				return;

			light.update(entity);
		});
		light.end();
	}

	private void actionTick() {
		++globalRealtimeTick;
		Location loc = getLocation();

		if (loc.isSettlement())
			loc.getSettlement().timeTick();

		Mouse.refreshTooltip();
	}

	public void skipActionTicks(long ticks) {
		for (; ticks > 0; --ticks)
			actionTick();
	}

	private void offlineReward(long offlineSeconds) {
		long offlineTicks = timeToActionTicks(offlineSeconds);
		globalRealtimeTick += offlineTicks;
		Lua.executeScript(Lua.offlineRewardScript, offlineSeconds);
		skipActionTicks(offlineTicks);
	}

	private static float HOUR = 3600;

	public void calcOfflineTime() {
		long offlineTime = Utils.now() - logoutTimeStamp;
		float offlineHours = offlineTime / HOUR;
		int maxHours = player.stats.skills.getLvl() + 1;

		if (offlineHours > maxHours)
			offlineTime = (long) (maxHours * HOUR);

		String offlineString = "You've been away for ";

		offlineString += Utils.timeString(offlineTime) + "." + Resources.LINEBREAK;
		offlineString += "Your maximum offline bonus is " + maxHours + " hours.";

		Gui.drawOkDialog(offlineString);

		offlineReward(offlineTime);
	}

	public void logout() {
		setLogoutTimeStamp(Utils.now());
	}

	public void finishPlayerCreation() {
		if (!player.uninitialized())
			return;

		player.finishCreation();
		Gui.refreshOverlay();
		Lua.executeScript(Lua.onCreationScript);
	}

	@JsonIgnore
	public boolean isUnderGround() {
		return worldMap.curLayer != Location.LAYER_OVERWORLD;
	}

	public boolean inEncounter() {
		return inEncounter;
	}

	public void startTimeSkip() {
		timeSkip = true;
	}

	public void endTimeSkip() {
		timeSkip = false;
	}

	public boolean timeSkipInProgress() {
		return timeSkip;
	}

	@JsonIgnore
	public int getMaxSimDistance() {
		return (int) (player.getFov() * MAX_SIM_DISTANCE_COEF);
	}

	@JsonIgnore
	public void setStorageValue(String name, String value) {
		luaStorage.put(name, value);
	}

	@JsonIgnore
	public String getStorageValue(String name) {
		return luaStorage.getOrDefault(name, "");
	}

	@JsonIgnore
	public WorldMapSaver getMapSaver() {
		return worldMapSaver;
	}

	@JsonIgnore
	public WorldGen getWorldGenerator() {
		return worldGen;
	}

	public long nextEntityUID() {
		return entityCounter.incrementAndGet();
	}

	public long nextItemUID() {
		return itemCounter.incrementAndGet();
	}
}