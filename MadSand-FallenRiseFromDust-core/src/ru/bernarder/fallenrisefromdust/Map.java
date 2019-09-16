package ru.bernarder.fallenrisefromdust;

import java.util.HashMap;

import ru.bernarder.fallenrisefromdust.enums.Direction;
import ru.bernarder.fallenrisefromdust.strings.Objects;

public class Map {
	private int xsz, ysz;

	static final Tile nullTile = new Tile(0);
	static final MapObject nullObject = new MapObject(0);
	static final Loot nullLoot = new Loot("n");

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

	int getWidth() {
		return xsz;
	}

	int getHeight() {
		return ysz;
	}

	void purge() {
		mapTiles = new HashMap<Pair, Tile>();
		mapObjects = new HashMap<Pair, MapObject>();
		mapLoot = new HashMap<Pair, Loot>();
		mapNpcs = new HashMap<Pair, Npc>();
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
			}
			else
				return nullTile;
		} catch (Exception e) {
			return nullTile;
		}
	}

	Tile getTile(int x, int y, Direction dir) {
		coords.set(x, y).addDirection(dir);
		return getTile(coords.x, coords.y);
	}

	void addRendMasks(int x, int y, int id) {
		int i = Objects.vRendMasks.get(id);
		if (y + 1 < MadSand.MAPSIZE - 1) {
			while (i > 0) {
				if (y + i < ysz) {
					addObject(x, y + i, 666);
				}
				i--;
			}
		}
		i = Objects.hRendMasks.get(id);
		if (x + 1 < MadSand.MAPSIZE - 1) {
			while (i > 0) {
				if (x + i < xsz) {
					addObject(x + i, y, 666);
				}
				i--;
			}
		}
	}

	boolean addObject(int x, int y, int id) {
		if (correctCoords(coords.set(x, y))) {
			mapObjects.put(coords, new MapObject(id));
			addRendMasks(x, y, id);
			return true;
		} else
			return false;
	}

	void dmgObjInDir(int x, int y, Direction direction) {
		coords.set(x, y).addDirection(direction);
		mapObjects.put(coords, getObject(coords.x, coords.y).takeDamage());
	}

	boolean addObject(int x, int y, Direction dir, int id) {
		coords.set(x, y).addDirection(dir);
		return addObject(coords.x, coords.y, id);
	}

	MapObject getObject(int x, int y) {
		try {
			if (correctCoords(coords.set(x, y))) {
				return mapObjects.get(coords);
			} else
				return nullObject;
		} catch (Exception e) {
			return nullObject;
		}
	}

	void randPlaceObject(int id) {
		Utils.out("" + xsz);
		Utils.out("" + ysz);
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
		try {
			return mapLoot.get(coords.set(x, y));
		} catch (Exception e) {
			return nullLoot;
		}
	}

}
