package ru.bernarder.fallenrisefromdust;

public class BuildScript {
	public static void bLine(int x, int y, int dir, int id, int len, int head) {
		int ii = 0;
		while (ii < len) {
			if (head == 0) {
				ObjLayer.AddObjForce(x + ii * dir, y, id);
			} else
				ObjLayer.AddObjForce(x, y + ii * dir, id);
			ii++;
		}
	}

	public static void tLine(int x, int y, int dir, int id, int len, int head) {
		int ii = 0;
		while (ii < len) {
			if (head == 0) {
				WorldGen.world[(x + ii * dir)][y][0] = id;
			} else
				WorldGen.world[x][(y + ii * dir)][0] = id;
			ii++;
		}
	}

	public static void execute(String query) {
		Utils.out(query);
		query.replaceAll("\n", "");
		String[] gl = query.split(";");

		int iter = gl.length;
		int i = 0;
		MadSand.print("Executing script");
		try {
			while (i < iter) {
				String[] opline = gl[i].split(" ");
				if (opline[0].equalsIgnoreCase("pb"))
					ObjLayer.AddObjForce(Integer.parseInt(opline[2]), Integer.parseInt(opline[3]),
							Integer.parseInt(opline[1]));
				if (opline[0].equalsIgnoreCase("pt"))
					Utils.putTile(Integer.parseInt(opline[1]), Integer.parseInt(opline[2]),
							Integer.parseInt(opline[3]), MadSand.curlayer);
				if (opline[0].equalsIgnoreCase("pl"))
					LootLayer.putLootQuery(Integer.parseInt(opline[1]), Integer.parseInt(opline[2]), opline[3]);
				if (opline[0].equalsIgnoreCase("pm"))
					MobLayer.placeMob(Integer.parseInt(opline[1]), Integer.parseInt(opline[2]), opline[3]);
				if (opline[0].equalsIgnoreCase("clear"))
					WorldGen.makeEmpty();
				if (opline[0].equalsIgnoreCase("bsquare")) {
					int ii = 0;
					int h = Integer.parseInt(opline[4]);
					int c = Integer.parseInt(opline[3]);
					int x = Integer.parseInt(opline[1]);
					int y = Integer.parseInt(opline[2]);
					while (ii < h) {
						bLine(x, y, 1, c, h, 1);
						x++;
						ii++;
					}
				}
				if (opline[0].equals("randomloot")) {
					LootLayer.putLoot(Integer.parseInt(opline[1]), Integer.parseInt(opline[2]),
							WorldGen.rand(1, MadSand.LASTITEMID), WorldGen.rand(1, 50));
				}
				if (opline[0].equalsIgnoreCase("tsquare")) {
					int ii = 0;
					int h = Integer.parseInt(opline[4]);
					int c = Integer.parseInt(opline[3]);
					int x = Integer.parseInt(opline[1]);
					int y = Integer.parseInt(opline[2]);
					while (ii < h) {
						tLine(x, y, 1, c, h, 1);
						x++;
						ii++;
					}
				}
				if (opline[0].equalsIgnoreCase("bline")) {
					int x = Integer.parseInt(opline[1]);
					int y = Integer.parseInt(opline[2]);
					bLine(x, y, Integer.parseInt(opline[5]), Integer.parseInt(opline[6]), Integer.parseInt(opline[3]),
							Integer.parseInt(opline[4]));
				}
				if (opline[0].equalsIgnoreCase("tline")) {
					int x = Integer.parseInt(opline[1]);
					int y = Integer.parseInt(opline[2]);
					bLine(x, y, Integer.parseInt(opline[5]), Integer.parseInt(opline[6]), Integer.parseInt(opline[3]),
							Integer.parseInt(opline[4]));
				}
				i++;
			}
		} catch (Exception e) {
			MadSand.print("An error has occured in your script on op " + i);
		}
	}
}