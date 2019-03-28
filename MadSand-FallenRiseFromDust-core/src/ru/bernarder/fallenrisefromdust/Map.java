package ru.bernarder.fallenrisefromdust;

import java.util.HashMap;

import ru.bernarder.fallenrisefromdust.enums.Direction;

public class Map {
	private int xsz, ysz;

	static final Tile nullTile = new Tile(0);
	static final Object nullObject = new Object(0);
	static final Loot nullLoot = new Loot("n");

	HashMap<Pair, Tile> mapTiles;
	HashMap<Pair, Object> mapObjects;
	HashMap<Pair, Loot> mapLoot;
	HashMap<Pair, Npc> mapNpcs;
	HashMap<Pair, Player> mapPlayers;

	Pair coords = new Pair(0, 0);

	public Map(int xsz, int ysz) {
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
		mapObjects = new HashMap<Pair, Object>();
		mapLoot = new HashMap<Pair, Loot>();
		mapNpcs = new HashMap<Pair, Npc>();
		mapPlayers = new HashMap<Pair, Player>();
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
			if (correctCoords(coords.set(x, y)))
				return mapTiles.get(coords);
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

	boolean addObject(int x, int y, int id) {
		if (correctCoords(coords.set(x, y))) {
			mapObjects.put(coords, new Object(id));
			return true;
		} else
			return false;
	}

	boolean addObject(int x, int y, Direction dir, int id) {
		coords.set(x, y).addDirection(dir);
		return addObject(coords.x, coords.y, id);
	}

	Object getObject(int x, int y) {
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

	Object getObject(int x, int y, Direction dir) {
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
