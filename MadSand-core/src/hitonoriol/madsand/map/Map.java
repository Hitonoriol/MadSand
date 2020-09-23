package hitonoriol.madsand.map;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.containers.Line;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.Npc;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.properties.ObjectProp;
import hitonoriol.madsand.properties.TileProp;
import hitonoriol.madsand.properties.WorldGenProp;
import hitonoriol.madsand.world.World;
import hitonoriol.madsand.world.worldgen.OverworldPreset;
import hitonoriol.madsand.world.worldgen.RollList;
import hitonoriol.madsand.world.worldgen.WorldGenPreset;

public class Map {
	private int xsz, ysz;

	public static int PLOWED_SOIL = 15;

	public static int MIN_MAPSIZE = 100;
	public static int MAX_MAPSIZE = 200;

	private int biome = -1;
	public int defTile = 0;
	public int defObject = 0;

	public boolean editable = true;

	public Pair spawnPoint = Pair.nullPair; // for dungeon levels only

	static final int COLLISION_MASK_ID = 666;
	public static Tile nullTile = new Tile();
	public static MapObject nullObject = new MapObject();
	public static Loot nullLoot = new Loot();
	public static Crop nullCrop = new Crop();
	public static Npc nullNpc = new Npc();

	private HashMap<Pair, Tile> mapTiles;
	private HashMap<Pair, MapObject> mapObjects;
	private HashMap<Pair, Loot> mapLoot;
	private HashMap<Pair, Crop> mapCrops;
	private HashMap<Pair, Npc> mapNpcs;

	Pair coords = new Pair(0, 0);

	public Map(int xsz, int ysz) {
		this.xsz = xsz;
		this.ysz = ysz;
		purge();
	}

	public Map() {
		rollSize();
		purge();
	}

	public void rollSize(int min, int max) {
		this.setSize(Utils.rand(min, max),
				Utils.rand(min, max));
	}

	public void rollSize() {
		rollSize(MIN_MAPSIZE, MAX_MAPSIZE);
	}

	public int getBiome() {
		return biome;
	}

	public void setBiome(int val) {
		biome = val;
	}

	public HashMap<Pair, Npc> getNpcs() {
		return mapNpcs;
	}

	void setNpcs(HashMap<Pair, Npc> npcs) {
		mapNpcs = npcs;
	}

	HashMap<Pair, Tile> getTiles() {
		return mapTiles;
	}

	HashMap<Pair, MapObject> getObjects() {
		return mapObjects;
	}

	void setTiles(HashMap<Pair, Tile> tiles) {
		mapTiles = tiles;
	}

	void setObjects(HashMap<Pair, MapObject> objects) {
		mapObjects = objects;
	}

	void setSize(int xsz, int ysz) {
		this.xsz = xsz;
		this.ysz = ysz;
	}

	public int getWidth() {
		return xsz;
	}

	public int getHeight() {
		return ysz;
	}

	public int getObjectCount() {
		return mapObjects.size();
	}

	public int getNpcCount() {
		return mapNpcs.size();
	}

	public Pair getRandomPoint(int distanceFromPlayer) {
		Player player = World.player;
		int x = player.x, y = player.y;

		while (Line.calcDistance(x, y, player.x, player.y) < distanceFromPlayer) {
			x = Utils.rand(xsz);
			y = Utils.rand(ysz);
		}

		return coords.set(x, y);
	}

	public Map purge() {
		mapTiles = new HashMap<Pair, Tile>();
		mapObjects = new HashMap<Pair, MapObject>();
		mapLoot = new HashMap<Pair, Loot>();
		mapNpcs = new HashMap<Pair, Npc>();
		mapCrops = new HashMap<Pair, Crop>();
		return this;
	}

	void putTileInDir(int x, int y, Direction dir, int id) {
		this.addTile(x, y, dir, id);
	}

	@FunctionalInterface
	private interface MapAction {
		public boolean changeMap(int x, int y, int id);
	}

	private MapAction tileAction = (int x, int y, int id) -> {
		return addTile(x, y, id, true);
	};

	private MapAction objectAction = (int x, int y, int id) -> {
		return addObject(x, y, id, true);
	};

	private Map drawRect(MapAction action, int x, int y, int w, int h, int id, boolean fill) {
		int rectWidth = x + w, rectHeight = y + h;
		int startX = x, startY = y;

		for (; x <= rectWidth; ++x) {
			for (y = startY; y <= rectHeight; ++y)
				if (fill || (x == startX || y == startY || x == rectWidth || y == rectHeight))
					action.changeMap(x, y, id);
		}

		return this;
	}

	public Map drawTileRectangle(int x, int y, int w, int h, int id) {
		return drawRect(tileAction, x, y, w, h, id, false);
	}

	public Map drawObjectRectangle(int x, int y, int w, int h, int id) {
		return drawRect(objectAction, x, y, w, h, id, false);
	}

	public Map fillTile(int x, int y, int w, int h, int id) {
		return drawRect(tileAction, x, y, w, h, id, true);
	}

	public Map fillTile(int id) {
		return fillTile(0, 0, xsz, ysz, id);
	}

	public Map fillTile() {
		return fillTile(defTile);
	}

	public Map fillObject(int x, int y, int w, int h, int id) {
		return drawRect(objectAction, x, y, w, h, id, true);
	}

	public Map fillObject(int id) {
		return fillObject(0, 0, xsz, ysz, id);
	}

	public Map fillObject() {
		return fillObject(defObject);
	}

	private boolean correctCoords(Pair coords) {
		if (coords.x <= xsz && coords.y <= ysz && coords.x >= 0 && coords.y >= 0)
			return true;
		else
			return false;
	}

	private boolean addTile(Pair coords, Tile tile) {
		return mapTiles.put(coords, tile) == null;
	}

	public boolean addTile(int x, int y, int id, boolean force) {
		if (!correctCoords(coords.set(x, y)))
			return false;

		if (force)
			mapTiles.remove(coords);

		return addTile(new Pair(coords), new Tile(id));
	}

	private boolean delTile(Pair coords) {
		boolean removed = mapTiles.remove(coords) != null;
		mapTiles.put(coords, new Tile(defTile));
		return removed;
	}

	public boolean delTile(int x, int y) {
		return delTile(coords.set(x, y));
	}

	public boolean addTile(int x, int y, int id) {
		return addTile(x, y, id, false);
	}

	public boolean addTile(int x, int y, Direction dir, int id) {
		if (correctCoords(coords.set(x, y))) {
			coords.addDirection(dir);
			addTile(coords.x, coords.y, id, false);
			return true;
		} else
			return false;
	}

	public Tile getTile(int x, int y) {
		if (correctCoords(coords.set(x, y))) {
			Tile ret = mapTiles.get(coords);
			if (ret == null)
				ret = nullTile;
			return ret;
		} else
			return nullTile;
	}

	Tile getTile(int x, int y, Direction dir) {
		coords.set(x, y).addDirection(dir);
		return getTile(coords.x, coords.y);
	}

	void setObjectSize(int x, int y, int id) { // If object is bigger than 1x1, fill the rest of the space with COLLISION_MASK_ID
		MapObject objectProp = ObjectProp.getObject(id);
		int i = objectProp.maskHeight;
		if (y + 1 < ysz - 1) {
			while (i > 0) {
				if (y + i < ysz) {
					addObject(x, y + i, COLLISION_MASK_ID);
				}
				i--;
			}
		}

		i = objectProp.maskWidth;
		if (x + 1 < xsz - 1) {
			while (i > 0) {
				if (x + i < xsz) {
					addObject(x + i, y, COLLISION_MASK_ID);
				}
				i--;
			}
		}
	}

	private boolean delObject(Pair coords) {
		return mapObjects.remove(coords) != null;
	}

	public boolean delObject(int x, int y) {
		if (correctCoords(coords.set(x, y)))
			return delObject(coords);

		return false;
	}

	private boolean addObject(Pair coords, MapObject object) {
		return mapObjects.put(coords, object) == null;
	}

	public boolean addObject(int x, int y, int id, boolean force) {
		if (!correctCoords(coords.set(x, y)))
			return false;

		if (mapObjects.containsKey(coords)) {
			if (force)
				mapObjects.remove(coords);
			else
				return false;
		}

		addObject(new Pair(coords), new MapObject(id));
		setObjectSize(x, y, id);
		return true;
	}

	public boolean addObject(int x, int y, int id) {
		return addObject(x, y, id, true);
	}

	boolean addObject(Pair coord, int id) {
		return addObject(coord.x, coord.y, id);
	}

	public boolean dmgObjInDir(int x, int y, Direction direction) {
		coords.set(x, y).addDirection(direction);
		return mapObjects.get(coords).takeDamage();
	}

	public boolean addObject(int x, int y, Direction dir, int id) {
		coords.set(x, y).addDirection(dir);
		return addObject(coords.x, coords.y, id);
	}

	public MapObject getObject(int x, int y) {
		if (correctCoords(coords.set(x, y))) {
			MapObject ret = mapObjects.get(coords);
			if (ret != null) {
				if (ret.id == 0) {
					mapObjects.remove(coords);
					return nullObject;
				}
				return ret;
			} else
				return nullObject;
		} else
			return nullObject;
	}

	boolean objectExists(int x, int y) {
		if (correctCoords(coords.set(x, y))) {
			return !getObject(x, y).equals(nullObject);
		} else
			return false;
	}

	public void randPlaceObject(int id) {
		int x = Utils.random.nextInt(this.xsz);
		int y = Utils.random.nextInt(this.ysz);
		addObject(x, y, id);
	}

	public void randPlaceObject(ArrayList<Integer> id, int range) {
		randPlaceObject(id.get(Utils.random.nextInt(range)));
	}

	public void randPlaceTile(int id) {
		int x = Utils.random.nextInt(this.xsz);
		int y = Utils.random.nextInt(this.ysz);
		addTile(x, y, id, true);
	}

	public MapObject getObject(int x, int y, Direction dir) {
		if (correctCoords(coords.set(x, y))) {
			coords.addDirection(dir);
			return getObject(coords.x, coords.y);
		} else
			return nullObject;
	}

	public Loot getLoot(int x, int y) {
		if (correctCoords(coords.set(x, y))) {
			Loot ret = mapLoot.get(coords.set(x, y));
			if (ret != null) {
				if (ret.isEmpty()) {
					removeLoot(x, y);
					return nullLoot;
				}
				return ret;
			} else
				return nullLoot;
		} else
			return nullLoot;
	}

	void removeLoot(int x, int y) {
		if (correctCoords(coords.set(x, y)))
			mapLoot.remove(coords);
	}

	public void putLoot(int x, int y, int id, int q) {
		if (correctCoords(coords.set(x, y))) {

			if (mapLoot.get(coords) != null)
				mapLoot.get(coords).add(id, q);
			else
				mapLoot.put(coords, new Loot(new Item(id, q)));

		}
	}

	public void putLoot(int x, int y, Item item) {
		if (correctCoords(coords.set(x, y))) {
			if (mapLoot.get(coords) != null)
				mapLoot.get(coords).add(item);
			else
				mapLoot.put(coords, new Loot(item));

		}
	}

	public void updateLight(int wx, int wy, int r) {
		Tile tile;
		MapObject object;
		boolean blocksLight = false;
		for (int x = -r; x < r; x++) {
			for (int y = -r; y < r; y++) {

				if (x * x + y * y > r * r)
					continue;

				if (wx + x < 0 || wx + x > xsz || wy + y < 0 || wy + y > ysz)
					continue;

				tile = getTile(wx + x, wy + y);

				tile.visible = true;
				blocksLight = false;

				for (Point p : new Line(wx, wy, wx + x, wy + y)) {
					tile = getTile(p.x, p.y);
					object = getObject(p.x, p.y);

					if (blocksLight)
						tile.visible = false;

					if (object != nullObject && !object.nocollide)
						blocksLight = true;

					if (tile.visible)
						tile.visited = true;
				}
			}
		}
	}

	public void updateCrops() {
		Pair coord = new Pair();
		Crop newCrop;
		ArrayList<Pair> del = new ArrayList<Pair>();
		for (Entry<Pair, Crop> crop : mapCrops.entrySet()) {
			if (crop.getValue().upd()) {
				coord = crop.getKey();
				newCrop = crop.getValue();

				if (newCrop.curStage == Crop.STAGE_COUNT - 1)
					del.add(coord);

				addObject(new Pair(coord), newCrop.objId);

			}
		}
		for (int i = 0; i < del.size(); ++i)
			mapCrops.remove(del.get(i));
	}

	public boolean putCrop(int x, int y, int id) { // item id
		if (!correctCoords(coords.set(x, y)))
			return false;
		if (objectExists(x, y))
			return false;
		if (getTile(x, y).id != PLOWED_SOIL)
			return false;

		Crop newCrop = new Crop(id, MadSand.world.globalTick);
		mapCrops.put(new Pair(coords), newCrop);
		addObject(x, y, newCrop.objId);
		return true;
	}

	public boolean putCrop(int x, int y, Crop crop) {
		if (!correctCoords(coords.set(x, y)))
			return false;
		if (getTile(x, y).id != PLOWED_SOIL)
			return false;
		addObject(x, y, crop.objId);
		mapCrops.put(new Pair(coords), crop);
		return true;
	}

	public Crop getCrop(int x, int y) {
		if (correctCoords(coords.set(x, y))) {
			Crop ret = mapCrops.get(new Pair(coords));
			if (ret != null)
				return ret;
		}
		return nullCrop;
	}

	void removeCrop(int x, int y) {
		if (correctCoords(coords.set(x, y)))
			mapCrops.remove(new Pair(coords));
	}

	public boolean spawnNpc(int id, int x, int y) {
		if (!correctCoords(coords.set(x, y)))
			return false;

		if (!getNpc(coords.x, coords.y).equals(nullNpc))
			return false;

		Npc npc = new Npc(id, x, y);

		return putNpc(npc);
	}

	public boolean putNpc(Npc npc) {
		int x = npc.x;
		int y = npc.y;

		if (!correctCoords(coords.set(x, y)))
			return false;

		if (!getNpc(coords.x, coords.y).equals(nullNpc))
			return false;

		if (npc.spawnOnce)
			World.player.knownNpcs.add(npc.id);
		mapNpcs.put(coords, npc);
		return true;
	}

	public boolean putNpc(Npc npc, int x, int y) {
		npc.teleport(x, y);
		return moveNpc(npc, x, y);
	}

	public Npc getNpc(int x, int y) {
		if (!correctCoords(coords.set(x, y)))
			return nullNpc;

		Npc npc = mapNpcs.get(coords);

		if (npc == null)
			return nullNpc;

		if (!npc.equals(nullNpc))
			return npc;

		return nullNpc;
	}

	public Npc getNpc(Pair coords) {
		return getNpc(coords.x, coords.y);
	}

	public boolean moveNpc(Npc npc, int x, int y) { // moves npc only on the grid(not on the screen) to process smooth movement;
		//should be called by an npc before changing its own position.
		int xold = npc.x, yold = npc.y;
		Npc destNpc = getNpc(coords.x, coords.y);

		if (!correctCoords(coords.set(x, y)))
			return false;

		if (destNpc != nullNpc && destNpc != npc)
			return false;

		npc.setGridCoords(x, y);
		putNpc(npc);

		if (!(xold == x && yold == y))
			removeNpc(xold, yold);

		return true;
	}

	public boolean removeNpc(Npc npc) {
		return removeNpc(npc.x, npc.y);
	}

	public boolean removeNpc(int x, int y) {

		if (!correctCoords(coords.set(x, y)))
			return false;

		if (getNpc(coords.x, coords.y).equals(nullNpc))
			return false;

		mapNpcs.remove(coords);
		return true;
	}

	private static int MAX_NPCS = 50; // Max npcs -- for autospawn only

	public void spawnMobs(boolean friendly) {
		WorldGenPreset preset = WorldGenProp.getBiome(getBiome());
		OverworldPreset overworld = preset.overworld;

		if (getNpcCount() >= MAX_NPCS)
			return;

		if (friendly)
			spawnFromRollList(overworld.friendlyMobs, overworld.friendlySpawnChance);
		else
			spawnFromRollList(overworld.hostileMobs, overworld.hostileSpawnChance);

	}

	private void spawnFromRollList(RollList list, double chance) {
		if (Utils.percentRoll(chance))
			for (int i = 0; i < list.rollCount; ++i) {
				getRandomPoint(World.player.fov);
				spawnNpc(Utils.randElement(list.idList), coords.x, coords.y);
			}
	}

	public void naturalRegeneration() {

		WorldGenPreset preset = WorldGenProp.getBiome(getBiome());
		OverworldPreset overworld = preset.overworld;

		if (overworld.regenerateObjects == null)
			return;

		if (!Utils.percentRoll(overworld.chanceToRegenerate))
			return;

		int maxObjects = getMaxObjects();

		Utils.out("Regenerating objects from regenerateObjects...");
		Utils.out("Max objects for current location: " + maxObjects);

		if (getObjectCount() < maxObjects)
			for (RollList rollList : overworld.regenerateObjects)
				rollObjects(rollList);

	}

	public Pair locateTile(int id) {
		Tile tile = TileProp.tiles.get(id);

		if (!mapTiles.containsValue(tile))
			return Pair.nullPair;

		for (Entry<Pair, Tile> entry : mapTiles.entrySet()) {

			if (entry.getValue().equals(tile))
				return (entry.getKey());

		}

		return Pair.nullPair;
	}

	/*
	 * Get maximum count of objects on map by its size
	 */
	private float MAX_OBJECT_PERCENT = 0.15f; // Max percent of map allowed to be filled with objects

	public int getMaxObjects() {
		return (int) (xsz * ysz * MAX_OBJECT_PERCENT);
	}

	public void rollObjects(RollList objectRollList) {
		ArrayList<Integer> objectIdList;
		int listSize;
		for (int i = 0; i < objectRollList.rollCount; ++i) {
			objectIdList = objectRollList.idList;
			listSize = objectIdList.size();
			randPlaceObject(objectIdList.get(Utils.random.nextInt(listSize)));
		}
	}

}
