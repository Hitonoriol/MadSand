package hitonoriol.madsand.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.Utils;
import hitonoriol.madsand.entities.inventory.EquipStats;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.enums.Faction;
import hitonoriol.madsand.enums.Skill;
import hitonoriol.madsand.enums.Stat;

public class Stats {
	final static double PERCENT = 100.0;
	public final static float WEIGHT_MULTIPLIER = 7.5f;
	public final static float BASE_MAX_WEIGHT = 50;
	public final static int BASE_FOOD_TICKS = 1;
	public static final int HP_MULTIPLIER = 10; // maxHp = constitution * HP_MULTIPLIER
	final float MIN_HP_AUTODAMAGE_PERCENT = 10;
	final static int STARVE_DMG = 1;
	final static int STAMINA_DMG = 1;
	final static int FOOD_HEAL = 1;

	public StatContainer baseStats;

	public int apRegenRate = 2;
	
	public double walkCost = 4.5; // action points consumed by walking
	public double attackCost = 4.25;
	public double minorCost = 1; // action points consumed by minor action
	public double actionPtsMax = 5; // Entity's speed
	public double actionPts = actionPtsMax;

	private int statBonus = 0; // Total equipment stat bonus

	public long spawnTime = 0;

	public int hp;
	public int mhp;

	public float minorStaminaCost = 0.09f;
	public float staminaLowPercent = 10;
	public float stamina;
	public float maxstamina;

	public int air = 3;

	public boolean hasRespawnPoint = false;
	public int respawnX = -1;
	public int respawnY = -1;
	public int respawnWX = -1, respawnWY = -1;

	public SkillContainer skills = new SkillContainer();

	@JsonIgnore
	public Equipment equipment;

	public double satiatedPercent = 90;
	public final int maxFood = 1000;
	public int foodTicks = skills.getLvl(Skill.Survival);
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
		baseStats = new StatContainer();
	}

	public int get(Stat stat) {
		return baseStats.get(stat);
	}

	public void set(Stat stat, int value) {
		baseStats.set(stat, value);
	}
	
	public void roll(int lvl) {
		baseStats.roll(lvl);
		calcStats();
	}

	public void roll() {
		roll(0);
	}

	public void calcStats() {
		hp = get(Stat.Constitution) * HP_MULTIPLIER;
		mhp = hp;

		stamina = ((get(Stat.Dexterity) + get(Stat.Constitution)) / 2) * 5;
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
		baseStats.add(bonus.stats);
		statBonus += bonus.getTotalBonus();
		calcStats();
	}

	public void removeBonus(Item item) {
		if (!item.type.isEquipment())
			return;

		EquipStats bonus = item.equipStats;
		baseStats.sub(bonus.stats);
		statBonus -= bonus.getTotalBonus();
		calcStats();
	}

	public void calcActionCosts() {
		// AP_MINOR = ;
		// AP_WALK = ;
		// AP_ATTACK = ;
		int dexterity = get(Stat.Dexterity);
		if (dexterity < 2)
			actionPtsMax = 1;
		else
			actionPtsMax = (Utils.log(Math.pow(dexterity + 0.1, 1.7), 5)
					/ (3 + ((2 * Math.sqrt(dexterity)) / dexterity)))
					* 9.9;
		actionPts = actionPtsMax;
	}

	@JsonIgnore
	public int getSum() {
		return baseStats.getSum() - statBonus;
	}

	@JsonIgnore
	public double getSatiationPercent() {
		return PERCENT * ((double) food / (double) maxFood);
	}

	@JsonIgnore
	public double getStaminaPercent() {
		return PERCENT * ((double) stamina / (double) maxstamina);
	}

	@JsonIgnore
	public double getHpPercent() {
		return PERCENT * ((double) hp / (double) mhp);
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

		perTickFoodCheck();
		perTickStaminaCheck();

		if (getHpPercent() > MIN_HP_AUTODAMAGE_PERCENT) {
			if (getStaminaPercent() < staminaLowPercent && !skills.skillRoll(Skill.Survival))
				owner._damage(STAMINA_DMG);

			if (food <= 0)
				owner._damage(STARVE_DMG);
		}

		if (getSatiationPercent() >= satiatedPercent && skills.skillRoll(Skill.Survival))
			owner._heal(FOOD_HEAL);

		check();
	}

	private void perTickStaminaCheck() {
		double survivalPercent = skills.getSkillRollPercent(Skill.Survival);
		double reqSatiationPercent = 95.0 - survivalPercent;
		if (getSatiationPercent() < reqSatiationPercent)
			return;

		stamina += (survivalPercent * 0.02);
		check();

	}

	/*
	 * Max food ticks = Survival skill level + base food ticks
	 * 
	 * Decrement food ticks on unsuccessful skill roll
	 * When food ticks < 0, decrement food level
	 */
	private void perTickFoodCheck() {

		if (!skills.skillRoll(Skill.Survival)) {
			--foodTicks;

			if (foodTicks < 0) {
				--food;
				foodTicks = skills.getLvl(Skill.Survival) + BASE_FOOD_TICKS;
			}
		}

	}

	public boolean luckRoll() {
		int luck = get(Stat.Constitution);
		int roll = Utils.rand(0, luck);
		return (roll != luck);
	}

	public boolean critRoll() {
		return Utils.percentRoll(Math.log(Math.pow(get(Stat.Accuracy) + 0.1, 7)));
	}

	public boolean attackMissed() {
		int accuracy = get(Stat.Constitution);
		return (Utils.rand(0, accuracy) == accuracy);
	}

	final float DEF_DENOMINATOR = 3f;

	public int calcAttack(int defense) {
		if (attackMissed())
			return 0;

		int atk = (int) (get(Stat.Strength) - (defense / DEF_DENOMINATOR));

		if (atk <= 0)
			atk = 1;

		if (critRoll())
			atk += atk * 0.25;
		else
			atk = Utils.rand(atk / 2, atk);

		return atk;
	}

	public float calcMaxInventoryWeight() {
		return BASE_MAX_WEIGHT + (get(Stat.Strength) + get(Stat.Dexterity)) * WEIGHT_MULTIPLIER;
	}

	public boolean rollEncounter() {
		double chance = (100 / (get(Stat.Luck) + get(Stat.Dexterity) * 0.75));
		Utils.out("Encounter chance: " + chance);
		return Utils.percentRoll(chance);
	}
}
