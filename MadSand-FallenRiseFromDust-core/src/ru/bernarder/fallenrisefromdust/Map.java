package ru.bernarder.fallenrisefromdust;

import java.util.HashMap;

import ru.bernarder.fallenrisefromdust.enums.Direction;
import ru.bernarder.fallenrisefromdust.properties.ObjectProp;

public class Map {
	private int xsz, ysz;

	static final Tile nullTile = new Tile(0);
	static final MapObject nullObject = new MapObject(0);
	static final Loot nullLoot = new Loot();

	private HashMap<Pair, Tile> mapTiles;
	private HashMap<Pair, MapObject> mapObjects;
	private HashMap<Pair, Loot> mapLoot;
	private HashMap<Pair, Npc> mapNpcs;

	Pair coords = new Pair(0, 0);

	public Map(int xsz, int ysz) {
		Utils.out("Creating map " + xsz + "x" + ysz);
		this.xsz = xsz;
		this.ysz = ysz;
		purge();
	}

	public Map() {
		this(0, 0);
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

	int getWidth() {
		return xsz;
	}

	int getHeight() {
		return ysz;
	}

	Map purge() {
		mapTiles = new HashMap<Pair, Tile>();
		mapObjects = new HashMap<Pair, MapObject>();
		mapLoot = new HashMap<Pair, Loot>();
		mapNpcs = new HashMap<Pair, Npc>();
		return this;
	}

	void putTileInDir(int x, int y, Direction dir, int id) {
		this.addTile(x, y, dir, id);
	}

	Map fillTile(int id) {
		int i = 0, ii = 0;
		while (i <= xsz) {
			while (ii <= ysz) {
				this.addTile(i, ii, id);
				ii++;
			}
			ii = 0;
			i++;
		}
		return this;
	}

	Map fillObject(int id) {
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

	boolean addTile(int x, int y, int id) {
		if (correctCoords(coords.set(x, y))) {
			if (mapTiles.containsKey(coords))
				mapTiles.remove(coords);
			mapTiles.put(coords, new Tile(id));
			return true;
		} else
			return false;
	}

	boolean addTile(int x, int y, Direction dir, int id) {
		if (correctCoords(coords.set(x, y))) {
			coords.addDirection(dir);
			addTile(coords.x, coords.y, id);
			return true;
		} else
			return false;
	}

	Tile getTile(int x, int y) {
		try {
			if (correctCoords(coords.set(x, y))) {
				Tile ret = mapTiles.get(coords);
				if (ret == null)
					ret = nullTile;
				return ret;
			} else {
				Utils.out("CorrectCoords false");
				return nullTile;
			}
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

	void delObject(int x, int y) {
		if (correctCoords(coords.set(x, y)))
			mapObjects.remove(coords);
	}

	boolean addObject(int x, int y, int id) {
		if (correctCoords(coords.set(x, y))) {
			if (mapObjects.containsKey(coords))
				mapObjects.remove(coords);
			mapObjects.put(coords, new MapObject(id));
			addRendMasks(x, y, id);
			return true;
		} else
			return false;
	}

	void dmgObjInDir(int x, int y, Direction direction) {
		coords.set(x, y).addDirection(direction);
		mapObjects.get(coords).takeDamage();
	}

	boolean addObject(int x, int y, Direction dir, int id) {
		coords.set(x, y).addDirection(dir);
		return addObject(coords.x, coords.y, id);
	}

	MapObject getObject(int x, int y) {
		if (correctCoords(coords.set(x, y))) {
			MapObject ret = mapObjects.get(coords);
			if (ret != null)
				return ret;
			else
				return nullObject;
		} else
			return nullObject;
	}

	void randPlaceObject(int id) {
		int x = Utils.random.nextInt(this.xsz);
		int y = Utils.random.nextInt(this.ysz);
		addObject(x, y, id);
	}

	void randPlaceObject(int[] id) {
		randPlaceObject(id[Utils.random.nextInt(id.length)]);
	}

	void randPlaceTile(int id) {
		int x = Utils.random.nextInt(this.xsz);
		int y = Utils.random.nextInt(this.ysz);
		addTile(x, y, id);
	}

	MapObject getObject(int x, int y, Direction dir) {
		if (correctCoords(coords.set(x, y))) {
			coords.addDirection(dir);
			return getObject(coords.x, coords.y);
		} else
			return nullObject;
	}

	Loot getLoot(int x, int y) {
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

	void putLoot(int x, int y, int id, int q) {
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

}
