package hitonoriol.madsand.entities;

import hitonoriol.madsand.entities.inventory.item.Projectile;
import hitonoriol.madsand.util.Utils;

public class Damage {
	private Entity dmgDealer;
	private int value;
	private boolean critical;
	private Type type = Type.Universal;

	private final static float MIN_ATK_FACTOR = 0.3f;
	private final static float RANGED_ATK_FACTOR = 0.75f;
	private final static float DEF_FACTOR = 0.33f;
	private final static float CRIT_FACTOR = 1.5f;

	public Damage(Entity dmgDealer) {
		this.dmgDealer = dmgDealer;
		calcBaseDamage();
	}

	public Damage(int value) {
		this.value = value;
	}

	private void calcBaseDamage() {
		Stats stats = dmgDealer.stats();
		value = (int) (stats.getBaseAttack());
		critical = stats.critRoll();

		if (critical)
			value *= CRIT_FACTOR;
		else
			value = Utils.rand((int) (value * MIN_ATK_FACTOR), value);
	}

	public Damage melee(int targetDef) {
		Stats stats = dmgDealer.stats();
		type = Type.Melee;
		if (stats.meleeAttackMissed())
			value = 0;
		else {
			value -= targetDef * DEF_FACTOR;
			if (value <= 0)
				value = 1;
		}
		return this;
	}

	public Damage ranged(Projectile proj, int distanceToTarget) {
		Stats stats = dmgDealer.stats();
		type = Type.Ranged;
		if (stats.rangedAttackMissed(distanceToTarget))
			value = 0;
		else
			value = proj.calcDamage()
					+ (int) Math.max(value * RANGED_ATK_FACTOR * MIN_ATK_FACTOR, value * RANGED_ATK_FACTOR);
		return this;
	}

	public boolean dealtBy(Entity entity) {
		return dmgDealer == entity;
	}

	public boolean missed() {
		return value == 0;
	}

	public String getValueString() {
		if (!critical)
			return Utils.str(value);
		else
			return "[RED]" + value + "[] [SALMON](critical)[]";
	}

	public Entity getDealer() {
		return dmgDealer;
	}

	public int getValue() {
		return value;
	}

	public boolean isCritical() {
		return critical;
	}

	public Type getType() {
		return type;
	}

	public static enum Type {
		Universal, Melee, Ranged
	}
}
