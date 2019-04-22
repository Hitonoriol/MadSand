package ru.bernarder.fallenrisefromdust;

public class CropLayer {
	public static int[][][] cropLayer = new int[MadSand.MAPSIZE + MadSand.BORDER][MadSand.MAPSIZE + MadSand.BORDER][2];
	public static String[] stages;

	public static void putCrop(int x, int y, int id) {
		cropLayer[x][y][0] = id;
		cropLayer[x][y][1] = MadSand.worldtime;
	}

	public static void putCropInDir(int x, int y, int id, String dir) {
		PlayerActions.harvestUp();
		if (dir == "up") {
			cropLayer[x][(y + 1)][0] = id;
			cropLayer[x][(y + 1)][1] = MadSand.worldtime;
		} else if (dir == "down") {
			cropLayer[x][(y - 1)][0] = id;
			cropLayer[x][(y - 1)][1] = MadSand.worldtime;
		} else if (dir == "left") {
			cropLayer[(x - 1)][y][0] = id;
			cropLayer[(x - 1)][y][1] = MadSand.worldtime;
		} else {
			cropLayer[(x + 1)][y][0] = id;
			cropLayer[(x + 1)][y][1] = MadSand.worldtime;
		}

	}

	public static void updCrops() {
		int i = 0;
		int ii = 0;
		while (i < MadSand.MAPSIZE + MadSand.BORDER) {
			while (ii < MadSand.MAPSIZE + MadSand.BORDER) {
				if ((cropLayer[i][ii][0] >= 40)) {
					if (MadSand.worldtime - cropLayer[i][ii][1] < 10
							&& MadSand.world.getCurLoc().getObject(i, ii).id != Integer
									.parseInt(stages[cropLayer[i][ii][0]].split(",")[0])) {
						MadSand.world.getCurLoc().addObject(i, ii,
								Integer.parseInt(stages[cropLayer[i][ii][0]].split(",")[0]));
					} else if (MadSand.worldtime - cropLayer[i][ii][1] == 10) {
						MadSand.world.getCurLoc().addObject(i, ii,
								Integer.parseInt(stages[cropLayer[i][ii][0]].split(",")[1]));
					} else if (MadSand.worldtime - cropLayer[i][ii][1] == 15) {
						MadSand.world.getCurLoc().addObject(i, ii,
								Integer.parseInt(stages[cropLayer[i][ii][0]].split(",")[2]));
					} else if (MadSand.worldtime - cropLayer[i][ii][1] == 25) {
						MadSand.world.getCurLoc().addObject(i, ii,
								Integer.parseInt(stages[cropLayer[i][ii][0]].split(",")[3]));
					} else
						cropLayer[i][ii][0] = 0;
				}

				ii++;
			}
			i++;
			ii = 0;
		}
	}
}