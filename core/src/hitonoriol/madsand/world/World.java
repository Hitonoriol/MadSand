package hitonoriol.madsand.world;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Timer;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.GameSaver;
import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.dialog.GameTextSubstitutor;
import hitonoriol.madsand.entities.Entity;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.npc.AbstractNpc;
import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.input.Keyboard;
import hitonoriol.madsand.input.Mouse;
import hitonoriol.madsand.lua.Lua;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.pathfinding.Graph;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.properties.WorldGenProp;
import hitonoriol.madsand.util.TimeUtils;
import hitonoriol.madsand.util.Utils;
import hitonoriol.madsand.world.worldgen.WorldGen;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class World {
	private static Map nullLoc = new Map(0, 0);
	public static int DEFAULT_MAPSIZE = Map.MIN_MAPSIZE;
	private static final float MAX_SIM_DISTANCE_COEF = 2.5f;
	private static final float ACT_DELAY_STEP = 0.00125f;
	public static final int DEFAULT_WORLDSIZE = 10;
	public static final int TILE_CAVE_EXIT = 25; //TODO move this to cave preset
	private final static int MAX_PREVIOUS_LOCATIONS = 3; // Max amount of maps allowed to be in WorldMap at the same time
	private static final float realtimeTickRate = 0.1f;

	private Pair coords = new Pair();

	@JsonIgnore
	private ArrayDeque<Pair> previousLocations = new ArrayDeque<>(); // Maps that are currently loaded in WorldMap

	@JsonIgnore
	private WorldGen worldGen;
	@JsonIgnore
	private WorldMapSaver worldMapSaver;

	private Player player;
	private WorldMap worldMap; // container of "Locations": maps grouped by world coords

	@JsonIgnore
	private Timer actDelayTimer;
	@JsonIgnore
	private Timer realTimeRefresher;
	private boolean timeSkip = false;
	private long realtimeActionPeriod = 10; // realtime ticks per 1 action tick
	private long globalRealtimeTick = 0; // global action tick counter, never resets

	private int ticksPerHour = 150; // ticks per one hourTick() trigger
	private int worldtime = 6; // time (00 - 23)
	private int tick = 0; // tick counter, resets every <ticksPerHour> ticks
	private long globalTick = 0; // global tick counter, never resets

	private MutableLong npcCounter = new MutableLong(0);
	private MutableLong itemCounter = new MutableLong(0);

	private long logoutTimeStamp;
	private HashMap<String, String> luaStorage = new HashMap<>();

	public World(int sz) {
		player = new Player();
		worldMap = new WorldMap(sz);
		initWorld();
	}

	public World() {
		this(DEFAULT_WORLDSIZE);
	}

	public void initWorld() {
		worldMapSaver = new WorldMapSaver(worldMap);
		worldGen = new WorldGen(worldMap);
		initRealtimeRefresher();
	}

	public void enter() {
		GameTextSubstitutor.add(GameTextSubstitutor.PLAYER_NAME, player.stats.name);
		getCurLoc().refreshGraph();

		if (!player.newlyCreated)
			calcOfflineTime();
	}

	public void close() {
		realTimeRefresher.clear();
		realTimeRefresher.stop();
		getCurLoc().close();
	}

	private void initRealtimeRefresher() {
		if (realTimeRefresher != null) {
			realTimeRefresher.clear();
			realTimeRefresher.stop();
		}

		realTimeRefresher = new Timer();
		realtimeSchedule(() -> actionTick());
		actDelayTimer = new Timer();
	}

	public void realtimeSchedule(Runnable task, long ticks) {
		TimeUtils.scheduleRepeatingTask(realTimeRefresher, task, ticksToTime(ticks));
	}

	public void realtimeSchedule(Runnable task) {
		realtimeSchedule(task, realtimeActionPeriod);
	}

	public static float ticksToTime(long realtimeTicks) {
		return (float) realtimeTicks * realtimeTickRate;
	}

	public long timeToActionTicks(long seconds) {
		return (long) (seconds / ticksToTime(realtimeActionPeriod));
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
			return nullLoc;
	}

	Map getLoc(int x, int y, int layer) {
		return getLoc(coords.set(x, y), layer);
	}

	public WorldMap getWorldMap() {
		return worldMap;
	}

	public long currentTick() {
		return globalTick;
	}

	public int getWorldTimeHour() {
		return worldtime;
	}

	public long currentActionTick() {
		return globalRealtimeTick;
	}

	@JsonIgnore
	public float getRealtimeActionSeconds() {
		return ticksToTime(realtimeActionPeriod);
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
		getCurLoc().refreshGraph();
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

	public boolean inEncounter = false;

	private void switchToEncounter() {
		inEncounter = true;
		clearCurLoc();

		Lua.executeScript(Resources.ENCOUNTER_DIR + Utils.randElement(WorldGenProp.encounters) + ".lua");
		getCurLoc().refreshGraph();
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

	public void delNpc(AbstractNpc npc) {
		getCurLoc().removeNpc(npc);
	}

	public void clearCurLoc() {
		Utils.dbg("Clearing current sector...");
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

	private void forEachEntity(Consumer<Entity> action) {
		Map loc = getCurLoc();
		List<Entity> queue = new ArrayList<Entity>();
		Graph graph = loc.getPathfindingGraph();

		graph.reIndex();
		queue.addAll(loc.getNpcs().values());
		queue.add(player);
		queue.forEach(entity -> entity.prepareToAct());
		Collections.sort(queue, Entity.speedComparator);
		queue.forEach(entity -> action.accept(entity));
		graph.reIndex();
	}

	/* Main time processor for all living creatures
	 * time - % of max AP player spent on their latest action
	 */
	public void timeSubtick(float time) {
		float maxSimDst = getMaxSimDistance();
		MutableFloat cumulativeDelay = new MutableFloat(0);
		MutableFloat maxDelay = new MutableFloat(0);
		MutableInt stopLevel = new MutableInt(0);

		forEachEntity(entity -> {
			if (timeSkip && entity.distanceTo(player) > maxSimDst)
				return;

			if (player.canSee(entity)) {
				boolean hostile = entity != player && !((AbstractNpc) entity).isNeutral();
				boolean actBeforePlayer = hostile && entity.getSpeed() >= player.getSpeed();

				if (actBeforePlayer)
					entity.speedUp(AbstractNpc.HOSTILE_SPEEDUP);

				float actDelay = entity.getAnimationDuration() + entity.getActDelay();

				if (entity != player) {
					actDelay += cumulativeDelay.getValue();
					if (actBeforePlayer) {
						maxDelay.setValue(Math.max(maxDelay.getValue(), actDelay));
						Keyboard.stopInput();
						stopLevel.increment();
						player.setActDelay(maxDelay.getValue());
					}

					cumulativeDelay.add(ACT_DELAY_STEP);
				}

				if (entity == player && !player.hasActDelay())
					actDelay = 0;

				TimeUtils.scheduleTask(actDelayTimer, () -> {
					entity.act(time);
					if (entity == player && player.hasActDelay())
						Keyboard.resumeInput(stopLevel.getValue());
				}, actDelay);
			} else
				entity.act(time);
		});

		TimeUtils.scheduleTask(() -> {
			Gui.overlay.refreshActionButton();

			if (timeSkip)
				endTimeSkip();
		}, maxDelay.getValue() + 0.01f);
	}

	public void updateLight() {
		getCurLoc().updateLight(player.x, player.y, player.fov);
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

	@JsonIgnore
	public boolean isUnderGround() {
		return worldMap.curLayer != Location.LAYER_OVERWORLD;
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
		return (int) (player.fov * MAX_SIM_DISTANCE_COEF);
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

	public MutableLong npcCounter() {
		return npcCounter;
	}

	public MutableLong itemCounter() {
		return itemCounter;
	}
}