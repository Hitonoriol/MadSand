package ru.bernarder.fallenrisefromdust;

import java.util.Random;

import com.github.czyzby.noise4j.map.Grid;
import com.github.czyzby.noise4j.map.generator.noise.NoiseGenerator;
import com.github.czyzby.noise4j.map.generator.room.dungeon.DungeonGenerator;
import com.github.czyzby.noise4j.map.generator.util.Generators;

public class WorldGen {
	public static int[] ores = { 22, 23 };
	public static int[][][] world = new int[MadSand.MAPSIZE + MadSand.BORDER][MadSand.MAPSIZE
			+ MadSand.BORDER][MadSand.OBJLEVELS];
	public static Random random = new Random();
	static int biome;

	public static int[][][] Generate(boolean tofile) {
		SysMethods.out("WorldGen start!");
		try {
			makeEmpty();
			random = new Random();
			if ((MadSand.curxwpos == 5) && (MadSand.curywpos == 5))
				biome = 0;
			else
				biome = random.nextInt(MadSand.BIOMES);
			genTerrain();
			if (random.nextBoolean())
				genOreFields();
			else
				genDungeon();
			genObjByTemplate();
			if ((MadSand.curxwpos == 5) && (MadSand.curywpos == 5))
				MadSand.setUpScene();
			if (tofile)
				GameSaver.saveWorld(MadSand.WORLDNAME);
			SysMethods.out("End of WorldGen!");
			return world;
		} catch (Exception e) {
			SysMethods.out("Whoops, fatal error... See MadSandCritical.log and/or MadSandErrors.log files.");
			e.printStackTrace(Resource.eps);
			System.exit(1);
		}
		return world;
	}

	public static void genTerrain() {
		SysMethods.out("Generating terrain!");
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
						world[ii][i][0] = 8;
					}
				}
				if (biome == 3) {
					if (grid.get(ii, i) >= 0.1f && grid.get(ii, i) <= 0.27f) {
						world[ii][i][0] = 22;
					}
				}
				ii++;
			}
			i++;
			ii = 0;
		}
		SysMethods.out("Done generating terrain!");
	}

	public static void genDungeon() {
		SysMethods.out("Generating dungeon!");
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
					world[it][iit][1] = 5;
				}
				if (grid.get(iit, it) == 1.0f)
					ObjLayer.AddObjForce(iit, it, 13, 1);
				if (grid.get(iit, it) == 0.5f) {
					ObjLayer.AddObjForce(iit, it, 0, 1);
					world[it][iit][1] = 5;
				}

				iit++;
			}
			iit = 0;
			it++;
		}
		SysMethods.out("Done dungeon generating!");
	}

	public static void genOreFields() {
		SysMethods.out("Generating orefields...");
		int a = random.nextInt(MadSand.OREFIELDS);
		while (a > 0) {

			try {
				MadSand.curlayer = 1;
				int x = random.nextInt(MadSand.MAPSIZE);
				int y = random.nextInt(MadSand.MAPSIZE);
				int w = random.nextInt(MadSand.MAXOREFIELDSIZE) + 1;
				int h = random.nextInt(MadSand.MAXOREFIELDSIZE) + 1;
				if ((x + w < MadSand.MAPSIZE) && (y + h < MadSand.MAPSIZE)) {
					int id = ores[random.nextInt(ores.length)];
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
		SysMethods.out("Orefields generated!");
	}

	public static void genBiomeTerrain(int ii, int i) {

		if (biome == 0) { // PLAIN
			if (random.nextInt(100) == 99) {
				world[ii][i][0] = 2;
			} else if (random.nextInt(100) == 99) {
				world[ii][i][0] = 1;
			} else if (random.nextInt(2000) == random.nextInt(2000)) {
				world[ii][i][0] = 6;
			} else {

				world[ii][i][0] = 0;
			}
		} else if (biome == 2) { // SNOW BIOME
			if (random.nextInt(1000) == 0) {
				world[ii][i][0] = 21;
			} else {
				world[ii][i][0] = 20;
			}
		} else if (biome == 3) { // SULFUR BIOME
			if (random.nextInt(100) == 0) {
				world[ii][i][0] = 10;
			} else if (random.nextInt(100) == 1) {
				world[ii][i][0] = 11;
			} else
				world[ii][i][0] = 15;
		} else if (biome == 1) { // DESSERT
			if (random.nextInt(100) == 4) {
				world[ii][i][0] = 3;
			} else if (random.nextInt(1000) == random.nextInt(1000)) {
				world[ii][i][0] = 17;
			} else {
				world[ii][i][0] = 4;
			}
		}

	}

	public static void makeEmpty() {
		SysMethods.out("makeEmpty start!");
		biome = 0;
		int i = 0;
		int ii = 0;
		while (i < MadSand.MAPSIZE + MadSand.BORDER) {
			while (ii < MadSand.MAPSIZE + MadSand.BORDER) {
				world[ii][i][1] = 5;
				ObjLayer.AddObj(ii, i, 13, MadSand.curxwpos, MadSand.curywpos, 1);
				ObjLayer.delObjectL(i, ii, 0);
				world[ii][i][0] = 0;
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
		SysMethods.out("End of makeEmpty!");
	}

	public static void genObjByTemplate() {
		SysMethods.out("Generating biome objects!");
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
		SysMethods.out("Done generating biome objects!");
	}

	public static int rend(int w, int h) {
		if (w >= 0 && h >= 0 && w < MadSand.MAPSIZE && h < MadSand.MAPSIZE) {
			int tile = world[w][h][MadSand.curlayer];
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
		return random.nextInt((max - min) + 1) + min;
	}
}