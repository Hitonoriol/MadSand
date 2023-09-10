package hitonoriol.madsand.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import hitonoriol.madsand.entities.npc.Npc;
import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.util.Utils;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY)
@JsonSubTypes({ @Type(PlayerStats.class) })
public class Stats {
	final static double PERCENT = 100.0;
	public final static float WEIGHT_MULTIPLIER = 7.5f;
	public final static float BASE_MAX_WEIGHT = 50;
	public final static int BASE_FOOD_TICKS = 2;
	public static final int HP_MULTIPLIER = 10; // maxHp = constitution * HP_MULTIPLIER
	final float MIN_HP_AUTODAMAGE_PERCENT = 40;
	final static int STARVE_DMG = 1;
	final static int STAMINA_DMG = 1;
	final static int FOOD_HEAL = 1;

	public BaseStats baseStats;

	public double walkCost = 4.5; // action points consumed by walking
	public double meleeAttackCost = 5;
	public double rangedAttackCost = 5.5;
	public double minorCost = 1; // action points consumed by minor action
	public double actionPtsMax = 5; // Entity's speed
	public double actionPts = actionPtsMax;

	public long spawnTime = 0;
	public long spawnRealTime = 0;

	public int hp = 0;
	public int mhp;

	public int air = 3;

	public Faction faction = Faction.None;
	public Direction look = Direction.DOWN;

	public String name;

	public boolean dead = false;

	@JsonIgnore
	protected Entity owner;

	private final static Stats maxStats = new Stats(new Npc());
	static {
		Stat.rollableStats.forEach(stat -> maxStats.baseStats.set(stat, BaseStats.MAX_LVL));
	}

	public Stats(Entity owner) {
		baseStats = new BaseStats().prepareLivingCreature();
		setOwner(owner);
	}

	public Stats() {
		this(null);
	}

	@JsonIgnore
	public void setOwner(Entity owner) {
		this.owner = owner;
	}

	public int get(Stat stat) {
		return baseStats.get(stat);
	}

	public void set(Stat stat, int value) {
		baseStats.set(stat, value);
	}

	public boolean rollAnd(Stat stat, int times) {
		return baseStats.rollAnd(stat, times);
	}
	
	public boolean rollOr(Stat stat, int times) {
		return baseStats.rollOr(stat, times);
	}

	public boolean roll(Stat stat) {
		return baseStats.roll(stat);
	}

	public void randomize(int lvl, int minStatVal) {
		baseStats.randomize(lvl, minStatVal);
		calcStats();
		restore();
	}

	public void randomize(int lvl) {
		randomize(lvl, 1);
	}

	public void calcStats() {
		owner.calcMovementSpeed();
		mhp = get(Stat.Constitution) * HP_MULTIPLIER;

		if (hp == 0)
			restore();

		calcSpeed();
		hp = Math.min(hp, mhp);
		owner.inventory.setMaxWeight(calcMaxInventoryWeight());
	}

	public void restore() {
		hp = mhp;
	}

	public boolean healthFull() {
		return hp == mhp;
	}

	public double calcSpeed() {
		int dexterity = get(Stat.Dexterity);
		if (dexterity < 2)
			actionPtsMax = 1;
		else
			actionPtsMax = (Utils.log(Math.pow(dexterity + 0.1, 1.7), 5)
				/ (3 + ((2 * Math.sqrt(dexterity)) / dexterity)))
				* 9.9;
		actionPtsMax -= actionPtsMax * getEncumbranceCoef();
		return (actionPts = actionPtsMax);
	}

	@JsonIgnore
	public int getSum() {
		return baseStats.getSum();
	}

	public void check() {
		if (hp > mhp)
			hp = mhp;

		if (hp <= 0) {
			hp = 0;
			owner.die();
		}
	}

	public boolean luckRoll() {
		return roll(Stat.Luck);
	}

	public boolean critRoll() {
		return roll(Stat.Accuracy) && roll(Stat.Accuracy);
	}

	public double calcAttackMissChance() {
		return (1.0 - baseStats.getEffectiveness(Stat.Accuracy)) * 0.5;
	}

	public double calcRangedAttackMissChance(int distance) {
		return calcAttackMissChance() + Math.exp(distance * 0.55) * (1d / Math.sqrt(get(Stat.Accuracy)));
	}

	public boolean rangedAttackMissed(int distance) {
		return Utils.percentRoll(calcRangedAttackMissChance(distance));
	}

	public boolean meleeAttackMissed() {
		return Utils.percentRoll(calcAttackMissChance());
	}

	public int getMinDamage(Stat attackStat) {
		int atk = getBaseAttack(attackStat);
		atk *= baseStats.getEffectiveness(Stat.Accuracy);
		return Math.max(1, atk);
	}
	
	public int getMaxDamage(Stat attackStat) {
		int atk = getBaseAttack(attackStat);
		atk *= 1.0 + baseStats.getEffectiveness(Stat.Accuracy);
		return atk;
	}
	
	public int getBaseAttack(Stat attackStat) {
		return get(attackStat) * 3;
	}

	/* Impact of 100% encumbrance on Entity's speed */
	private final static float ENCUMBRANCE_IMPACT = 0.35f;

	public float getEncumbranceCoef() {
		return getEncumbrancePercent() * ENCUMBRANCE_IMPACT;
	}

	public float getEncumbrancePercent() {
		var inv = owner.inventory;
		float maxWeight = inv.getMaxWeight();
		if (maxWeight == 0)
			return 0;

		return inv.getTotalWeight() / maxWeight;
	}

	@JsonIgnore
	public int getDefense() {
		return get(Stat.Defense);
	}

	@JsonIgnore
	public int getHealthPercent() {
		return (int) (((float) hp / (float) mhp) * PERCENT);
	}

	public float calcMaxInventoryWeight() {
		return BASE_MAX_WEIGHT + (get(Stat.Strength) + get(Stat.Dexterity)) * WEIGHT_MULTIPLIER;
	}

	public static Stats max() {
		return maxStats;
	}
}
