package hitonoriol.madsand.entities.inventory.item;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.Stat;
import hitonoriol.madsand.entities.inventory.EquipStats;
import hitonoriol.madsand.util.Utils;
import hitonoriol.madsand.world.World;

public abstract class CombatEquipment extends AbstractEquipment {
	private static final int EQUIPMENT_HP_PER_LVL = 500;

	public EquipStats equipStats;

	public CombatEquipment(CombatEquipment protoItem) {
		super(protoItem);
	}

	public CombatEquipment() {
		super();
	}
	
	protected void initEquipStats(EquipStats protoEquipStats) {
		if (protoEquipStats == null)
			equipStats = new EquipStats(lvl, this);
		else
			equipStats = new EquipStats(protoEquipStats);
	}

	protected CombatEquipment rollProperties() {
		if (World.player.stats.luckRoll()) {
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
		return equipStats.getString();
	}
}
