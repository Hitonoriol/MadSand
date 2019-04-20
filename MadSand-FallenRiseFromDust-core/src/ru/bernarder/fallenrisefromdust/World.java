package ru.bernarder.fallenrisefromdust;

import java.util.HashMap;
import java.util.Random;

import com.github.czyzby.noise4j.map.Grid;
import com.github.czyzby.noise4j.map.generator.noise.NoiseGenerator;
import com.github.czyzby.noise4j.map.generator.room.dungeon.DungeonGenerator;
import com.github.czyzby.noise4j.map.generator.util.Generators;

public class World {
	private int xsz, ysz;
	HashMap<MapID, Map> WorldLoc;

	public World(int xsz, int ysz) {
		this.xsz = xsz;
		this.ysz = ysz;
		createBasicLoc(new Pair(5, 5));
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
			return null;
	}

	Map getLoc(int x, int y, int layer) {
		return getLoc(new Pair(x, y), layer, 0);
	}

	Map putLoc(Pair wc, int layer, int id, Map loc) {
		WorldLoc.put(new MapID(wc, layer, id), loc);
		return loc;
	}

	Map putLoc(int x, int y, int layer, Map loc) {
		return putLoc(new Pair(x, y), layer, 0, loc);
	}

	boolean createBasicLoc(Pair wc) {
		if (!this.createLoc(new MapID(wc, 0, 0), new Map(xsz, ysz)))
			return false;
		if (!this.createLoc(new MapID(wc, 1, 0), new Map(xsz, ysz)))
			return false;
		return true;
	}

	public static int[] ores = { 22, 23 };
	static int biome;

	public void Generate(boolean tofile) {
		Utils.out("WorldGen start!");
		try {
			makeEmpty();
			Utils.random = new Random();
			if ((MadSand.curxwpos == 5) && (MadSand.curywpos == 5))
				biome = 0;
			else
				biome = Utils.random.nextInt(MadSand.BIOMES);
			genTerrain();
			if (Utils.random.nextBoolean())
				genOreFields();
			else
				genDungeon();
			genObjByTemplate();
			if ((MadSand.curxwpos == 5) && (MadSand.curywpos == 5))
				MadSand.setUpScene();
			if (tofile)
				GameSaver.saveWorld(MadSand.WORLDNAME);
			Utils.out("End of WorldGen!");
		} catch (Exception e) {
			Utils.out("Whoops, fatal error... See MadSandCritical.log and/or MadSandErrors.log files.");
			e.printStackTrace(Resource.eps);
			System.exit(1);
		}
	}

	private void putMapTile(int wx, int wy, int x, int y, int layer, int locid, int id) {
		WorldLoc.get(new MapID(new Pair(wx, wy), layer, locid)).mapTiles.put(new Pair(x, y), new Tile(id));
	}

	Tile getMapTile(int x, int y, int layer) {
		return WorldLoc.get(new MapID(new Pair(MadSand.curxwpos, MadSand.curywpos), layer, 0)).mapTiles
				.get(new Pair(x, y));
	}

	private void putMapTile(int x, int y, int id) {
		putMapTile(MadSand.curxwpos, MadSand.curywpos, x, y, MadSand.curlayer, 0, id);
	}

	private void putMapTile(int x, int y, int layer, int id) {
		putMapTile(MadSand.curxwpos, MadSand.curywpos, x, y, layer, 0, id);
	}

	public void genTerrain() {
		Utils.out("Generating terrain!");
		final Grid grid = new Grid(MadSand.MAPSIZE + MadSand.BORDER);
		final NoiseGenerator noiseGenerator = new NoiseGenerator();
		noiseGenerator.setRadius(4);
		noiseGenerator.setModifier(1f);
		noiseGenerator.setSeed(Generators.rollSeed());
		noiseGenerator.generate(grid);

		int i = 0;
		int ii = 0;

		while (i < MadSand.MAPSIZE + 1) {
			while (ii < MadSand.MAPSIZE + 1) {
				genBiomeTerrain(ii, i);
				ii++;
			}
			i++;
			ii = 0;
		}
		i = 0;
		ii = 0;
		while (i < MadSand.MAPSIZE + MadSand.BORDER) {
			while (ii < MadSand.MAPSIZE + MadSand.BORDER) {
				if (biome == 0) {
					if (grid.get(ii, i) >= 0.1f && grid.get(ii, i) <= 0.27f) {
						putMapTile(ii, i, 8);
					}
				}
				if (biome == 3) {
					if (grid.get(ii, i) >= 0.1f && grid.get(ii, i) <= 0.27f) {
						putMapTile(ii, i, 22);
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
		final Grid grid = new Grid(MadSand.MAPSIZE);
		final DungeonGenerator dungeonGenerator = new DungeonGenerator();
		dungeonGenerator.setRoomGenerationAttempts(MadSand.MAPSIZE);
		dungeonGenerator.setMaxRoomSize(25);
		dungeonGenerator.setTolerance(10); // Max difference between width and height.
		dungeonGenerator.setMinRoomSize(5);
		dungeonGenerator.generate(grid);
		int it = 0, iit = 0;
		while (it < MadSand.MAPSIZE) {
			while (iit < MadSand.MAPSIZE) {
				if (grid.get(iit, it) == 0.0f) {
					
					ObjLayer.AddObjForce(iit, it, 0, 1);
					putMapTile(it, iit, 1, 5);
				}
				if (grid.get(iit, it) == 1.0f)
					ObjLayer.AddObjForce(iit, it, 13, 1);
				if (grid.get(iit, it) == 0.5f) {
					ObjLayer.AddObjForce(iit, it, 0, 1);
					putMapTile(it, iit, 1, 5);
				}

				iit++;
			}
			iit = 0;
			it++;
		}
		Utils.out("Done dungeon generating!");
	}

	public static void genOreFields() {
		Utils.out("Generating orefields...");
		int a = Utils.random.nextInt(MadSand.OREFIELDS);
		while (a > 0) {

			try {
				MadSand.curlayer = 1;
				int x = Utils.random.nextInt(MadSand.MAPSIZE);
				int y = Utils.random.nextInt(MadSand.MAPSIZE);
				int w = Utils.random.nextInt(MadSand.MAXOREFIELDSIZE) + 1;
				int h = Utils.random.nextInt(MadSand.MAXOREFIELDSIZE) + 1;
				if ((x + w < MadSand.MAPSIZE) && (y + h < MadSand.MAPSIZE)) {
					int id = ores[Utils.random.nextInt(ores.length)];
					int k = 0;
					int kk = 0;
					while (kk < w) {
						while (k < h) {
							ObjLayer.AddObjForce(x + kk, y + k, id);
							k++;
						}
						k = 0;
						kk++;
					}
				}
				MadSand.curlayer = 0;

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

	public void makeEmpty() {
		Utils.out("makeEmpty start!");
		biome = 0;
		int i = 0;
		int ii = 0;
		while (i < MadSand.MAPSIZE + MadSand.BORDER) {
			while (ii < MadSand.MAPSIZE + MadSand.BORDER) {
				putMapTile(ii, i, 1, 5);
				ObjLayer.AddObj(ii, i, 13, MadSand.curxwpos, MadSand.curywpos, 1);
				ObjLayer.delObjectL(i, ii, 0);
				putMapTile(ii, i, 0);
				LootLayer.lootLayer[i][ii][0] = "n";
				LootLayer.lootLayer[i][ii][1] = "n";
				MobLayer.delMob(i, ii, 0);
				MobLayer.mobLayer[i][ii][11][0] = "-1";
				MobLayer.mobLayer[i][ii][11][0] = "-1";
				MobLayer.delMob(i, ii, 1);
				ii++;
			}
			i++;
			ii = 0;
		}
		Utils.out("End of makeEmpty!");
	}

	public static void genObjByTemplate() {
		Utils.out("Generating biome objects!");
		int iii = 0;
		if (biome == 0) {
			iii = 0;
			while (iii < MadSand.TREESDENSITY) {
				ObjLayer.randPlaceObject(new int[] { 2, 30, 35, 36 }, MadSand.MAPSIZE);
				iii++;
			}
			iii = 0;
			while (iii < MadSand.BOULDERDENSITY) {
				ObjLayer.randPlaceObject(new int[] { 1, 27, 28 }, MadSand.MAPSIZE);
				iii++;
			}
			iii = 0;
			while (iii < MadSand.BUSHDENSITY) {
				ObjLayer.randPlaceObject(new int[] { 37, 4 }, MadSand.MAPSIZE);
				iii++;
			}
		}
		if (biome == 2) {
			iii = 0;
			while (iii < MadSand.TREESDENSITY) {
				ObjLayer.randPlaceObject(new int[] { 32, 38, 33 }, MadSand.MAPSIZE);
				iii++;
			}
		}
		if (biome == 3) {
			iii = 0;
			while (iii < MadSand.TREESDENSITY) {
				ObjLayer.randPlaceObject(new int[] { 34, 33 }, MadSand.MAPSIZE);
				iii++;
			}
			if (biome == 2) {
				iii = 0;
				while (iii < MadSand.BUSHDENSITY) {
					ObjLayer.randPlaceObject(new int[] { 39 }, MadSand.MAPSIZE);
					iii++;
				}
			}
		}
		if (biome == 1) {
			iii = 0;
			while (iii < 35) {
				ObjLayer.randPlaceObject(10, MadSand.MAPSIZE);
				ObjLayer.randPlaceObject(24, MadSand.MAPSIZE);
				iii++;
			}
		}
		Utils.out("Done generating biome objects!");
	}

	public int rend(int w, int h) {
		if (w >= 0 && h >= 0 && w < MadSand.MAPSIZE && h < MadSand.MAPSIZE) {
			int tile = getMapTile(w, h, MadSand.curlayer).id;
			if (tile >= 0 && tile <= MadSand.LASTTILEID)
				return tile;
			else
				return 0;
		} else
			return 0;
	}

	public static int GetParam(int q) {
		int h = 0;
		h = q * 33;
		return h;
	}

	public static int rand(int min, int max) {
		return Utils.random.nextInt((max - min) + 1) + min;
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