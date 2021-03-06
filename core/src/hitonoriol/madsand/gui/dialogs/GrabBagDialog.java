package hitonoriol.madsand.gui.dialogs;

import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
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

		TextButton closeButton = new TextButton("Close", Gui.skin);

		super.add(ItemUI.createItemList(items, ITEMS_PER_ROW)).row();
		super.skipLine();
		super.add(closeButton).size(150, 35);

		closeButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				remove();
			}
		});
	}

}
