package ru.bernarder.fallenrisefromdust;

import com.badlogic.gdx.math.Vector2;

import ru.bernarder.fallenrisefromdust.enums.Direction;
import ru.bernarder.fallenrisefromdust.strings.InventoryNames;

import java.util.Random;

public class InvUtils {
	public static int getFreeCell(int id) {
		int cnum = 0;
		boolean found = false;
		int i = 0;
		while (i <= 29) {
			if (getSameCell(id, 1) != -1) {
				cnum = getSameCell(id, 1);
				found = true;
				break;
			}
			if (MadSand.inv[i][0] == 0) {
				cnum = i;
				MadSand.inv[i][1] = 0;
				found = true;
				break;
			}
			i++;
		}
		if (found) {
			return cnum;
		}
		return -1;
	}

	public static int getFreeCellT(int id) {
		int cnum = 0;
		boolean found = false;
		int i = 0;
		while (i <= 29) {
			if (MadSand.trade[i][0] == 0) {
				cnum = i;
				found = true;
				break;
			}
			if ((MadSand.trade[i][0] == id) && (MadSand.trade[i][1] <= 999)) {
				cnum = i;
				found = true;
				break;
			}
			i++;
		}
		if (found) {
			return cnum;
		}
		return -1;
	}

	public static int getType(int id) {
		try {
			return InventoryNames.type.get(id);
		} catch (Exception e) {
			return 0;
		}
	}

	public static void checkHands(int id) {
		if (getSameCell(id, 1) == -1)
			MadSand.player.hand = 0;
	}

	public static int getAltObject(int id) {
		return InventoryNames.altObject.get(id);
	}

	public static int getSameCell(int id, int q) {
		int cnum = 0;
		boolean found = false;
		int i = 0;
		while (i <= 29) {
			if (MadSand.inv[i][0] == id) {
				cnum = i;
				found = true;
				break;
			}
			i++;
		}
		if (found) {
			return cnum;
		}
		return -1;
	}

	public static int getSameCellT(int id, int q) {
		int cnum = 0;
		boolean found = false;
		int i = 0;
		while (i <= 29) {
			if ((MadSand.trade[i][0] == id) && (MadSand.trade[i][1] >= q)) {
				cnum = i;
				found = true;
				break;
			}
			i++;
		}
		if (found) {
			return cnum;
		}
		return -1;
	}

	public static void emptyInv() {
		int i = 0;
		while (i < 30) {
			MadSand.inv[i][0] = 0;
			i++;
		}
	}

	public static void emptyInvT() {
		int i = 0;
		while (i < 30) {
			MadSand.trade[i][0] = 0;
			MadSand.trade[i][1] = 0;
			i++;
		}
	}

	public static boolean putItem(int id, int quantity, boolean silent) {
		Utils.out("ITEM PUT " + quantity + " " + MadSand.inv[getFreeCell(id)][1]);
		boolean r = false;
		if (getFreeCell(id) != -1) {
			MadSand.inv[getFreeCell(id)][0] = id;
			int finalQuantity = quantity + MadSand.inv[getFreeCell(id)][1];
			MadSand.inv[getFreeCell(id)][1] = finalQuantity;
			MadSand.inv[getFreeCell(id)][2] = InventoryNames.type.get(id);
			r = true;
			new ThreadedUtils().invSend.start();
		}
		if (!silent) {
			MadSand.print("You got " + LootLayer.getTextQuery(":" + id + "/" + quantity));
		}
		return r;
	}

	public static boolean putItemT(int id, int quantity, int trade) {
		boolean r = false;
		if (getFreeCellT(id) != -1) {
			MadSand.trade[getFreeCellT(id)][0] = id;
			int finalQuantity = quantity + MadSand.trade[getFreeCellT(id)][1];
			MadSand.trade[getFreeCellT(id)][1] = finalQuantity;
			MadSand.trade[getFreeCellT(id)][2] = (new Random().nextInt(id) + 10);
			r = true;
		}
		return r;
	}

	public static boolean delItem(int id, int quantity) {
		boolean r = false;
		if (getSameCell(id, quantity) != -1) {
			int cid = getSameCell(id, quantity);
			int finalQuantity = MadSand.inv[cid][1] - quantity;
			if (finalQuantity == 0) {
				MadSand.inv[cid][0] = 0;
				r = true;
			} else if (finalQuantity < 0) {
				r = false;
			} else {
				MadSand.inv[cid][1] = finalQuantity;
				r = true;
			}
			new ThreadedUtils().invSend.start();
		}
		return r;
	}

	public static boolean delItemT(int id, int quantity) {
		boolean r = false;
		if (getSameCellT(id, quantity) != -1) {
			int cid = getSameCellT(id, quantity);
			int finalQuantity = MadSand.trade[cid][1] - quantity;
			if (finalQuantity == 0) {
				MadSand.trade[cid][0] = 0;
				r = true;
			} else if (finalQuantity < 0) {
				r = false;
			} else {
				MadSand.trade[cid][1] = finalQuantity;
				r = true;
			}
		}
		return r;
	}

	public static boolean dropItem(int id, int quantity) {
		boolean r = false;
		if (getSameCell(id, quantity) != -1) {
			int cid = getSameCell(id, quantity);
			int finalQuantity = MadSand.inv[cid][1] - quantity;
			if (finalQuantity == 0) {
				MadSand.inv[cid][0] = 0;
				r = true;
			}
			if (finalQuantity < 0) {
				r = false;
			} else {
				MadSand.inv[cid][1] = finalQuantity;
				if (MadSand.player.look == Direction.UP)
					LootLayer.putLoot(MadSand.x, MadSand.y + 1, id, quantity);
				if (MadSand.player.look == Direction.DOWN)
					LootLayer.putLoot(MadSand.x, MadSand.y - 1, id, quantity);
				if (MadSand.player.look == Direction.LEFT)
					LootLayer.putLoot(MadSand.x - 1, MadSand.y, id, quantity);
				if (MadSand.player.look == Direction.RIGHT)
					LootLayer.putLoot(MadSand.x + 1, MadSand.y, id, quantity);
				r = true;
			}
			new ThreadedUtils().invSend.start();
		}
		return r;
	}

	public static Vector2 getItemCellCoord(int x) {
		int y = 2;
		int xx = 81;
		if ((x > -1) && (x < 5)) {
			y = 2;
			if (x == 0)
				xx = 81;
			if (x == 1)
				xx = 165;
			if (x == 2)
				xx = 247;
			if (x == 3)
				xx = 329;
			if (x == 4)
				xx = 411;
		}
		if ((x > 4) && (x < 10)) {
			y = 84;
			if (x == 5)
				xx = 81;
			if (x == 6)
				xx = 165;
			if (x == 7)
				xx = 247;
			if (x == 8)
				xx = 329;
			if (x == 9)
				xx = 411;
		}
		if ((x > 9) && (x < 15)) {
			y = 166;
			if (x == 10)
				xx = 81;
			if (x == 11)
				xx = 165;
			if (x == 12)
				xx = 247;
			if (x == 13)
				xx = 329;
			if (x == 14)
				xx = 411;
		}
		if ((x > 14) && (x < 20)) {
			y = 248;
			if (x == 15)
				xx = 81;
			if (x == 16)
				xx = 165;
			if (x == 17)
				xx = 247;
			if (x == 18)
				xx = 329;
			if (x == 19)
				xx = 411;
		}
		if ((x > 19) && (x < 25)) {
			y = 330;
			if (x == 20)
				xx = 81;
			if (x == 21)
				xx = 165;
			if (x == 22)
				xx = 247;
			if (x == 23)
				xx = 329;
			if (x == 24)
				xx = 411;
		}
		if ((x > 24) && (x <= 39)) {
			y = 412;
			if (x == 25)
				xx = 81;
			if (x == 26)
				xx = 165;
			if (x == 27)
				xx = 247;
			if (x == 28)
				xx = 329;
			if (x == 29)
				xx = 411;
		}
		Vector2 ret = new Vector2(xx - 80, y);
		return ret;
	}

	public static int getItemCell(int x, int y) {
		int xx = 0;
		if ((y >= 2) && (y <= 84)) {
			if ((x >= 1) && (x <= 83))
				xx = 0;
			if ((x >= 83) && (x <= 165))
				xx = 1;
			if ((x >= 165) && (x <= 247))
				xx = 2;
			if ((x >= 247) && (x <= 329))
				xx = 3;
			if ((x >= 329) && (x <= 411))
				xx = 4;
		}
		if ((y >= 84) && (y <= 166)) {
			if ((x >= 1) && (x <= 83))
				xx = 5;
			if ((x >= 83) && (x <= 165))
				xx = 6;
			if ((x >= 165) && (x <= 247))
				xx = 7;
			if ((x >= 247) && (x <= 329))
				xx = 8;
			if ((x >= 329) && (x <= 411))
				xx = 9;
		}
		if ((y >= 166) && (y <= 248)) {
			if ((x >= 1) && (x <= 83))
				xx = 10;
			if ((x >= 83) && (x <= 165))
				xx = 11;
			if ((x >= 165) && (x <= 247))
				xx = 12;
			if ((x >= 247) && (x <= 329))
				xx = 13;
			if ((x >= 329) && (x <= 411))
				xx = 14;
		}
		if ((y >= 248) && (y <= 330)) {
			if ((x >= 1) && (x <= 83))
				xx = 15;
			if ((x >= 83) && (x <= 165))
				xx = 16;
			if ((x >= 165) && (x <= 247))
				xx = 17;
			if ((x >= 247) && (x <= 329))
				xx = 18;
			if ((x >= 329) && (x <= 411))
				xx = 19;
		}
		if ((y >= 330) && (y <= 412)) {
			if ((x >= 1) && (x <= 83))
				xx = 20;
			if ((x >= 83) && (x <= 165))
				xx = 21;
			if ((x >= 165) && (x <= 247))
				xx = 22;
			if ((x >= 247) && (x <= 329))
				xx = 23;
			if ((x >= 329) && (x <= 411))
				xx = 24;
		}
		if ((y >= 412) && (y <= 494)) {
			if ((x >= 1) && (x <= 83))
				xx = 25;
			if ((x >= 83) && (x <= 165))
				xx = 26;
			if ((x >= 165) && (x <= 247))
				xx = 27;
			if ((x >= 247) && (x <= 329))
				xx = 28;
			if ((x >= 329) && (x <= 411))
				xx = 29;
		}
		return xx;
	}

	public static int getItemInfo(int num, int x) {
		int ret = 0;
		ret = MadSand.inv[num][x];
		return ret;
	}

	InventoryNames inn = new InventoryNames();

	public static int getItemInfoT(int num, int x) {
		int ret = 0;
		ret = MadSand.trade[num][x];
		return ret;
	}

	public static String getItemName(int id) {
		return InventoryNames.name.get(id);
	}

	public static int getItemIdByCursorCoord() {
		return getItemInfo(getItemCell(MadSand.cx, MadSand.cy), 0);
	}

	public static int getItemIdByCursorCoordT() {
		return getItemInfoT(getItemCell(MadSand.cx, MadSand.cy), 0);
	}

	public static int getItemPriceByCursorCoord(int id) {
		try {
			return InventoryNames.cost.get(id);
		} catch (Exception e) {
		}
		return 0;
	}
}