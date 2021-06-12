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
import hitonoriol.madsand.util.Functional;

public class InventoryUICell extends ItemUI {

	private static final float CONTEXT_BTN_WIDTH = 100F;
	private static final float CONTEXT_BTN_HEIGHT = 30F;
	public static final float CONTEXT_W_DENOMINATOR = 1.75f;

	private Table invCellContextContainer; // RMB context menu container and buttons

	private TextButton dropBtn;

	static String equippedText = "[#387aff]E []";
	private Label equippedLabel = new Label(equippedText, Gui.skin);
	Player player = MadSand.player();

	public InventoryUICell(Item item) {
		super(item);
		initContextMenu(item);

		refreshEquippedStatus();
		equippedLabel.setPosition(itemQuantityLabel.getX(), itemQuantityLabel.getY() + itemQuantityLabel.getHeight());
		super.addActor(equippedLabel);

		Gui.setClickAction(this, Buttons.LEFT, () -> {
			item.leftClickAction();
			refreshEquippedStatus();
			player.doAction(player.stats.minorCost);
		});

		Gui.setClickAction(this, Buttons.RIGHT, () -> toggleContextMenu());
	}

	private void initContextMenu(Item item) {
		invCellContextContainer = new Table(Gui.skin);
		dropBtn = new TextButton("Drop", Gui.skin);
		addContextBtn(dropBtn);
		invCellContextContainer.setVisible(false);
		super.addActor(invCellContextContainer);

		Gui.setAction(dropBtn, () -> {
			hideContext();

			if (Functional.test(item.as(AbstractEquipment.class), eq -> eq.cantBeDropped())) {
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
		if (!invCellContextContainer.isVisible()) {
			toFront();
			player.inventory.getUI().clearContextMenus();
			invCellContextContainer.setVisible(true);
			Mouse.updScreenCoords();
			invCellContextContainer.setPosition(SIZE / 2, SIZE / 2);

		} else
			invCellContextContainer.setVisible(false);
	}

	public void equipItem() {
		equippedLabel.setVisible(true);
	}

	public void unEquipItem() {
		equippedLabel.setVisible(false);
	}

	private void closeContextMenu() {
		invCellContextContainer.setVisible(false);
	}

	private void addContextBtn(TextButton btn) {
		invCellContextContainer.add(btn).width(CONTEXT_BTN_WIDTH).height(CONTEXT_BTN_HEIGHT).row();
	}

	boolean contextActive() {
		return invCellContextContainer.isVisible();
	}

	void hideContext() {
		closeContextMenu();
	}

	public void refreshEquippedStatus() {
		equippedLabel.setVisible(player.stats.equipment.itemEquipped(item) || player.stats.hand() == item
				|| player.stats.offHand() == item);
	}
}
