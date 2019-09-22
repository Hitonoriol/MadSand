package ru.bernarder.fallenrisefromdust;

import java.util.HashMap;
import java.util.Random;

import com.github.czyzby.noise4j.map.Grid;
import com.github.czyzby.noise4j.map.generator.noise.NoiseGenerator;
import com.github.czyzby.noise4j.map.generator.room.dungeon.DungeonGenerator;
import com.github.czyzby.noise4j.map.generator.util.Generators;

import ru.bernarder.fallenrisefromdust.enums.Direction;

/*	Old worldgen
	public static int[] ores = { 22, 23 };
	public static int[][][] world = new int[MadSand.MAPSIZE + MadSand.BORDER][MadSand.MAPSIZE
			+ MadSand.BORDER][MadSand.OBJLEVELS];
	public static Random random = new Random();
	static int biome;
 */

public class World {
	Map nullLoc = new Map(0, 0);
	private int xsz, ysz;
	public int curywpos = 5;
	public int curxwpos = 5;
	public int curlayer = 0;
	static final int BORDER = 1;
	static int MAPSIZE = 100;
	HashMap<MapID, Map> WorldLoc = new HashMap<MapID, Map>();
	static int worldtime = 12;

	public World(int sz) {
		this.xsz = sz;
		this.ysz = sz;
		if (!createBasicLoc(new Pair(curxwpos, curywpos), MAPSIZE, MAPSIZE))
			System.exit(-1);
	}

	boolean locExists(MapID loc) {
		return WorldLoc.containsKey(loc);
	}

	boolean createLoc(MapID loc, Map map) {
		if (!locExists(loc)) {
			this.WorldLoc.put(loc, map);
			return true;
		} else
			return false;
	}

	Map getLoc(Pair wc, int layer, int id) {
		MapID loc = new MapID(wc, layer, id);
		if (locExists(loc)) {
			return WorldLoc.get(loc);
		} else
			return nullLoc;
	}

	Map getLoc(int x, int y, int layer) {
		return getLoc(new Pair(x, y), layer, 0);
	}

	Map getCurLoc() {
		return getLoc(curxwpos, curywpos, curlayer);
	}

	Map getCurLoc(int layer) {
		return getLoc(curxwpos, curywpos, layer);
	}

	Map putLoc(Pair wc, int layer, int id, Map loc) {
		WorldLoc.put(new MapID(wc, layer, id), loc);
		return loc;
	}

	Map putLoc(int x, int y, int layer, Map loc) {
		return putLoc(new Pair(x, y), layer, 0, loc);
	}

	boolean createBasicLoc(Pair wc, int mx, int my) {
		if (!this.createLoc(new MapID(wc, 0), new Map(mx, my)))
			return false;
		if (!this.createLoc(new MapID(wc, 1), new Map(mx, my)))
			return false;
		return true;
	}

	public static int[] ores = { 22, 23 };
	static int biome;

	public void Generate() {
		Utils.out("WorldGen start!");
		try {
			clearCurLoc();
			Utils.random = new Random();
			if ((curxwpos == 5) && (curywpos == 5))
				biome = 0;
			else
				biome = Utils.random.nextInt(MadSand.BIOMES);
			genTerrain();
			if (Utils.random.nextBoolean())
				genOreFields();
			else
				genDungeon();
			genObjByTemplate();
			if ((curxwpos == 5) && (curywpos == 5))
				MadSand.setUpScene();
			Utils.out("End of WorldGen!");
		} catch (Exception e) {
			Utils.out("Whoops, fatal error... See MadSandCritical.log and/or MadSandErrors.log files.");
			e.printStackTrace(Resource.eps);
			System.exit(1);
		}
	}

	public void genTerrain() {
		Utils.out("Generating terrain!");
		final Grid grid = new Grid(World.MAPSIZE + World.BORDER);
		final NoiseGenerator noiseGenerator = new NoiseGenerator();
		noiseGenerator.setRadius(4);
		noiseGenerator.setModifier(1f);
		noiseGenerator.setSeed(Generators.rollSeed());
		noiseGenerator.generate(grid);

		int i = 0;
		int ii = 0;

		while (i < World.MAPSIZE + 1) {
			while (ii < World.MAPSIZE + 1) {
				genBiomeTerrain(ii, i);
				ii++;
			}
			i++;
			ii = 0;
		}
		i = 0;
		ii = 0;
		while (i < World.MAPSIZE + World.BORDER) {
			while (ii < World.MAPSIZE + World.BORDER) {
				if (biome == 0) {
					if (grid.get(ii, i) >= 0.1f && grid.get(ii, i) <= 0.27f) {
						getCurLoc().addTile(ii, i, 8);
					}
				}
				if (biome == 3) {
					if (grid.get(ii, i) >= 0.1f && grid.get(ii, i) <= 0.27f) {
						getCurLoc().addTile(ii, i, 22);
					}
				}
				ii++;
			}
			i++;
			ii = 0;
		}
		Utils.out("Done generating terrain!");
	}

	public void genDungeon() {
		Utils.out("Generating dungeon!");
		final Grid grid = new Grid(World.MAPSIZE);
		final DungeonGenerator dungeonGenerator = new DungeonGenerator();
		dungeonGenerator.setRoomGenerationAttempts(World.MAPSIZE);
		dungeonGenerator.setMaxRoomSize(25);
		dungeonGenerator.setTolerance(10); // Max difference between width and height.
		dungeonGenerator.setMinRoomSize(5);
		dungeonGenerator.generate(grid);
		int it = 0, iit = 0;
		while (it < World.MAPSIZE) {
			while (iit < World.MAPSIZE) {
				if (grid.get(iit, it) == 0.0f) {
					getCurLoc(1).addObject(iit, it, 0);
					getCurLoc(1).addTile(it, iit, 5);
				}
				if (grid.get(iit, it) == 1.0f)
					getCurLoc(1).addObject(iit, it, 13);
				if (grid.get(iit, it) == 0.5f) {
					getCurLoc(1).addObject(iit, it, 0);
					getCurLoc(1).addTile(it, iit, 5);
				}

				iit++;
			}
			iit = 0;
			it++;
		}
		Utils.out("Done dungeon generating!");
	}

	void genOreFields() {
		Utils.out("Generating orefields...");
		int a = Utils.random.nextInt(MadSand.OREFIELDS);
		while (a > 0) {

			try {
				curlayer = 1;
				int x = Utils.random.nextInt(World.MAPSIZE);
				int y = Utils.random.nextInt(World.MAPSIZE);
				int w = Utils.random.nextInt(MadSand.MAXOREFIELDSIZE) + 1;
				int h = Utils.random.nextInt(MadSand.MAXOREFIELDSIZE) + 1;
				if ((x + w < World.MAPSIZE) && (y + h < World.MAPSIZE)) {
					int id = ores[Utils.random.nextInt(ores.length)];
					int k = 0;
					int kk = 0;
					while (kk < w) {
						while (k < h) {
							addObj(x + kk, y + k, 1, id);
							k++;
						}
						k = 0;
						kk++;
					}
				}
				curlayer = 0;

			} catch (Exception e) {
				e.printStackTrace(Resource.eps);
			}
			a--;
		}
		Utils.out("Orefields generated!");
	}

	public void genBiomeTerrain(int ii, int i) {

		if (biome == 0) { // PLAIN
			if (Utils.random.nextInt(100) == 99) {
				putMapTile(ii, i, 2);
			} else if (Utils.random.nextInt(100) == 99) {
				putMapTile(ii, i, 1);
			} else if (Utils.random.nextInt(2000) == Utils.random.nextInt(2000)) {
				putMapTile(ii, i, 6);
			} else {

				putMapTile(ii, i, 0);
			}
		} else if (biome == 2) { // SNOW BIOME
			if (Utils.random.nextInt(1000) == 0) {
				putMapTile(ii, i, 21);
			} else {
				putMapTile(ii, i, 20);
			}
		} else if (biome == 3) { // SULFUR BIOME
			if (Utils.random.nextInt(100) == 0) {
				putMapTile(ii, i, 10);
			} else if (Utils.random.nextInt(100) == 1) {
				putMapTile(ii, i, 11);
			} else
				putMapTile(ii, i, 15);
		} else if (biome == 1) { // DESSERT
			if (Utils.random.nextInt(100) == 4) {
				putMapTile(ii, i, 3);
			} else if (Utils.random.nextInt(1000) == Utils.random.nextInt(1000)) {
				putMapTile(ii, i, 17);
			} else {
				putMapTile(ii, i, 4);
			}
		}

	}

	// Lazyass area

	int getTileId(int x, int y) {
		return getCurLoc().getTile(x, y).id;
	}

	int getObjID(int x, int y) {
		return getCurLoc().getObject(x, y).id;
	}

	int getObjID(int x, int y, Direction dir) {
		return getCurLoc().getObject(x, y, dir).id;
	}

	void putMapTile(int x, int y, int id) {
		getCurLoc().addTile(x, y, id);
	}

	void putMapTile(int x, int y, int layer, int id) {
		getCurLoc(layer).addTile(x, y, id);
	}

	void addObj(int x, int y, int id) {
		getCurLoc().addObject(x, y, id);
	}

	void delObj(int x, int y) {
		addObj(x, y, 0);
	}

	void addObj(int x, int y, int layer, int id) {
		getCurLoc(layer).addObject(x, y, id);
	}

	void delObj(int x, int y, int layer) {
		addObj(x, y, layer, 0);
	}

	// end of lazyass area

	public void clearCurLoc() {
		Utils.out("clearCurLoc");
		Map curLoc = getCurLoc();
		int xsz = curLoc.getWidth(), ysz = curLoc.getHeight();
		biome = 0;
		int i = 0;
		int ii = 0;
		while (i < ysz + World.BORDER) {
			while (ii < xsz + World.BORDER) {
				putMapTile(ii, i, 1, 5);
				addObj(ii, i, 1, 13);
				addObj(ii, i, 0);
				putMapTile(ii, i, 0);
				ii++;
			}
			i++;
			ii = 0;
		}
		Utils.out("End of clearCurLoc!");
	}

	void genObjByTemplate() {
		Utils.out("Generating biome objects!");
		int iii = 0;
		if (biome == 0) {
			iii = 0;
			while (iii < MadSand.TREESDENSITY) {
				getCurLoc().randPlaceObject(new int[] { 2, 30, 35, 36 });
				iii++;
			}
			iii = 0;
			while (iii < MadSand.BOULDERDENSITY) {
				getCurLoc().randPlaceObject(new int[] { 1, 27, 28 });
				iii++;
			}
			iii = 0;
			while (iii < MadSand.BUSHDENSITY) {
				getCurLoc().randPlaceObject(new int[] { 37, 4 });
				iii++;
			}
		}
		if (biome == 2) {
			iii = 0;
			while (iii < MadSand.TREESDENSITY) {
				getCurLoc().randPlaceObject(new int[] { 32, 38, 33 });
				iii++;
			}
		}
		if (biome == 3) {
			iii = 0;
			while (iii < MadSand.TREESDENSITY) {
				getCurLoc().randPlaceObject(new int[] { 34, 33 });
				iii++;
			}
			if (biome == 2) {
				iii = 0;
				while (iii < MadSand.BUSHDENSITY) {
					getCurLoc().randPlaceObject(new int[] { 39 });
					iii++;
				}
			}
		}
		if (biome == 1) {
			iii = 0;
			while (iii < 35) {
				getCurLoc().randPlaceObject(10);
				getCurLoc().randPlaceObject(24);
				iii++;
			}
		}
		Utils.out("Done generating biome objects!");
	}

	public int rend(int x, int y) {
		if (x >= 0 && y >= 0 && x < getCurLoc().getWidth() && y < getCurLoc().getHeight()) {
			int tile = getCurLoc().getTile(x, y).id;
			if (tile >= 0 && tile <= MadSand.LASTTILEID)
				return tile;
			else
				return 0;
		} else
			return 0;
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

	void timeTick() {// on successful movement or action
		// moblogic, croplogic, playerstats update
	}
}