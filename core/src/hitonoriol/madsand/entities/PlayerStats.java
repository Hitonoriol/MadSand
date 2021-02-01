package hitonoriol.madsand.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.Utils;
import hitonoriol.madsand.entities.inventory.EquipStats;
import hitonoriol.madsand.entities.inventory.item.CombatEquipment;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.inventory.item.Projectile;
import hitonoriol.madsand.entities.inventory.item.Tool;
import hitonoriol.madsand.entities.inventory.item.Weapon;

public class PlayerStats extends Stats {
	static float BASE_SATIATION_PERCENT = 90;

	private int statBonus = 0; // Total equipment stat bonus

	public float STAMINA_BASE_COST = 0.1f;
	public float staminaLowPercent = 10;
	public float stamina = 0;
	public float maxstamina;

	public boolean hasRespawnPoint = false;
	public int respawnX = -1;
	public int respawnY = -1;
	public int respawnWX = -1, respawnWY = -1;

	public SkillContainer skills = new SkillContainer();

	@JsonIgnore
	public Equipment equipment;

	public final int maxFood = 1000;
	public int foodTicks = skills.getLvl(Skill.Survival);
	public int food = maxFood;

	public PlayerStats(Entity owner) {
		super(owner);
		equipment = new Equipment(this);
	}

	public PlayerStats() {
		this(null);
	}

	public void perTickCheck() {
		perTickFoodCheck();
		perTickStaminaCheck();

		if (getHpPercent() > MIN_HP_AUTODAMAGE_PERCENT) {
			if (getStaminaPercent() < staminaLowPercent && !skills.skillRoll(Skill.Survival))
				owner.damage(STAMINA_DMG);

			if (food <= 0)
				owner.damage(STARVE_DMG);
		}

		if (isSatiated() && skills.skillRoll(Skill.Survival))
			owner.heal(FOOD_HEAL);

		check();
	}

	public void check() {
		super.check();
		skills.check();

		if (food > maxFood)
			food = maxFood;

		if (food < 0)
			food = 0;

		int maxFoodTicks = getMaxFoodTicks();
		if (foodTicks > maxFoodTicks)
			foodTicks = maxFoodTicks;

		if (stamina > maxstamina)
			stamina = maxstamina;

		if (stamina < 0)
			stamina = 0;
	}

	public void calcStats() {
		super.calcStats();
		maxstamina = ((get(Stat.Dexterity) + get(Stat.Constitution)) / 2) * 5;

		if (maxstamina == 0)
			restore();
	}

	public void restore() {
		super.restore();
		stamina = maxstamina;
	}

	@JsonIgnore
	public boolean isSatiated() {
		double reqSatiationPercent = BASE_SATIATION_PERCENT - skills.getSkillRollPercent(Skill.Survival);
		return getSatiationPercent() >= reqSatiationPercent;
	}

	public double getStaminaRegenRate() {
		return skills.getSkillRollPercent(Skill.Survival) * 0.02;
	}

	private void perTickStaminaCheck() {
		if (isSatiated())
			stamina += getStaminaRegenRate();
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

	@JsonIgnore
	public int getSum() {
		return super.getSum() - statBonus;
	}

	public float calcStaminaCost() {
		return (float) (STAMINA_BASE_COST - (STAMINA_BASE_COST * skills.getSkillRollPercent(Skill.Survival) * 0.01));
	}

	float atkSkillPercent = 0.5f;
	float evasionSkillPercent = 0.3f;

	protected int getBaseAttack() {
		return (int) (super.getBaseAttack() + skills.getLvl(Skill.Melee) * atkSkillPercent);
	}

	@JsonIgnore
	public int getDefense() {
		return (int) (super.getDefense() + skills.getLvl(Skill.Evasion) * evasionSkillPercent);
	}

	@JsonIgnore
	public int getMaxFoodTicks() {
		float lvl = 0.5f + ((float) skills.getLvl(Skill.Survival) * 0.5f);
		return (int) (Math.sqrt(lvl) * 5);
	}

	public Item hand() {
		return equipment.getHand();
	}

	public Item offHand() {
		return equipment.getItem(EquipSlot.Offhand);
	}

	public Tool getEquippedTool() {
		return hand().as(Tool.class);
	}

	public Weapon getEquippedWeapon() {
		return hand().as(Weapon.class);
	}

	public Projectile getEquippedProjectile() {
		return offHand().as(Projectile.class);
	}

	public void applyBonus(CombatEquipment item) {
		if (item.isEmpty())
			return;

		EquipStats bonus = item.equipStats;
		baseStats.add(bonus.stats);
		statBonus += bonus.getTotalBonus();
		calcStats();
	}

	public void removeBonus(CombatEquipment item) {
		if (item.isEmpty())
			return;

		EquipStats bonus = item.equipStats;
		baseStats.sub(bonus.stats);
		statBonus -= bonus.getTotalBonus();
		calcStats();
	}

	static float BASE_ENCOUNTER_CHANCE = 9.75f;

	public boolean rollEncounter() {
		return Utils.percentRoll(BASE_ENCOUNTER_CHANCE);
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
}
