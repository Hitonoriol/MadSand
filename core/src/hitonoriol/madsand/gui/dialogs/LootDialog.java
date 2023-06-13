package hitonoriol.madsand.gui.dialogs;

import java.util.function.Consumer;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.inventory.ItemUI;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;
import hitonoriol.madsand.map.Loot;

public class LootDialog extends GameDialog {
	private static int ITEMS_PER_ROW = 5;
	private static int TABLE_PADDING = 25;

	Player player = MadSand.player();
	Table lootTable = Widgets.table();
	Loot loot;

	TextButton pickUpAllBtn = Widgets.button("Pick Up All");
	TextButton mergeBtn = Widgets.button("Merge All Stacks");

	public LootDialog(Loot loot) {
		super(Gui.overlay);
		this.loot = loot;
		super.setTitle("Items at (" + player.x + ", " + player.y + ")");
		super.skipLine();
		super.add(new AutoFocusScrollPane(lootTable))
			.size((ItemUI.SIZE * ITEMS_PER_ROW) + PADDING, 2.1f * ItemUI.SIZE)
			.padTop(TABLE_PADDING).row();
		super.skipLine();

		var buttonTable = Widgets.table();
		buttonTable.defaults().size(Gui.BTN_WIDTH, Gui.BTN_HEIGHT).pad(5);
		buttonTable.add(pickUpAllBtn);
		buttonTable.add(mergeBtn).row();
		buttonTable.add(super.createCloseButton()).colspan(2);
		super.add(buttonTable).padTop(PADDING);

		lootTable.align(Align.top);
		refreshLootTable();

		Gui.setAction(pickUpAllBtn, () -> {
			player.pickUpLoot(loot);
			remove();
		});

		Gui.setAction(mergeBtn, () -> {
			loot.mergeItemStacks();
			refreshLootTable();
		});
	}

	@SuppressWarnings("unchecked")
	private void refreshLootTable() {
		if (loot.isEmpty()) {
			remove();
			return;
		}

		lootTable.clear();
		ItemUI.createItemList(lootTable, loot.getContents(), ITEMS_PER_ROW);
		Actor actor;
		for (Cell<Actor> cell : lootTable.getCells()) {
			if (!((actor = cell.getActor()) instanceof ItemUI))
				continue;

			var item = ((ItemUI) actor).getItem();
			Gui.setClickAction(actor, () -> {
				Consumer<Integer> pickUpAction = quantity -> {
					player.pickUpLoot(loot, item, quantity);
					refreshLootTable();
				};

				if (item.quantity > 1)
					new SliderDialog(item.quantity)
						.setTitle("Pick Up " + item.name)
						.setSliderTitle("Quantity of " + item.name + " to pick up:")
						.setOnUpdateText(item.name)
						.setConfirmAction(pickUpAction)
						.show();
				else
					pickUpAction.accept(item.quantity);
			});
		}
	}
}
