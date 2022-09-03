package hitonoriol.madsand.entities.inventory;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.MadSand.Screens;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.GuiSkin;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;

public class InventoryUI extends GameDialog {
	private static final int ITEMS_PER_ROW = 5;
	private static final int WIDTH = 400;
	private static final int HEIGHT = 500;
	private static final int OFFSET = 5;

	private static final float BUTTON_PADDING = 5;
	private static final float BUTTON_WIDTH = 250;
	private static final float BUTTON_HEIGHT = 50;

	private Table invContainer;
	private Table invTable;
	private AutoFocusScrollPane invScroll;
	private Table invScrollTable;
	private Label header;
	private TextButton craftMenuButton;

	private Inventory inventory;
	private int items;

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

		invTable.setBackground(GuiSkin.darkBackground);
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
		invContainer.add(craftMenuButton)
				.size(BUTTON_WIDTH, BUTTON_HEIGHT)
				.align(Align.center)
				.pad(BUTTON_PADDING)
				.row();
		invContainer.add(Gui.setAction(Widgets.button("Close [E]"), () -> hide()))
				.size(BUTTON_WIDTH, BUTTON_HEIGHT).row();
		invContainer.setFillParent(true);

		super.add(invContainer);
		super.setBackground(GuiSkin.transparency);

		Gui.setAction(craftMenuButton, () -> {
			Gui.openCraftMenu(0);
			Screens.Game.screen().onShow(() -> refresh());
		});
		refresh();
	}

	private void refresh() {
		items = 0;
		invTable.clear();
		inventory.getItems().stream()
				.forEach(item -> addItem(item));
		refreshTitle();
	}

	private void refreshTitle() {
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
		addCell(new InventoryUICell(this, item));
	}

	void refreshCell(InventoryUICell cell) {
		Item item = cell.getItem();
		if (item.quantity > 0 && !item.equals(Item.nullItem))
			cell.refresh();
		else
			refresh();
		refreshTitle();
	}

	void openingContextMenu(InventoryUICell cell) {
		Object obj = getUserObject();
		if (!(obj instanceof InventoryUICell))
			setUserObject(cell);
		else {
			((InventoryUICell) obj).closeContextMenu();
			setUserObject(null);
		}
	}

	void setHeader(String str) {
		header.setText(str);
	}
}
