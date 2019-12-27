package ru.bernarder.fallenrisefromdust.map;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;

import ru.bernarder.fallenrisefromdust.MadSand;
import ru.bernarder.fallenrisefromdust.Utils;
import ru.bernarder.fallenrisefromdust.containers.Pair;
import ru.bernarder.fallenrisefromdust.entities.Npc;
import ru.bernarder.fallenrisefromdust.entities.inventory.Item;
import ru.bernarder.fallenrisefromdust.enums.Direction;
import ru.bernarder.fallenrisefromdust.properties.ObjectProp;
import ru.bernarder.fallenrisefromdust.world.World;

public class Map {
	private int xsz, ysz;

	public static int PLOWED_SOIL = 15;

	private int biome = -1;
	private int defTile = 0;

	static final int COLLISION_MASK_ID = 666;
	static Tile nullTile = new Tile(0);
	public static MapObject nullObject = new MapObject(0);
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

	public void setDefTile(int tile) {
		defTile = tile;
	}

	public int getDefTile() {
		return defTile;
	}

	public Map() {
		this(0, 0);
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

	public Map fillTile(int id) {
		int i = 0, ii = 0;
		while (i <= xsz) {
			while (ii <= ysz) {
				this.addTile(i, ii, id, true);
				ii++;
			}
			ii = 0;
			i++;
		}
		return this;
	}

	public Map fillObject(int id) {
		int i = 0, ii = 0;
		while (i <= xsz) {
			while (ii <= ysz) {
				this.addObject(i, ii, id);
				ii++;
			}
			ii = 0;
			i++;
		}
		return this;
	}

	private boolean correctCoords(Pair coords) {
		if (coords.x <= xsz && coords.y <= ysz && coords.x >= 0 && coords.y >= 0)
			return true;
		else
			return false;
	}

	public boolean addTile(int x, int y, int id, boolean force) {
		if (correctCoords(coords.set(x, y))) {
			if (mapTiles.containsKey(coords) && force)
				mapTiles.remove(coords);
			mapTiles.put(coords, new Tile(id));
			return true;
		} else
			return false;
	}

	public void delTile(int x, int y) {
		mapTiles.remove(coords.set(x, y));
		mapTiles.put(coords, new Tile(defTile));
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
		try {
			if (correctCoords(coords.set(x, y))) {
				Tile ret = mapTiles.get(coords);
				if (ret == null)
					ret = nullTile;
				return ret;
			} else
				return nullTile;
		} catch (Exception e) {
			Utils.out("gettile error " + e.getMessage());
			e.printStackTrace();
			return nullTile;

		}
	}

	Tile getTile(int x, int y, Direction dir) {
		coords.set(x, y).addDirection(dir);
		return getTile(coords.x, coords.y);
	}

	void addRendMasks(int x, int y, int id) {
		int i = ObjectProp.vRendMasks.get(id);
		if (y + 1 < World.MAPSIZE - 1) {
			while (i > 0) {
				if (y + i < ysz) {
					addObject(x, y + i, 666);
				}
				i--;
			}
		}
		i = ObjectProp.hRendMasks.get(id);
		if (x + 1 < World.MAPSIZE - 1) {
			while (i > 0) {
				if (x + i < xsz) {
					addObject(x + i, y, 666);
				}
				i--;
			}
		}
	}

	public void delObject(int x, int y) {
		if (correctCoords(coords.set(x, y)))
			mapObjects.remove(coords);
	}

	public boolean addObject(int x, int y, int id) {
		if (correctCoords(coords.set(x, y))) {
			if (mapObjects.containsKey(coords))
				mapObjects.remove(coords);
			mapObjects.put(coords, new MapObject(id));
			addRendMasks(x, y, id);
			return true;
		} else
			return false;
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

	void randPlaceObject(int id) {
		int x = Utils.random.nextInt(this.xsz);
		int y = Utils.random.nextInt(this.ysz);
		addObject(x, y, id);
	}

	public void randPlaceObject(Vector<Integer> id, int range) {
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
			Utils.out("Adding loot id " + id + " q " + q + "|" + x + "," + y);
			if (mapLoot.get(coords) != null) {
				Utils.out("Adding to existing loot node...");
				mapLoot.get(coords).add(id, q);
			} else {
				Utils.out("Creating loot node...");
				mapLoot.put(coords, new Loot(new Item(id, q)));
			}
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

	public void update() {
		Pair coord = new Pair();
		Crop newCrop;
		Vector<Pair> del = new Vector<Pair>();
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

		if (getNpc(coords.x, coords.y) != nullNpc)
			return false;

		Npc npc = new Npc(id, x, y);

		return putNpc(npc);
	}

	public boolean putNpc(Npc npc) {
		int x = npc.x;
		int y = npc.y;

		if (!correctCoords(coords.set(x, y)))
			return false;

		if (getNpc(coords.x, coords.y) != nullNpc)
			return false;

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

		if (npc != nullNpc && npc != null)
			return npc;

		return nullNpc;
	}

	public boolean moveNpc(Npc npc, int x, int y) { // moves npc only on the grid(not on the screen) to process smooth
		// movement;should be called by an npc before changing its own position.
		// yeah, the
		// system is fucky.
		int xold = npc.x, yold = npc.y;
		Npc destNpc = getNpc(coords.x, coords.y);

		if (!correctCoords(coords.set(x, y)))
			return false;

		if (destNpc != nullNpc && destNpc != npc) {
			Utils.out("Can't move npc to non-empty cell");
			return false;
		}

		npc.setGridCoords(x, y);
		mapNpcs.put(coords, npc);

		if (!(xold == x && yold == y))
			removeNpc(xold, yold);

		return true;
	}

	boolean removeNpc(int x, int y) {

		if (!correctCoords(coords.set(x, y)))
			return false;

		if (getNpc(coords.x, coords.y) == nullNpc)
			return false;

		mapNpcs.remove(coords);
		return true;
	}

}
