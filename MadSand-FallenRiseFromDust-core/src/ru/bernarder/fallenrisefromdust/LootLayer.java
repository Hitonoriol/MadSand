package ru.bernarder.fallenrisefromdust;

import ru.bernarder.fallenrisefromdust.strings.InventoryNames;

public class LootLayer {
	public static String[][][] lootLayer = new String[MadSand.MAPSIZE + MadSand.BORDER][MadSand.MAPSIZE
			+ MadSand.BORDER][MadSand.OBJLEVELS];

	public static void init() {
		int i = 0;
		int ii = 0;
		while (i < MadSand.MAPSIZE + MadSand.BORDER) {
			while (ii < MadSand.MAPSIZE + MadSand.BORDER) {
				lootLayer[i][ii][0] = "n";
				lootLayer[i][ii][1] = "n";
				ii++;
			}
			i++;
			ii = 0;
		}
	}

	static void lootCollision() {
		if (Player.standingOnLoot(MadSand.x, MadSand.y)) {
			addLootToInv(MadSand.x, MadSand.y);
			removeLoot(MadSand.x, MadSand.y);
			MadSand.print("You picked some things up");
		}
	}

	static void removeLoot(int x, int y) {
		lootLayer[x][y][MadSand.curlayer] = "n";
	}

	public static void putLoot(int x, int y, int id, int q) {
		if (lootLayer[x][y][MadSand.curlayer] == "n") {
			lootLayer[x][y][MadSand.curlayer] = "";
		}
		int tmp39_36 = MadSand.curlayer;
		String[] tmp39_35 = lootLayer[x][y];
		tmp39_35[tmp39_36] = (tmp39_35[tmp39_36] + ":" + id + "/" + q);
	}

	public static void putLootQuery(int x, int y, String query) {
		if (lootLayer[x][y][MadSand.curlayer] == "n") {
			lootLayer[x][y][MadSand.curlayer] = "";
		}
		int tmp39_36 = MadSand.curlayer;
		String[] tmp39_35 = lootLayer[x][y];
		tmp39_35[tmp39_36] = (tmp39_35[tmp39_36] + query);
	}

	public static int addLootToInv(int x, int y) {
		int i = 1;
		String temp = lootLayer[x][y][MadSand.curlayer];
		Utils.out("Loot Cell contents: " + temp);
		try {
			String[] block = temp.split(":");
			String[] attr = new String[0];
			if (!temp.equals("nn")) {
				while (i < block.length) {
					attr = block[i].split("/");

					InvUtils.putItem(Integer.parseInt(attr[0]), Integer.parseInt(attr[1]), false);
					i++;
				}
				return Integer.parseInt(attr[0]);
			}
			removeLoot(x, y);
			return 0;
		} catch (Exception localException) {
		}

		return 0;
	}

	public static String getTextQuery(String query) {
		int i = 1;
		String temp = query;
		String ret = "";
		try {
			String[] block = temp.split(":");
			String[] attr = new String[0];
			while (i < block.length) {
				attr = block[i].split("/");
				ret = ret + attr[1] + " " + InventoryNames.name.get(Integer.parseInt(attr[0])) + " ";
				i++;
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace(Resource.eps);
		}
		return "";
	}

	public static boolean removeLootFromInv(String query) {
		int i = 1;
		String temp = query;
		try {
			String[] block = temp.split(":");
			String[] attr = new String[0];
			while (i < block.length) {
				attr = block[i].split("/");
				if (InvUtils.getSameCell(Integer.parseInt(attr[0]), Integer.parseInt(attr[1])) == -1) {
					return false;
				}
				i++;
			}
			i = 1;
			while (i < block.length) {
				attr = block[i].split("/");
				InvUtils.delItem(Integer.parseInt(attr[0]), Integer.parseInt(attr[1]));
				i++;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace(Resource.eps);
		}
		return false;
	}

	public static int addLootToTInv(String query) {
		int i = 1;
		String temp = query;
		try {
			String[] block = temp.split(":");
			String[] attr = new String[0];
			while (i < block.length) {
				attr = block[i].split("/");
				InvUtils.putItemT(Integer.parseInt(attr[0]), Integer.parseInt(attr[1]), 0);
				i++;
			}
			return Integer.parseInt(attr[0]);
		} catch (Exception e) {
			e.printStackTrace(Resource.eps);
		}
		return 0;
	}

	public static boolean ifExist(String sequence) {
		int i = 1;

		String temp = sequence;
		String[] block = temp.split(":");
		String[] attr = new String[0];
		int id = 0;
		int q = 0;
		while (i < block.length) {
			attr = block[i].split("/");
			id = Integer.parseInt(attr[0]);
			q = Integer.parseInt(attr[1]);
			if (InvUtils.getSameCell(id, q) == -1) {
				return false;
			}
			i++;
		}
		return true;
	}

	public static String getCell(int x, int y) {
		String t = "n";
		if (x < MadSand.MAPSIZE && y < MadSand.MAPSIZE && x >= 0 && y >= 0)
			t = lootLayer[x][y][MadSand.curlayer];
		return t;
	}
}