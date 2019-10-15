package ru.bernarder.fallenrisefromdust;

import ru.bernarder.fallenrisefromdust.enums.Direction;
import ru.bernarder.fallenrisefromdust.enums.Faction;

public class Stats {
	final static int STR_WEIGHT_MULTIPLIER = 25;

	final static int AP_WALK = 5; // action points consumed by walking
	final static int AP_MINOR = 1; // action points consumed by minor action

	final static int STARVE_DMG = 1;
	final static int FOOD_HEAL = 3;

	public Item hand;

	public int actionPtsMax = 5;
	public int actionPts = actionPtsMax;

	public final int maxFood = 1000;
	public final int satiatedVal = (int) (maxFood * 0.9);
	public int food = maxFood;
	
	public long spawnTime = 0;

	public int accur = 2; // ACCUR
	public int hp = 200; // CONSTITUTION*10
	public int mhp = 200;
	public int str = 3;// ATK
	public int luck = 1; // LUCK
	public int dexterity = 1; // DEX
	public int intelligence = 1; // INT
	public int stamina = 50; // STAMINA*5
	public int maxstamina = 50;
	
	public int air = 3;

	public int respawnX = -1;
	public int respawnY = -1;
	public int respawnWX = -1, respawnWY = -1;

	public int[] def = new int[3];

	public int helmet = 0;
	public int cplate = 0;
	public int shield = 0;

	public SkillContainer skills = new SkillContainer();

	public Faction faction;
	public Direction look = Direction.DOWN;

	public String name;

	public boolean dead = false;

	StatAction actions;

	public void check() {
		skills.check();

		if (food > maxFood)
			food = maxFood;

		if (food < 0)
			food = 0;

		if (stamina > maxstamina)
			stamina = maxstamina;

		if (stamina < 0)
			stamina = 0;

		if (hp > mhp)
			hp = mhp;

		if (hp <= 0) {
			hp = 0;
			dead = true;
			actions._die();
		}
	}

	public void perTickCheck() {
		--food;

		if (food <= 0)
			actions._damage(STARVE_DMG);

		if (food >= satiatedVal)
			actions._heal(FOOD_HEAL);
	}
}
