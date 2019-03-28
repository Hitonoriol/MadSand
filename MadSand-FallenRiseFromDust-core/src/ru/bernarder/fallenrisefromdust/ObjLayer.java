package ru.bernarder.fallenrisefromdust;

import ru.bernarder.fallenrisefromdust.strings.Objects;

public class ObjLayer {
	public static int[][][][] ObjLayer;
	public static int[] vRendMasks, hRendMasks;
	public static String altitems[][];
	public static int mx;
	public static int my;

	public static void randPlaceObject(int id, int size) {
		int x = Utils.random.nextInt(size);
		int y = Utils.random.nextInt(size);
		AddObj(x, y, id);
	}

	public static void randPlaceObject(int[] id, int size) {
		int x = Utils.random.nextInt(size);
		int y = Utils.random.nextInt(size);
		AddObj(x, y, id[Utils.random.nextInt(id.length)]);
	}

	public static void randPlaceTile(int id, int size) {
		WorldGen.world[Utils.random.nextInt(size)][Utils.random.nextInt(size)][0] = id;
	}

	public static void putTileInDir(int x, int y, String dir, int id) {
		if (dir == "up")
			WorldGen.world[(y + 1)][x][MadSand.curlayer] = id;
		if (dir == "down")
			WorldGen.world[(y - 1)][x][MadSand.curlayer] = id;
		if (dir == "left")
			WorldGen.world[y][(x - 1)][MadSand.curlayer] = id;
		if (dir == "right") {
			WorldGen.world[y][(x + 1)][MadSand.curlayer] = id;
		}
	}

	public static void init() {
		ObjLayer = new int[MadSand.MAPSIZE + MadSand.BORDER][MadSand.MAPSIZE + MadSand.BORDER][2][MadSand.OBJLEVELS];
	}

	public static void dmgObj(int x, int y, String direction) {
		if (direction.equals("up")) {
			ObjLayer[x][(y + 1)][1][MadSand.curlayer] -= 1;
			if (ObjLayer[x][(y + 1)][1][MadSand.curlayer] <= 0) {
				ObjLayer[x][(y + 1)][0][MadSand.curlayer] = 0;
			}
		}
		if ((direction.equals("down")) && (y > 0)) {
			ObjLayer[x][(y - 1)][1][MadSand.curlayer] -= 1;
			if (ObjLayer[x][(y - 1)][1][MadSand.curlayer] <= 0) {
				ObjLayer[x][(y - 1)][0][MadSand.curlayer] = 0;
			}
		}
		if ((direction.equals("left")) && (x > 0)) {
			ObjLayer[(x - 1)][y][1][MadSand.curlayer] -= 1;
			if (ObjLayer[(x - 1)][y][1][MadSand.curlayer] <= 0) {
				ObjLayer[(x - 1)][y][0][MadSand.curlayer] = 0;
			}
		}
		if (direction.equals("right")) {
			ObjLayer[(x + 1)][y][1][MadSand.curlayer] -= 1;
			if (ObjLayer[(x + 1)][y][1][MadSand.curlayer] <= 0) {
				ObjLayer[(x + 1)][y][0][MadSand.curlayer] = 0;
			}
		}
		new ThreadedUtils().mapSendK.start();
	}

	public static void setObjToughness(int x, int y, int to) {
		ObjLayer[x][y][1][MadSand.curlayer] = to;
	}

	public static void AddObj(int x, int y, int id) {
		if (ObjLayer[x][y][0][MadSand.curlayer] == 0 && WorldGen.world[y][x][0] != 8) {
			ObjLayer[x][y][0][MadSand.curlayer] = id;
			int i = vRendMasks[id];
			if (y + 1 < MadSand.MAPSIZE - 1) {
				while (i > 0) {
					if (y + i < MadSand.MAPSIZE) {
						ObjLayer[x][(y + i)][0][MadSand.curlayer] = 666;
						ObjLayer[x][(y + i)][1][MadSand.curlayer] = 666;
					}
					i--;
				}
			}
			i = hRendMasks[id];
			if (x + 1 < MadSand.MAPSIZE - 1) {
				while (i > 0) {
					if (y + i < MadSand.MAPSIZE) {
						ObjLayer[x + i][(y)][0][MadSand.curlayer] = 666;
						ObjLayer[x + i][(y)][1][MadSand.curlayer] = 666;
					}
					i--;
				}
			}

			setObjToughness(x, y, Objects.hp.get(id));
		}
		new ThreadedUtils().mapSendK.start();
	}

	public static void AddObjForce(int x, int y, int id) {
		ObjLayer[x][y][0][MadSand.curlayer] = id;
		int i = vRendMasks[id];
		if (y + 1 < 99) {
			while (i > 0) {
				ObjLayer[x][(y + i)][0][MadSand.curlayer] = 666;
				ObjLayer[x][(y + i)][1][MadSand.curlayer] = 666;
				i--;
			}
		}
		i = hRendMasks[id];
		if (x + 1 < MadSand.MAPSIZE - 1) {
			while (i > 0) {
				if (y + i < MadSand.MAPSIZE) {
					ObjLayer[x + i][(y)][0][MadSand.curlayer] = 666;
					ObjLayer[x + i][(y)][1][MadSand.curlayer] = 666;
				}
				i--;
			}
		}

		setObjToughness(x, y, Objects.hp.get(id));

		new ThreadedUtils().mapSendK.start();
	}

	public static void AddObjForce(int x, int y, int id, int layer) {
		ObjLayer[x][y][0][layer] = id;
		int i = vRendMasks[id];
		if (y + 1 < 99) {
			while (i > 0) {
				ObjLayer[x][(y + i)][0][layer] = 666;
				ObjLayer[x][(y + i)][1][layer] = 666;
				i--;
			}
		}
		i = hRendMasks[id];
		if (x + 1 < MadSand.MAPSIZE - 1) {
			while (i > 0) {
				if (y + i < MadSand.MAPSIZE) {
					ObjLayer[x + i][(y)][0][MadSand.curlayer] = 666;
					ObjLayer[x + i][(y)][1][MadSand.curlayer] = 666;
				}
				i--;
			}
		}

		setObjToughness(x, y, Objects.hp.get(id));

		new ThreadedUtils().mapSendK.start();
	}

	public static void AddObj(int x, int y, int id, int xwpos, int ywpos, int layer) {
		if (ObjLayer[x][y][0][layer] == 0) {
			ObjLayer[x][y][0][layer] = id;
			int i = vRendMasks[id];
			if (y + 1 < 99) {
				while (i > 0) {
					ObjLayer[x][(y + i)][0][layer] = 666;
					ObjLayer[x][(y + i)][1][layer] = 666;
					i--;
				}
			}
			i = hRendMasks[id];
			if (x + 1 < MadSand.MAPSIZE - 1) {
				while (i > 0) {
					if (y + i < MadSand.MAPSIZE) {
						ObjLayer[x + i][(y)][0][MadSand.curlayer] = 666;
						ObjLayer[x + i][(y)][1][MadSand.curlayer] = 666;
					}
					i--;
				}
			}

			ObjLayer[x][y][1][layer] = Objects.hp.get(id);
		}
		new ThreadedUtils().mapSendK.start();
	}

	public static int getBlock(int x, int y, String direction, int layer) {
		int id = 0;
		try {
			if (direction.equals("up"))
				id = ObjLayer[x][(y + 1)][0][layer];
			if ((direction.equals("down")) && (y > 0))
				id = ObjLayer[x][(y - 1)][0][layer];
			if ((direction.equals("left")) && (x > 0))
				id = ObjLayer[(x - 1)][y][0][layer];
			if (direction.equals("right"))
				id = ObjLayer[(x + 1)][y][0][layer];
			return id;
		} catch (Exception e) {
			return 0;
		}
	}

	public static int getBlock(int x, int y, int layer) {
		int id = 0;
		if ((x < MadSand.MAPSIZE + MadSand.BORDER) && (y < MadSand.MAPSIZE + MadSand.BORDER) && (x >= 0) && (y >= 0))
			id = ObjLayer[x][y][0][layer];
		if (id == 666)
			return 0;
		return id;
	}

	public static String getObjName(int x, int y, String direction) {
		String name = "";
		int id = 0;
		if ((direction.equals("up")) && (y < MadSand.MAPSIZE + MadSand.BORDER))
			id = ObjLayer[x][(y + 1)][0][MadSand.curlayer];
		if ((direction.equals("down")) && (y > 0))
			id = ObjLayer[x][(y - 1)][0][MadSand.curlayer];
		if ((direction.equals("left")) && (x > 0))
			id = ObjLayer[(x - 1)][y][0][MadSand.curlayer];
		if ((direction.equals("right")) && (x < MadSand.MAPSIZE + MadSand.BORDER)
				&& (y < MadSand.MAPSIZE + MadSand.BORDER))
			id = ObjLayer[(x + 1)][y][0][MadSand.curlayer];
		if (id != 0) {
			name = Objects.name.get(id);
		}
		return name;
	}

	public static void delObject(int x, int y, String direction) {
		if (direction.equals("up"))
			ObjLayer[x][(y + 1)][0][MadSand.curlayer] = 0;
		if ((direction.equals("down")) && (y > 0))
			ObjLayer[x][(y - 1)][0][MadSand.curlayer] = 0;
		if ((direction.equals("left")) && (x > 0))
			ObjLayer[(x - 1)][y][0][MadSand.curlayer] = 0;
		if (direction.equals("right"))
			ObjLayer[(x + 1)][y][0][MadSand.curlayer] = 0;
		new ThreadedUtils().mapSendK.start();
	}

	public static void delObjectL(int x, int y, int layer) {
		ObjLayer[x][y][0][layer] = 0;
		new ThreadedUtils().mapSendK.start();
	}

	public static void placeObjInDirection(int x, int y, int id, String direction) {
		System.out.println(id);
		if (direction.equals("up")) {
			AddObj(x, y + 1, id, MadSand.curxwpos, MadSand.curywpos, MadSand.curlayer);
		}
		if ((direction.equals("down")) && (y > 0)) {
			AddObj(x, y - 1, id, MadSand.curxwpos, MadSand.curywpos, MadSand.curlayer);
		}
		if ((direction.equals("left")) && (x > 0)) {
			AddObj(x - 1, y, id, MadSand.curxwpos, MadSand.curywpos, MadSand.curlayer);
		}
		if (direction.equals("right")) {
			AddObj(x + 1, y, id, MadSand.curxwpos, MadSand.curywpos, MadSand.curlayer);
		}
		new ThreadedUtils().mapSendK.start();
	}

	public static int rend(int w, int h) {
		int tile = 0;
		if (w <= MadSand.MAPSIZE && h <= MadSand.MAPSIZE && w >= 0 && h >= 0)
			tile = ObjLayer[w][h][0][MadSand.curlayer];
		if (tile <= MadSand.LASTOBJID || tile == 666)
			return tile;
		else
			return 0;
	}
}
