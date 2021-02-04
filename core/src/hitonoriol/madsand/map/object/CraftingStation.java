package hitonoriol.madsand.map.object;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.entities.Player;

public class CraftingStation extends BuiltObject {
	public CraftingStation(CraftingStation protoObject) {
		super(protoObject);
	}

	@Override
	public CraftingStation copy() {
		return new CraftingStation(this);
	}

	public CraftingStation() {
		super();
	}

	@Override
	public void interact(Player player) {
		super.interact(player, () -> Gui.openCraftMenu(id));
	}
}
