package hitonoriol.madsand.entities.inventory.item;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.Stat;
import hitonoriol.madsand.entities.inventory.EquipStats;
import hitonoriol.madsand.entities.inventory.item.category.ItemCategories;
import hitonoriol.madsand.entities.inventory.item.category.ItemCategory;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.Utils;

public abstract class CombatEquipment extends AbstractEquipment {
	private static final int EQUIPMENT_HP_PER_LVL = 500;

	public EquipStats equipStats;

	public CombatEquipment(CombatEquipment protoItem) {
		super(protoItem);

		if (protoItem.isProto() && MadSand.player().stats().luckRoll())
			lvl += ItemCategories.clampTier(ItemCategories.rollTier());
	}

	public CombatEquipment() {}

	protected void initEquipStats(EquipStats protoEquipStats) {
		equipStats = protoEquipStats == null
				? new EquipStats(lvl, this)
				: new EquipStats(protoEquipStats);
	}

	protected CombatEquipment rollProperties() {
		if (MadSand.player().stats.luckRoll()) {
			++this.lvl;
			name += " of " + Utils.randWord();
		}

		equipStats = new EquipStats(lvl, this);
		hp = ((lvl == 0 ? 1 : lvl) * EQUIPMENT_HP_PER_LVL);
		maxHp = hp;
		return this;
	}

	@JsonIgnore
	public abstract Stat getMainStat();

	@Override
	public void equip(Player player) {
		player.stats.equipment.equip(this);
	}

	@Override
	public void use(Player player) {
		super.useIfPossible(player, () -> equip(player));
	}

	@Override
	protected String getMiscInfo() {
		return identified() ? equipStats.getString() : ("[RED]Unidentified[]" + Resources.LINEBREAK);
	}

	protected ItemCategory combatCategory() {
		return null;
	}

	@Override
	public void initCategory() {
		setCategory(combatCategory(), Math.max(cost / 50, lvl - 1));
	}
}
