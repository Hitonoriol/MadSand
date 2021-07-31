package hitonoriol.madsand.entities;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.entities.equipment.EquipSlot;
import hitonoriol.madsand.entities.equipment.Equipment;
import hitonoriol.madsand.entities.inventory.EquipStats;
import hitonoriol.madsand.entities.inventory.item.AbstractEquipment;
import hitonoriol.madsand.entities.inventory.item.CombatEquipment;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.inventory.item.Projectile;
import hitonoriol.madsand.entities.inventory.item.Tool;
import hitonoriol.madsand.entities.inventory.item.Weapon;
import hitonoriol.madsand.entities.skill.Skill;
import hitonoriol.madsand.entities.skill.SkillContainer;
import hitonoriol.madsand.util.Functional;
import hitonoriol.madsand.util.Utils;

public class PlayerStats extends Stats {
	static float BASE_SATIATION_PERCENT = 85;

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
			if (getStaminaPercent() < staminaLowPercent && !skills.skillRoll(Skill.Survival)) {
				owner.damage(STAMINA_DMG);
				MadSand.warn("You are exhausted");
			}

			if (food <= 0) {
				owner.damage(STARVE_DMG);
				MadSand.warn("You are starving");
			}
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

	public int getBaseAttack() {
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
		return equipment.getItem(EquipSlot.MainHand);
	}

	public Item offHand() {
		return equipment.getItem(EquipSlot.Offhand);
	}

	public boolean isToolEquipped(Tool.Type type) {
		return Functional.test(getEquippedTool(), tool -> tool.type == type);
	}

	public int getEquippedToolDamage(Skill skill) {
		return getEquippedTool()
				.map(tool -> tool.getSkillDamage(skill))
				.orElse(Tool.MIN_SKILL_DMG);
	}

	public Tool.Type getEquippedToolType() {
		return getEquippedTool()
				.map(tool -> tool.type)
				.orElse(Tool.Type.None);
	}

	public Optional<AbstractEquipment> getHeldEquipment() {
		return hand().as(AbstractEquipment.class);
	}

	public Optional<Tool> getEquippedTool() {
		return hand().as(Tool.class);
	}

	public Optional<Weapon> getEquippedWeapon() {
		return hand().as(Weapon.class);
	}

	public Optional<Projectile> getEquippedProjectile() {
		return offHand().as(Projectile.class);
	}

	public void applyBonus(CombatEquipment item) {
		EquipStats bonus = item.equipStats;
		baseStats.add(bonus.stats);
		statBonus += bonus.getTotalBonus();
		calcStats();
	}

	public void removeBonus(CombatEquipment item) {
		EquipStats bonus = item.equipStats;
		baseStats.sub(bonus.stats);
		statBonus -= bonus.getTotalBonus();
		calcStats();
	}

	private final static float BASE_ENCOUNTER_CHANCE = 7.5f;

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
