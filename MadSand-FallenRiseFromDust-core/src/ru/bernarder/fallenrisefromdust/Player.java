package ru.bernarder.fallenrisefromdust;


import com.badlogic.gdx.Gdx;

import ru.bernarder.fallenrisefromdust.enums.*;

public class Player {

	public int hand = 0;
	public int accur = 2; // ACCUR
	public int hp = 200; // CONSTITUTION*10
	public int mhp = 200;
	public int atk = 3;// ATK
	public int luck = 1; // LUCK
	public int dexterity = 1; // DEX
	public int intelligence = 1; // INT
	public float stamina = 50.0F; // STAMINA*5
	public float maxstamina = 50.0F;
	public int[] def = new int[3];

	public int[] rest = { -1, -1, -1, -1 };

	public int helmet = 0;
	public int cplate = 0;
	public int shield = 0;

	public int lvl = 0;
	public int exp = 0;
	public int requiredexp = 100;

	public int[] woodcutterskill = { 1, 0, 50 };
	public int[] miningskill = { 1, 0, 50 };
	public int[] survivalskill = { 1, 0, 65 };
	public int[] harvestskill = { 1, 0, 35 };
	public int[] craftingskill = { 1, 0, 30 };

	float speed, splim; // moves/actions per world tick
	String name;
	boolean isMain;
	Faction faction;
	Direction look;

	public Player(String name) {
		this.name = name;
	}

	boolean interact(final int x, final int y, final Direction direction) {
		// TODO
		return true;
	}

	public static boolean isCollision(int x, int y, Direction direction, int flag) {
		boolean collision = false;
		int oid = MadSand.world.getCurLoc().getObject(x, y, direction).id;
		if (((flag == 0) && (oid == 12)) || (oid == 0) || (oid == 666)) {
			collision = false;
		} else
			collision = true;
		return collision;
	}

	public static boolean isCollisionMask(int x, int y) {
		if (x < MadSand.MAPSIZE && y < MadSand.MAPSIZE) {
			if (MadSand.world.getCurLoc().getObject(x, y).id == 666) {
				return true;
			}
		}
		return false;
	}

	static boolean standingOnLoot(int x, int y) {
		String ass = LootLayer.getCell(x, y);
		int n = ass.length();
		if (n == "n".length()) {
			return false;
		}
		return true;
	}

	void skillBonusItems(int x, int y, String direction, int id) {
		// Idk what's this thing
	}

	void damagePlayer(int to) {
		hp -= to;
	}

	void healPlayer(int to) {
		if (hp + to < mhp) {
			hp += to;
		} else {
			hp = mhp;
		}
	}

	void increaseStamina(int to) {
		if (stamina + to < maxstamina) {
			stamina += to;
		} else {
			stamina = maxstamina;
		}
	}

	public void checkStats() {
		if (exp >= requiredexp) {
			lvl += 1;
			exp = 0;
			requiredexp *= 2;
			MadSand.print("You've leveled up!");
			atk += 1;
			mhp += 50;
			maxstamina += 15;
		}

		if (stamina > maxstamina) {
			stamina = maxstamina;
		}
		if (stamina < 0.0F) {
			stamina = 0.0F;
		}

		if (hp > mhp) {
			hp = mhp;
		}

		if (hp <= 0) {
			hp = 0;
			Gdx.input.setInputProcessor(Gui.dead);
			MadSand.state = "DEAD";
			InvUtils.emptyInv();
		}
	}
}