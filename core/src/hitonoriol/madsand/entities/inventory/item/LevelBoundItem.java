package hitonoriol.madsand.entities.inventory.item;

import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.gui.Gui;

public abstract class LevelBoundItem extends Item {
	public int lvl = 1;

	public LevelBoundItem(LevelBoundItem protoItem) {
		super(protoItem);
		lvl = protoItem.lvl;
	}

	public LevelBoundItem() {
		super();
	}

	public boolean canBeUsed(Player player) {
		int playerLvl = player.getLvl();
		int minLvl = playerLvl == 0 ? lvl - 1 : lvl;
		return playerLvl >= minLvl;
	}

	protected void doIfPossible(Player player, Runnable action) {
		if (!canBeUsed(player))
			Gui.drawOkDialog(
					"Your level is too low! You need to be at least level [WARNING]" + lvl + "[] to use this item.");
		else {
			action.run();
		}
	}
	
	@Override
	public void equip(Player player) {
		doIfPossible(player, () -> super.equip(player));
	}

	@Override
	public String getFullName() {
		return super.getFullName() + " [ORANGE][[Lvl " + lvl + "][]";
	}
}
