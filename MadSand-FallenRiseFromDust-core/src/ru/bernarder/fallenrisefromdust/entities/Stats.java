package ru.bernarder.fallenrisefromdust.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ru.bernarder.fallenrisefromdust.Utils;
import ru.bernarder.fallenrisefromdust.entities.inventory.EquipStats;
import ru.bernarder.fallenrisefromdust.entities.inventory.Item;
import ru.bernarder.fallenrisefromdust.enums.Direction;
import ru.bernarder.fallenrisefromdust.enums.Faction;
import ru.bernarder.fallenrisefromdust.enums.ItemType;

public class Stats {
	public final static int WEIGHT_MULTIPLIER = 5;

	public int AP_WALK = 5; // action points consumed by walking
	public int AP_ATTACK = 3;
	public int AP_MINOR = 1; // action points consumed by minor action

	final static int STARVE_DMG = 1;
	final static int FOOD_HEAL = 1;

	static final int STAT_MIN_SUM = 15;
	static final int STAT_MAX_SUM = 22;
	static final int STAT_RAND_MAX = 9;

	@JsonIgnore
	public Item hand = Item.nullItem;
	@JsonIgnore
	public Item offHand = Item.nullItem;

	@JsonIgnore
	public Item headEquip = Item.nullItem;
	@JsonIgnore
	public Item chestEquip = Item.nullItem;
	@JsonIgnore
	public Item legsEquip = Item.nullItem;

	public int actionPtsMax = 5;
	public int actionPts = actionPtsMax;

	public float satiationFactor = 0.9f;
	public final int maxFood = 1000;
	public final int satiatedVal = (int) (maxFood * satiationFactor);
	public int food = maxFood;

	public long spawnTime = 0;

	public int hp;
	public int mhp;
	public int stamina = 50;
	public int maxstamina = 50;

	public int accuracy = 2;
	public int constitution;
	public int strength = 3;
	public int luck = 1;
	public int dexterity = 1;
	public int intelligence = 1;

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

	@JsonIgnore
	public StatAction owner;

	// public Stats(Entity owner)

	public boolean equip(Item item) {
		switch (item.type) {
		case HeadArmor:
			headEquip = item;
			break;
		case ChestArmor:
			chestEquip = item;
			break;
		case LegArmor:
			legsEquip = item;
			break;

		default:
			return false;
		}
		applyBonus(item);
		return true;
	}

	public boolean unequip(Item item) {
		switch (item.type) {
		case HeadArmor:
			headEquip = Item.nullItem;
			break;
		case ChestArmor:
			chestEquip = Item.nullItem;
			break;
		case LegArmor:
			legsEquip = Item.nullItem;
			break;

		default:
			return false;
		}
		removeBonus(item);
		return true;
	}

	public void applyBonus(Item item) {
		if (item.type == ItemType.HeadArmor && headEquip == item)
			return;
		if (item.type == ItemType.ChestArmor && chestEquip == item)
			return;
		if (item.type == ItemType.LegArmor && legsEquip == item)
			return;

		EquipStats bonus = item.equipStats;
		constitution += bonus.constitution;
		dexterity += bonus.dexterity;
		strength += bonus.strength;
		accuracy += bonus.accuracy;
		intelligence += bonus.intelligence;
	}

	public void removeBonus(Item item) {
		if (item.type == ItemType.HeadArmor && headEquip != item)
			return;
		if (item.type == ItemType.ChestArmor && chestEquip != item)
			return;
		if (item.type == ItemType.LegArmor && legsEquip != item)
			return;

		EquipStats bonus = item.equipStats;
		constitution -= bonus.constitution;
		dexterity -= bonus.dexterity;
		strength -= bonus.strength;
		accuracy -= bonus.accuracy;
		intelligence -= bonus.intelligence;
	}

	public void calcActionCosts() {
		// AP_MINOR = ;
		// AP_WALK = ;
		// AP_ATTACK = ;
		// actionPtsMax = ;
	}

	public void roll() {
		int sum = 0;
		while (sum < STAT_MIN_SUM || sum > STAT_MAX_SUM) {
			strength = Utils.rand(1, STAT_RAND_MAX);
			constitution = Utils.rand(1, STAT_RAND_MAX);
			accuracy = Utils.rand(1, STAT_RAND_MAX);
			luck = Utils.rand(1, STAT_RAND_MAX);
			dexterity = Utils.rand(1, STAT_RAND_MAX);
			intelligence = Utils.rand(1, STAT_RAND_MAX);
			sum = getSum();
		}

		hp = constitution * 10;
		mhp = hp;

		stamina = ((dexterity + constitution) / 2) * 5;
		maxstamina = stamina;
	}

	@JsonIgnore
	public int getSum() {
		return strength + constitution + accuracy + luck + dexterity + intelligence;
	}

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
			owner._die();
		}
	}

	public void perTickCheck() {
		--food;

		if (food <= 0)
			owner._damage(STARVE_DMG);

		if (food >= satiatedVal)
			owner._heal(FOOD_HEAL);
	}

	public boolean attackMissed() {
		return (Utils.rand(0, accuracy) == accuracy);
	}

	public int calcAttack() {
		if (attackMissed())
			return 0;

		int weaponStr;
		if (!hand.type.isWeapon())
			weaponStr = 0;
		else
			weaponStr = hand.equipStats.strength;

		int atk = (strength + weaponStr);

		return atk;
	}

	public int calcMaxInventoryWeight() {
		return (strength + dexterity) * WEIGHT_MULTIPLIER;
	}
}
