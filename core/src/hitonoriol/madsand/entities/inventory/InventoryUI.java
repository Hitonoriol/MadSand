package hitonoriol.madsand.entities.inventory;

import static com.badlogic.gdx.Gdx.graphics;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.GuiSkin;
import hitonoriol.madsand.gui.Widgets;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;

public class InventoryUI extends GameDialog {
	private static final float ITEM_PAD = 7, PAD = 10;
	private float BODY_WIDTH = 0.6f * graphics.getWidth();
	private final float BODY_HEIGHT = 0.7f * graphics.getHeight();
	private final int ITEMS_PER_ROW = (int) ((BODY_WIDTH - ITEM_PAD * 2 * (BODY_WIDTH / InventoryUICell.SIZE))
			/ InventoryUICell.SIZE);

	private static final float BUTTON_PADDING = 5;
	private static final float BUTTON_WIDTH = 250;
	private static final float BUTTON_HEIGHT = 50;

	private Table invContainer = new Table();
	private Table itemTable = new Table();
	private AutoFocusScrollPane invScroll = new AutoFocusScrollPane(itemTable);
	private Label header = new Label("", Gui.skin);
	private ItemSearchPanel searchPanel = new ItemSearchPanel(this);

	private Inventory inventory;
	private int items;

	public InventoryUI(Inventory inventory) {
		super(Gui.overlay);
		this.inventory = inventory;
		BODY_WIDTH = ITEMS_PER_ROW * InventoryUICell.SIZE + ITEMS_PER_ROW * ITEM_PAD * 2;
		Gui.setFontSize(header, Gui.FONT_L);
		itemTable.setBackground(GuiSkin.darkBackground);
		invScroll.setOverscroll(false, false);
		invScroll.setScrollingDisabled(true, false);

		Table body = Widgets.table();
		Table buttonTable = Widgets.table();
		header.setAlignment(Align.center);
		body.add(header).colspan(2).width(BODY_WIDTH).padBottom(PAD).align(Align.right).row();
		body.add(searchPanel).padRight(PAD).align(Align.top);
		body.add(invScroll).size(BODY_WIDTH, BODY_HEIGHT);
		invContainer.add(body).align(Align.left).row();
		invContainer.add(buttonTable).colspan(2)
				.padTop(BUTTON_PADDING)
				.width(BODY_WIDTH)
				.align(Align.right)
				.row();
		invContainer.setFillParent(true);

		super.add(invContainer);
		super.setBackground(GuiSkin.transparency);

		TextButton craftMenuButton = new TextButton("Crafting", Gui.skin);
		buttonTable.defaults()
				.size(BUTTON_WIDTH, BUTTON_HEIGHT)
				.align(Align.center)
				.pad(BUTTON_PADDING);
		buttonTable.add(craftMenuButton).row();
		buttonTable.add(Gui.setAction(Widgets.button("Close [E]"), () -> hide()));

		Gui.setAction(craftMenuButton, () -> {
			addAction(Actions.fadeOut(FADE_DURATION));
			new CraftDialog(this).show();	
		});
		searchPanel.onChange(() -> refresh());
		refresh();		
	}

	void refresh() {
		items = 0;
		itemTable.clear();
		searchPanel.search(inventory.getItems())
				.forEach(item -> addItem(item));
		itemTable.align(items > ITEMS_PER_ROW ? Align.top : Align.topLeft);
		refreshTitle();
	}

	private void refreshTitle() {
		setHeader(inventory.getWeightString());
	}

	private void addCell(InventoryUICell cell) {
		cell.refreshEquippedStatus();
		++items;
		if (items % ITEMS_PER_ROW == 1)
			itemTable.row();
		itemTable.add(cell).pad(ITEM_PAD);
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
		if (obj instanceof InventoryUICell)
			((InventoryUICell) obj).closeContextMenu();
		setUserObject(cell);
	}

	void setHeader(String str) {
		header.setText("Total weight: " + str);
	}
}
