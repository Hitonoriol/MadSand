package hitonoriol.madsand.entities.inventory;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;

public class InventoryUI extends GameDialog {
	int ITEMS_PER_ROW = 5;
	int WIDTH = 400;
	int HEIGHT = 500;
	int OFFSET = 5;

	float BUTTON_PADDING = 5;
	float BUTTON_WIDTH = 250;
	float BUTTON_HEIGHT = 50;

	Table invContainer;
	Table invTable;
	AutoFocusScrollPane invScroll;
	Table invScrollTable;
	Label header;
	TextButton craftMenuButton;

	private Inventory inventory;
	private Map<Item, InventoryUICell> itemUI = new HashMap<>();

	boolean dialogActive;
	int items;

	public InventoryUI(Inventory inventory) {
		super(Gui.overlay);
		this.inventory = inventory;
		invContainer = new Table();
		invScrollTable = new Table();
		invTable = new Table();
		craftMenuButton = new TextButton("Crafting", Gui.skin);
		invTable.row();

		header = new Label("", Gui.skin);
		Gui.setFontSize(header, Gui.FONT_L);

		invTable.setBackground(Gui.darkBackground);
		invTable.align(Align.topLeft);
		invTable.setWidth(WIDTH);
		invScrollTable.add(invTable);

		invScroll = new AutoFocusScrollPane(invScrollTable);
		invScroll.setHeight(HEIGHT);
		invScroll.setWidth(WIDTH + OFFSET);
		invScroll.setOverscroll(false, false);
		invScroll.setScrollingDisabled(true, false);

		invContainer.add(header).pad(10).fillY().align(Align.center).row();
		invContainer.add(invScroll).size(WIDTH, HEIGHT).row();
		invContainer.add(craftMenuButton).size(BUTTON_WIDTH, BUTTON_HEIGHT).align(Align.center).pad(BUTTON_PADDING)
				.row();

		super.add(invContainer)
				.width(WIDTH + OFFSET)
				.height(HEIGHT + header.getHeight() + BUTTON_HEIGHT + BUTTON_PADDING * 3);
		super.setBackground(Gui.transparency);

		items = 0;
		Gui.setAction(craftMenuButton, () -> Gui.openCraftMenu(0));
	}

	void refresh() {
		items = 0;
		invTable.clear();
		itemUI.entrySet().removeIf(uiEntry -> uiEntry.getKey().equals(Item.nullItem));
		itemUI.forEach((item, itemUICell) -> addCell(itemUICell));
	}

	void refreshTitle() {
		setHeader(inventory.getWeightString());
	}

	private void addCell(InventoryUICell cell) {
		cell.refreshEquippedStatus();
		++items;
		if (items % ITEMS_PER_ROW == 1)
			invTable.row();
		invTable.add(cell);
	}

	private void addItem(Item item) {
		InventoryUICell cell = new InventoryUICell(item);
		itemUI.put(item, cell);
		addCell(cell);
	}

	private void removeItem(Item item) {
		itemUI.remove(item);
		--items;
		refresh();
	}

	public void refreshItem(Item item) {
		if (itemExists(item)) {
			if (item.quantity > 0 && !item.equals(Item.nullItem))
				itemUI.get(item).refresh();
			else
				removeItem(item);
		} else if (!item.equals(Item.nullItem)) {
			addItem(item);
			refresh();
		}
		refreshTitle();
	}

	public InventoryUICell getUICell(Item item) {
		return itemUI.get(item);
	}

	public void clearContextMenus() {
		itemUI.forEach((item, uiCell) -> {
			if (uiCell.contextActive())
				uiCell.hideContext();
		});
	}

	/* Equip/Unequip for equippable & usable items */
	public void equipItem(Item item) {
		Player player = MadSand.player();
		InventoryUICell itemCell = getUICell(item);
		if (player.stats.equipment.itemEquipped(item)) {
			if (player.unEquip(item))
				itemCell.unEquipItem();
		} else {
			Item prev = player.stats.equipment.previouslyEquipped(item);
			if (player.unEquip(prev))
				getUICell(prev).unEquipItem();

			player.equip(item);
			refreshItem(prev);
			itemCell.equipItem();
		}
	}

	public void hide() {
		super.hide();
		clearContextMenus();
	}

	void setHeader(String str) {
		header.setText(str);
	}

	boolean itemExists(Item item) {
		return itemUI.containsKey(item);
	}
}
