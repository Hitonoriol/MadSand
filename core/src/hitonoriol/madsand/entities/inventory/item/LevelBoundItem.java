package hitonoriol.madsand.entities.inventory.item;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.entities.Player;

public abstract class LevelBoundItem extends Item {
	public int lvl;

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

	protected void useIfPossible(Player player, Runnable useAction) {
		if (!canBeUsed(player))
			Gui.drawOkDialog(
					"Your level is too low! You need to be at least level " + lvl + " to equip this item.");
		else
			useAction.run();
	}

	@Override
	public String getFullName() {
		return super.getFullName() + " [ORANGE][[Lvl " + lvl + "][]";
	}
}
