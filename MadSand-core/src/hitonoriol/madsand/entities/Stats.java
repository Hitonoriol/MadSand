package hitonoriol.madsand.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.Utils;
import hitonoriol.madsand.entities.inventory.EquipStats;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.enums.Faction;
import hitonoriol.madsand.enums.Skill;

public class Stats {
	public final static float WEIGHT_MULTIPLIER = 7.5f;
	public final static float BASE_MAX_WEIGHT = 50;

	public int AP_WALK = 5; // action points consumed by walking
	public int AP_ATTACK = 3;
	public int AP_MINOR = 1; // action points consumed by minor action

	final static int STARVE_DMG = 1;
	final static int STAMINA_DMG = 3;
	final static int FOOD_HEAL = 1;

	static int STAT_MIN_SUM = 20; //for roll() method
	public int maxStatSum = 22; // Changes every levelup
	private int statBonus = 0; // Total equipment stat bonus

	static final int STAT_RAND_MAX = 8;
	static final int STAT_RAND_MIN = 3;

	static int STAMINA_INTERACT_COST = 5;

	public int actionPtsMax = 5;
	public int actionPts = actionPtsMax;

	public long spawnTime = 0;

	public int hp;
	public int mhp;

	public static final float staminaLow = 0.15f;
	public int stamina;
	public int maxstamina;

	public int defense = 0;
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

	public SkillContainer skills = new SkillContainer();

	@JsonIgnore
	public Equipment equipment;

	public float satiationFactor = 0.95f;
	public final int maxFood = 1000;
	public int foodTicks = skills.getLvl(Skill.Survival);
	public final int satiatedVal = (int) (maxFood * satiationFactor);
	public int food = maxFood;

	public Faction faction = Faction.None;
	public Direction look = Direction.DOWN;

	public String name;

	public boolean dead = false;

	@JsonIgnore
	public StatAction owner;

	public Stats() {
		this(false);
	}

	public Stats(boolean isPlayer) {
		equipment = new Equipment(this, isPlayer);
	}

	public void calcStats() {
		hp = constitution * 10;
		mhp = hp;

		stamina = ((dexterity + constitution) / 2) * 5;
		maxstamina = stamina;

		calcActionCosts();
	}

	public Item hand() {
		return equipment.getHand();
	}

	@JsonIgnore
	public void setHand(Item item) {
		equipment.setHand(item);
	}

	public boolean equip(Item item) {
		return equipment.equip(item);
	}

	public boolean unequip(Item item) {
		return equipment.unEquip(item);
	}

	public void applyBonus(Item item) {
		if (!item.type.isEquipment())
			return;

		EquipStats bonus = item.equipStats;

		constitution += bonus.constitution;
		dexterity += bonus.dexterity;
		strength += bonus.strength;
		accuracy += bonus.accuracy;
		intelligence += bonus.intelligence;
		defense += bonus.defense;

		statBonus += bonus.getTotalBonus();
		calcStats();
	}

	public void removeBonus(Item item) {
		if (!item.type.isEquipment())
			return;

		EquipStats bonus = item.equipStats;

		constitution -= bonus.constitution;
		dexterity -= bonus.dexterity;
		strength -= bonus.strength;
		accuracy -= bonus.accuracy;
		intelligence -= bonus.intelligence;
		defense -= bonus.defense;

		statBonus -= bonus.getTotalBonus();
		calcStats();
	}

	public void calcActionCosts() {
		// AP_MINOR = ;
		// AP_WALK = ;
		// AP_ATTACK = ;
		actionPtsMax = dexterity;
		actionPts = actionPtsMax;
	}

	public void roll() {
		int sum = 0;
		while (sum < STAT_MIN_SUM || sum > maxStatSum) {
			strength = Utils.rand(STAT_RAND_MIN, STAT_RAND_MAX);
			constitution = Utils.rand(STAT_RAND_MIN, STAT_RAND_MAX);
			accuracy = Utils.rand(STAT_RAND_MIN, STAT_RAND_MAX);
			luck = Utils.rand(STAT_RAND_MIN, STAT_RAND_MAX);
			dexterity = Utils.rand(STAT_RAND_MIN, STAT_RAND_MAX);
			intelligence = Utils.rand(STAT_RAND_MIN, STAT_RAND_MAX);
			sum = getSum();
		}

		calcStats();
	}

	@JsonIgnore
	public int getSum() {
		return (strength +
				constitution +
				accuracy +
				luck +
				dexterity +
				intelligence) - statBonus;
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
		if (stamina < maxstamina * staminaLow)
			owner._damage(STAMINA_DMG);

		perTickFoodCheck();

		if (food <= 0)
			owner._damage(STARVE_DMG);

		if (food >= satiatedVal)
			owner._heal(FOOD_HEAL);
	}

	/*
	 * Max food ticks = Survival skill level
	 * 
	 * Decrement food ticks on unsuccessful skill roll
	 * When food ticks <= 0, decrement food level
	 */
	private void perTickFoodCheck() {

		if (!skills.skillRoll(Skill.Survival)) {
			--foodTicks;

			if (foodTicks <= 0) {
				--food;
				foodTicks = skills.getLvl(Skill.Survival);
			}
		}

	}

	public void interactStamina() {
		stamina -= (STAMINA_INTERACT_COST);
	}

	public boolean luckRoll() {
		int roll = Utils.rand(0, luck);
		return (roll != luck);
	}

	public boolean attackMissed() {
		return (Utils.rand(0, accuracy) == accuracy);
	}

	public int calcAttack(int defense) {
		if (attackMissed())
			return 0;

		int atk = (strength - defense);

		if (atk <= 0)
			atk = 1;

		Utils.out("CalcAttack with defense: " + defense + " | Strength: " + strength + "; ATK: " + atk);

		return atk;
	}

	public float calcMaxInventoryWeight() {
		return BASE_MAX_WEIGHT + (strength + dexterity) * WEIGHT_MULTIPLIER;
	}
}
