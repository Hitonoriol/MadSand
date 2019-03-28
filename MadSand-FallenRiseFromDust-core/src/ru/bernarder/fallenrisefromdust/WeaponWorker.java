package ru.bernarder.fallenrisefromdust;

public class WeaponWorker {
	static int[][] wstats;

	static void init() {
		wstats = new int[101][3];

		wstats[0][0] = 12;
		wstats[0][1] = 1;
		wstats[0][2] = 3;

		wstats[1][0] = 20;
		wstats[1][1] = 3;
		wstats[1][2] = 50;

		wstats[2][0] = 13;
		wstats[2][1] = 5;
		wstats[2][2] = 9;

		wstats[100][0] = 0;
		wstats[100][1] = 0;
		wstats[100][2] = 0;
	}

	static int getWeaponAtk(int wid) {
		return wstats[wid][0];
	}

	static int getWid(int invid) {
		if (invid == 12)
			return 0;
		if (invid == 20)
			return 1;
		if (invid == 13) {
			return 2;
		}
		return 100;
	}

	public static int getWAccBonus(int wid) {
		return wstats[wid][2];
	}
}
