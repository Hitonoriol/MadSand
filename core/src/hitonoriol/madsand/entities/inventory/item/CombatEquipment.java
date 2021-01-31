package hitonoriol.madsand.entities.inventory.item;

import hitonoriol.madsand.Utils;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.inventory.EquipStats;
import hitonoriol.madsand.world.World;

public abstract class CombatEquipment extends AbstractEquipment {
	private static final int EQUIPMENT_HP_PER_LVL = 500;

	public EquipStats equipStats;

	public CombatEquipment(CombatEquipment protoItem) {
		super(protoItem);
		this.lvl = protoItem.lvl;

		if (World.player.stats.luckRoll()) {
			++this.lvl;
			name += " of " + Utils.randWord();
		}

		equipStats = new EquipStats(lvl, type);
		hp = ((lvl == 0 ? 1 : lvl) * EQUIPMENT_HP_PER_LVL);
		maxHp = hp;
	}
	
	public CombatEquipment() {
		super();
	}
	
	@Override
	public void use(Player player) {
		super.useIfPossible(player, () -> player.stats.equipment.equip(this));
	}

	@Override
	public String getMiscInfo() {
		return equipStats.getString();
	}
}
