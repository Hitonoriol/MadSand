package hitonoriol.madsand.gui.dialogs;

import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.inventory.ItemUI;
import hitonoriol.madsand.entities.inventory.item.Item;

public class GrabBagDialog extends GameDialog {

	int ITEMS_PER_ROW = 5;

	private GrabBagDialog(Stage stage) {
		super(stage);
	}

	public GrabBagDialog(String bagName, List<Item> items) {
		this(Gui.overlay);
		setTitle(bagName);
		super.skipLine();
		String itemString = (!items.isEmpty()) ? "Items you found inside:" : "Damn! The bag is empty.";
		super.add(itemString).align(Align.center).row();

		super.add(ItemUI.createItemList(items, ITEMS_PER_ROW)).row();
		super.skipLine();
		super.add(createCloseButton()).size(150, 35);
	}

}
