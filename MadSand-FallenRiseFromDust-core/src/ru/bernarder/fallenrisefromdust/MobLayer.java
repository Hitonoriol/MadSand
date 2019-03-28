package ru.bernarder.fallenrisefromdust;

import java.util.Random;
import values.PlayerStats;

public class MobLayer {
	static int mobcount = 0;
	static final int atkRadius = 3;
	public static String[][][][] mobLayer;
	public static String[][] mobStats;
	static int statsq = 12;
	static boolean[] spawnflag = new boolean[14];
 
	public static void start() {
		mobStats = new String[MadSand.NPCSPRITES + 1][statsq];
	}

	public static void initLayer() {
		mobcount = 0;
		mobLayer = new String[MadSand.MAPSIZE + MadSand.BORDER][MadSand.MAPSIZE
				+ MadSand.BORDER][statsq][MadSand.OBJLEVELS];

		int i = 0;
		int ii = 0;
		while (i < MadSand.MAPSIZE + MadSand.BORDER) {
			while (ii < MadSand.MAPSIZE + MadSand.BORDER) {
				delMob(i, ii, 0);
				delMob(i, ii, 1);

				ii++;
			}
			i++;
			ii = 0;
		}
	}

	public static String rndItem() {
		String ret = "";
		int i = 29;
		while (i > 0) {
			ret = ret + ":" + (new Random().nextInt(40) + 1) + "/" + new Random().nextInt(100) + 1;
			i--;
		}
		return ret;
	}

	public static void placeMob(int x, int y, String id) {
		if ((mobcount < MadSand.MAXMOBSONMAP) && (Integer.parseInt(id) > 0)
				&& (ObjLayer.getBlock(x, y, MadSand.curlayer) == 0)) {
			if ((mobStats[Integer.parseInt(id)][10].equals("1")) && (!spawnflag[Integer.parseInt(id)]))
				return;
			mobcount += 1;
			try {
				int id1 = Integer.parseInt(id);
				spawnflag[id1] = true;
				int i = 0;
				while (i <= statsq - 1) {
					if ((id.equals("8")) && (i == 3)) {
						mobLayer[x][y][i][MadSand.curlayer] = (mobStats[8][3] + rndItem());
					} else
						mobLayer[x][y][i][MadSand.curlayer] = mobStats[id1][i];
					i++;
				}
				if (y + 1 < MadSand.MAPSIZE) {
					ObjLayer.ObjLayer[x][(y + 1)][0][0] = 0;
				}
			} catch (Exception localException) {
			}
		}
		new ThreadedUtils().mapSendK.start();
	}

	public static void placeMobForce(int x, int y, String id, int layer) {
		if (Integer.parseInt(id) > 0) {
			mobcount += 1;
			try {
				int id1 = Integer.parseInt(id);
				spawnflag[id1] = true;
				int i = 0;
				while (i <= statsq - 1) {
					if ((id.equals("8")) && (i == 3)) {
						mobLayer[x][y][i][layer] = (mobStats[8][3] + rndItem());
					} else
						mobLayer[x][y][i][layer] = mobStats[id1][i];
					i++;
				}
				if (y + 1 < MadSand.MAPSIZE) {
					ObjLayer.ObjLayer[x][(y + 1)][0][0] = 0;
				}
			} catch (Exception localException) {
			}
		}
	}

	public static int getMobId(int x, int y) {
		try {
			return Integer.parseInt(mobLayer[x][y][5][MadSand.curlayer]);
		} catch (Exception e) {
		}
		return 0;
	}

	private static int mobMoveToPlayerX(int mobx, int playerx) {
		if ((mobx - 1 == playerx) || (mobx + 1 == playerx))
			return 0;
		if (mobx - playerx < 0)
			return 1;
		if (mobx - playerx > 0) {
			return -1;
		}
		return 0;
	}

	private static int mobMoveToPlayerY(int moby, int playery) {
		if ((moby - 1 == playery) || (moby + 1 == playery))
			return 0;
		int delta = moby - playery;
		if (delta < 0)
			return 1;
		if (delta > 0) {
			return -1;
		}
		return 0;
	}

	public static void updateMobLogic() {
		int i = 0;
		int ii = 0;
		boolean moved = false;
		boolean fgt = false;
		boolean isn = false;
		int newx = 0;
		int newy = 0;
		int cou = 0;
		double delta = 0.0D;
		int moby = 0;
		while (i < MadSand.MAPSIZE) {
			while (ii < MadSand.MAPSIZE) {
				moved = false;
				int mobx = ii;
				moby = i;
				newx = mobx + mobMoveToPlayerX(mobx, MadSand.x);
				newy = moby + mobMoveToPlayerY(moby, MadSand.y);

				delta = MadSand.calcDistance(mobx * 33, moby * 33, MadSand.x * 33, MadSand.y * 33);
				if (mobLayer[ii][i][5][MadSand.curlayer] != "0") {
					if ((MadSand.calcDistance(MadSand.x * 33, MadSand.y * 33, ii * 33, i * 33) <= 99.0D)
							&& (getMobStat(ii, i, 8).equals("0"))
							&& (mobLayer[newx][newy][5][MadSand.curlayer].equals("0"))) {
						if ((mobMoveToPlayerX(mobx, MadSand.x) != 0) || (mobMoveToPlayerY(moby, MadSand.y) != 0)) {
							moved = true;
						}

						if (delta < 99.0D) {
							fgt = true;
							isn = true;
						}

						while (cou <= statsq - 1) {
							mobLayer[newx][newy][cou][MadSand.curlayer] = mobLayer[mobx][moby][cou][MadSand.curlayer];
							cou++;
						}
						cou = 0;
						if (moved)
							mobLayer[ii][i][5][MadSand.curlayer] = "0";
						if ((isn) && (fgt))
							mobAttack(ii, i);
					}
				}
				ii++;
			}
			i++;
			ii = 0;
		}
	}

	public static boolean isMobCollision(String dir) {
		if (getMobId(MadSand.x, MadSand.y, dir) != 0) {
			return true;
		}
		return false;
	}

	public static int getMobId(int x, int y, String dir) {
		int r = 0;
		try {
			if (dir.equals("up"))
				r = Integer.parseInt(mobLayer[x][(y + 1)][5][MadSand.curlayer]);
			if (dir.equals("down"))
				r = Integer.parseInt(mobLayer[x][(y - 1)][5][MadSand.curlayer]);
			if (dir.equals("left"))
				r = Integer.parseInt(mobLayer[(x - 1)][y][5][MadSand.curlayer]);
			if (dir.equals("right"))
				r = Integer.parseInt(mobLayer[(x + 1)][y][5][MadSand.curlayer]);
			return r;
		} catch (Exception e) {
			e.printStackTrace(Resource.eps);
			return 0;
		}
	}

	@SuppressWarnings("unlikely-arg-type")
	public static String getMobLoot(int x, int y, String dir) {
		String r = "nn";
		try {
			if ((!mobLayer[x][(y + 1)][5].equals(null)) || (!mobLayer[x][(y + 1)][5].equals("null"))) {
				if ((x - 1 == -1) || (y - 1 == -1)) {
					if (dir.equals("up")) {
						r = mobLayer[x][(y + 1)][3][MadSand.curlayer];
						ObjLayer.mx = x;
						ObjLayer.my = y + 1;
					}
					if (dir.equals("down")) {
						r = mobLayer[x][(y - 1)][3][MadSand.curlayer];
						ObjLayer.mx = x;
						ObjLayer.my = y - 1;
					}
					if (dir.equals("left")) {
						r = mobLayer[(x - 1)][y][3][MadSand.curlayer];
						ObjLayer.mx = x - 1;
						ObjLayer.my = y;
					}
					if (dir.equals("right")) {
						r = mobLayer[(x + 1)][y][3][MadSand.curlayer];
						ObjLayer.mx = x + 1;
						ObjLayer.my = y;
					}

				}
				return r;
			}
		} catch (Exception e) {
		}
		return "nn";
	}

	public static String getMobStat(int x, int y, int id) {
		if (x < MadSand.MAPSIZE && y < MadSand.MAPSIZE)
			return mobLayer[x][y][id][MadSand.curlayer];
		else
			return "error";
	}

	public static String getMobStat(int x, int y, int id, String dir) {
		if (x < MadSand.MAPSIZE && y < MadSand.MAPSIZE && x >= 0 && y >= 0) {
			if (dir.equals("left"))
				return mobLayer[x - 1][y][id][MadSand.curlayer];
			if (dir.equals("right"))
				return mobLayer[x + 1][y][id][MadSand.curlayer];
			if (dir.equals("up"))
				return mobLayer[x][y + 1][id][MadSand.curlayer];
			if (dir.equals("down"))
				return mobLayer[x][y - 1][id][MadSand.curlayer];
		} else
			return "error";
		return "error";
	}

	public static void setMobStat(int x, int y, int wx, int wy, int id, String val) {
		mobLayer[x][y][id][MadSand.curlayer] = val;
	}

	public static boolean isAlive(int x, int y) {
		if (Integer.parseInt(getMobStat(x, y, 0)) <= 0) {
			delMob(x, y, MadSand.curlayer);
			return false;
		}
		return true;
	}

	public static void delMob(int x, int y, int layer) {
		int i = 0;
		while (i <= statsq - 1) {
			mobLayer[x][y][i][layer] = "0";
			i++;
		}
		mobcount -= 1;
		if (mobcount < 1)
			mobcount = 0;
	}

	static void mobAttack(int x, int y) {
		Random random = new Random();
		String ename = getMobStat(x, y, 4);
		String gdmg = getMobStat(x, y, 6);
		int eacc = Integer.parseInt(getMobStat(x, y, 7));
		if (random.nextInt(eacc) != 0) {
			MadSand.print(ename + " hits you on " + gdmg + " hitpoints");
			PlayerStats.blood -= Integer.parseInt(gdmg) - PlayerStats.def[new Random().nextInt(3)];
		} else {
			MadSand.print(ename + " misses you");
		}
	}

	public static void updStatus(int x, int y, double rep) {
		if (rep < 0.0D) {
			setMobStat(x, y, MadSand.curxwpos, MadSand.curywpos, 8, "0");
		} else
			setMobStat(x, y, MadSand.curxwpos, MadSand.curywpos, 8, "1");
	}

	public static void fight(int x, int y) {
		if (getMobStat(x, y, 8).equals("1")) {
			MadSand.showDialog(1, "You hit friendly creature!", 0);
			if (getMobStat(x, y, 9).equals("Marauders")) {
				MadSand.MarauderRep -= 0.1D;
				updStatus(x, y, MadSand.MarauderRep);
			}
			if (getMobStat(x, y, 9).equals("Outlaws")) {
				MadSand.OutlawRep -= 0.1D;
				updStatus(x, y, MadSand.OutlawRep);
			}
			if (getMobStat(x, y, 9).equals("Partisans")) {
				MadSand.PartisanRep -= 0.1D;
				updStatus(x, y, MadSand.PartisanRep);
			}
		}
		int finat = 0;
		String ename = getMobStat(x, y, 4);
		String gdmg = getMobStat(x, y, 6);
		int rdmg = Integer.parseInt(gdmg) - PlayerStats.def[new Random().nextInt(3)];
		int eacc = Integer.parseInt(getMobStat(x, y, 7));
		if (getMobStat(x, y, 8).equals("0")) {
			if (Utils.random.nextInt(eacc) != 0) {
				MadSand.print(ename + " hits you on " + rdmg + " hitpoints");
				PlayerStats.blood -= rdmg;
			} else {
				MadSand.print(ename + " misses you");
			}
		}
		if (Utils.random
				.nextInt(PlayerStats.accur + WeaponWorker.getWAccBonus(WeaponWorker.getWid(PlayerStats.hand))) != 0) {
			int critical = 0;
			if (Utils.random.nextInt(PlayerStats.accur * 2) == 0) {
				critical = PlayerStats.atk + WeaponWorker.getWeaponAtk(WeaponWorker.getWid(PlayerStats.hand));
				MadSand.print("You made a critical hit!");
			}
			finat = critical + PlayerStats.atk + WeaponWorker.getWeaponAtk(WeaponWorker.getWid(PlayerStats.hand));
			if (MadSand.roguelike)
				Utils.makeTurn();
			MadSand.print("You hit " + ename + " on " + finat + " hitpoints");
			setMobStat(x, y, MadSand.curxwpos, MadSand.curywpos, 0,
					(Integer.parseInt(getMobStat(x, y, 0)) - finat) + "");
		} else {
			MadSand.print("You miss " + ename);
		}
		if (!isAlive(x, y)) {
			int reward = Integer.parseInt(getMobStat(x, y, 2));
			MadSand.print("You killed " + ename);
			MadSand.print("You gained " + reward + " EXP");
			PlayerStats.exp += reward;
			LootLayer.putLootQuery(x, y, getMobStat(x, y, 3));
		}
	}

	public static boolean isQuestMob(int mobId) {
		if (mobStats[mobId][11] != "-1") {
			Utils.out("Mob id " + mobId + " is a quest mob");
			return true;
		
		}
		else {
			Utils.out("Mob id " + mobId + " is NOT a quest mob");
			return false;
		}
	}

}
