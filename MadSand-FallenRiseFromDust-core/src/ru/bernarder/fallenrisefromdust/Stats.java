package ru.bernarder.fallenrisefromdust;

import ru.bernarder.fallenrisefromdust.enums.Direction;
import ru.bernarder.fallenrisefromdust.enums.Faction;

public class Stats {
	final static int STR_WEIGHT_MULTIPLIER = 25;
	
	final static int AP_WALK = 5;	//action points consumed by walking
	final static int AP_MINOR = 1;	//action points consumed by minor action

	public int hand = 0;

	public int actionPtsMax = 5;
	public int actionPts = actionPtsMax;

	public final int maxFood = 1000;
	int food = maxFood;

	public int accur = 2; // ACCUR
	public int hp = 200; // CONSTITUTION*10
	public int mhp = 200;
	public int str = 3;// ATK
	public int luck = 1; // LUCK
	public int dexterity = 1; // DEX
	public int intelligence = 1; // INT
	public float stamina = 50.0F; // STAMINA*5
	public float maxstamina = 50.0F;

	int respawnX = -1;
	int respawnY = -1;
	int respawnWX, respawnWY;

	public int[] def = new int[3];

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

	Faction faction;
	Direction look = Direction.DOWN;

	String name;

	boolean dead = false;

	public void check() {
		if (exp >= requiredexp) {
			lvl += 1;
			exp = 0;
			requiredexp *= 2;
			MadSand.print("You leveled up!");
			str += 1;
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
			dead = true;
		}
	}
}
