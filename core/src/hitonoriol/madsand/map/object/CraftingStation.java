package hitonoriol.madsand.map.object;

import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.gui.Gui;

public class CraftingStation extends BuiltObject {
	public CraftingStation(CraftingStation protoObject) {
		super(protoObject);
	}

	@Override
	public CraftingStation copy() {
		return new CraftingStation(this);
	}

	public CraftingStation() {
	}

	@Override
	public void interact(Player player) {
		super.interact(player, () -> Gui.openCraftMenu(id));
	}

	@Override
	public String getBuildInfo() {
		return "Crafting station";
	}
}
