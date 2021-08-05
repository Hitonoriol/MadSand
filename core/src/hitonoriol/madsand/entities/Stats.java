package hitonoriol.madsand.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

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

	public StatContainer baseStats;

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

	public Stats(Entity owner) {
		baseStats = new StatContainer();
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

	public void roll(int lvl) {
		baseStats.roll(lvl);
		calcStats();
		restore();
	}

	public void roll() {
		roll(0);
	}

	public void calcStats() {
		owner.calcMovementSpeed();
		mhp = get(Stat.Constitution) * HP_MULTIPLIER;

		if (hp == 0)
			restore();

		calcActionCosts();
		hp = Math.min(hp, mhp);
	}

	public void restore() {
		hp = mhp;
	}
	
	public boolean healthFull() {
		return hp == mhp;
	}

	public void calcActionCosts() {
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
		return baseStats.getSum();
	}

	public void check() {
		if (hp > mhp)
			hp = mhp;

		if (hp <= 0) {
			hp = 0;
			dead = true;
			owner.die();
		}
	}

	public boolean luckRoll() {
		return Utils.percentRoll(Utils.log(Math.pow(get(Stat.Luck), 1.75), 8) + 9.75);
	}

	public boolean critRoll() {
		return Utils.percentRoll(Math.log(Math.pow(get(Stat.Accuracy) + 0.1, 7)));
	}

	public double calcAttackMissChance() {
		return (1 / Math.log(get(Stat.Accuracy) + 1) * 45.0);
	}

	public double calcRangedAttackMissChance(int distance) {
		return calcAttackMissChance() + Math.exp((double) distance * 0.55) * (1d / Math.sqrt(get(Stat.Accuracy)));
	}

	public boolean rangedAttackMissed(int distance) {
		return Utils.percentRoll(calcRangedAttackMissChance(distance));
	}

	public boolean meleeAttackMissed() {
		return Utils.percentRoll(calcAttackMissChance());
	}

	float defPercent = 0.333f;
	float minAttackPercent = 0.3f;
	float critPercent = 0.45f;

	public int getBaseAttack() {
		return get(Stat.Strength);
	}

	float rangedAtkCoef = 0.45f;

	public int calcBaseRangedAttack(int distance) {
		if (rangedAttackMissed(distance))
			return 0;

		return (int) Math.max(getBaseAttack() * minAttackPercent * rangedAtkCoef, calcAttack() * rangedAtkCoef);
	}

	public int calcMeleeAttack(int defense) {
		if (meleeAttackMissed())
			return 0;

		return calcAttack(defense);
	}

	private int calcAttack(int defense) {
		int atk = (int) (getBaseAttack() - (defense * defPercent));

		if (atk <= 0)
			atk = 1;

		if (critRoll())
			atk += atk * critPercent;
		else
			atk = Utils.rand((int) (atk * minAttackPercent), atk);

		return atk;
	}

	private int calcAttack() {
		return calcAttack(0);
	}

	@JsonIgnore
	public int getDefense() {
		return get(Stat.Defense);
	}

	public float calcMaxInventoryWeight() {
		return BASE_MAX_WEIGHT + (get(Stat.Strength) + get(Stat.Dexterity)) * WEIGHT_MULTIPLIER;
	}
}
