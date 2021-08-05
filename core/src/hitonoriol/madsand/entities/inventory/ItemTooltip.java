package hitonoriol.madsand.entities.inventory;

import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.gui.widgets.AutoSizeTooltip;

public class ItemTooltip extends AutoSizeTooltip {
	public ItemTooltip(Item item) {
		super(() -> item.getInfoString());
	}
}
