package hitonoriol.madsand.entities.inventory;

import java.util.function.Consumer;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.inventory.item.AbstractEquipment;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.gui.dialogs.ConfirmDialog;
import hitonoriol.madsand.gui.dialogs.SliderDialog;
import hitonoriol.madsand.input.Mouse;

public class InventoryUICell extends ItemUI {

	private static final float CONTEXT_BTN_WIDTH = 100F;
	private static final float CONTEXT_BTN_HEIGHT = 30F;
	public static final float CONTEXT_W_DENOMINATOR = 1.75f;

	private Table contextContainer; // RMB context menu container and buttons
	private Label equippedLabel;

	public InventoryUICell(Item item) {
		super(item);
		initContextMenu(item);

		refreshEquippedStatus();
		equippedLabel.setPosition(itemQuantityLabel.getX(), itemQuantityLabel.getY() + itemQuantityLabel.getHeight());
		super.addActor(equippedLabel);

		Gui.setClickAction(this, Buttons.LEFT, () -> {
			Player player = MadSand.player();
			item.leftClickAction();
			refreshEquippedStatus();
			player.doAction(player.stats.minorCost);
		});

		Gui.setClickAction(this, Buttons.RIGHT, () -> toggleContextMenu());
	}

	private void initContextMenu(Item item) {
		contextContainer = new Table(Gui.skin);
		TextButton dropBtn = new TextButton("Drop", Gui.skin);
		addContextBtn(dropBtn);
		contextContainer.setVisible(false);
		super.addActor(contextContainer);

		Gui.setAction(dropBtn, () -> {
			hideContext();

			if (MadSand.player().stats().equipment.itemEquipped(item)
					&& AbstractEquipment.isCursed(item)) {
				Gui.drawOkDialog("Cursed item", item.name + " is cursed and can't be dropped!");
				return;
			}

			Consumer<Integer> dropAction = quantity -> MadSand.player().dropItem(item, quantity);
			if (item.quantity > 1)
				new SliderDialog(item.quantity).setTitle("Drop " + item.name)
						.setSliderTitle("Quantity of " + item.name + " to drop").setOnUpdateText(item.name)
						.setConfirmAction(dropAction).show();
			else
				new ConfirmDialog("Drop " + item.name + "?", () -> dropAction.accept(item.quantity), Gui.overlay)
						.show();
		});
	}

	private void toggleContextMenu() {
		if (!contextContainer.isVisible()) {
			toFront();
			MadSand.player().inventory.getUI().clearContextMenus();
			contextContainer.setVisible(true);
			Mouse.updScreenCoords();
			contextContainer.setPosition(SIZE / 2, SIZE / 2);
		} else
			contextContainer.setVisible(false);
	}

	public void equipItem() {
		equippedLabel.setVisible(true);
	}

	public void unEquipItem() {
		equippedLabel.setVisible(false);
	}

	private void closeContextMenu() {
		contextContainer.setVisible(false);
	}

	private void addContextBtn(TextButton btn) {
		contextContainer.add(btn).width(CONTEXT_BTN_WIDTH).height(CONTEXT_BTN_HEIGHT).row();
	}

	boolean contextActive() {
		return contextContainer.isVisible();
	}

	void hideContext() {
		closeContextMenu();
	}

	public void refreshEquippedStatus() {
		if (equippedLabel == null)
			equippedLabel = new Label("[#387aff]E []", Gui.skin);

		equippedLabel.setVisible(MadSand.player().stats.equipment.itemEquipped(item));
	}

	@Override
	public void refresh() {
		super.refresh();
		refreshEquippedStatus();
	}
}
