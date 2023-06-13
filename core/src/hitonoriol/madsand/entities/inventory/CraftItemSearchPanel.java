package hitonoriol.madsand.entities.inventory;

import java.util.List;
import java.util.stream.Stream;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.gui.Widgets;

public class CraftItemSearchPanel extends ItemSearchPanel {
	private CheckBox craftableBox = Widgets.plainCheckbox();

	public CraftItemSearchPanel(Actor parent) {
		super(parent, false);
		addEntry(craftableBox, "Craftable only").align(Align.left);
	}

	@Override
	public Stream<Item> search(List<Item> list) {
		var items = super.search(list);
		if (!craftableBox.isChecked())
			return items;

		var crafting = new CraftWorker(MadSand.player());
		return items.filter(item -> {
			crafting.setItemToCraft(item);
			return crafting.canBeCrafted();
		});
	}
}
