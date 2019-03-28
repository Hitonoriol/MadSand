package ru.bernarder.fallenrisefromdust;

public class PlayerLayer {
	public static String[][][] playerLayer;
	public static String[][][] playerLayer1;

	public static void init() {
		playerLayer = new String[MadSand.MAPSIZE+MadSand.BORDER][MadSand.MAPSIZE+MadSand.BORDER][5];
		playerLayer1 = new String[MadSand.MAPSIZE+MadSand.BORDER][MadSand.MAPSIZE+MadSand.BORDER][5];
	}

	public static boolean isCollision(int x, int y, String dir) {
		boolean ret = false;
		if ((dir == "up") && (!playerLayer[x][(y + 1)][1].equals(""))) {
			ret = true;
		}

		if ((dir == "down") && (y - 1 >= 0) && (!playerLayer[x][(y - 1)][1].equals(""))) {
			ret = true;
		}

		if ((dir == "left") && (x - 1 >= 0) && (!playerLayer[(x - 1)][y][1].equals(""))) {
			ret = true;
		}

		if ((dir == "right") && (!playerLayer[(x + 1)][y][1].equals(""))) {
			ret = true;
		}

		return ret;
	}
}
