package hitonoriol.madsand.entities.inventory.trade;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.inventory.Inventory;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.gamecontent.Items;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.GuiSkin;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;
import hitonoriol.madsand.resources.Resources;

public class TradeInventoryUI extends GameDialog {
	TradeUIRefresher refresher;

	private AutoFocusScrollPane sellPane, buyPane;
	private Table sellTable, buyTable;
	private Table tradeUITable;
	private Label sellHeader, buyHeader;
	private TextButton exitBtn;

	Inventory playerInventory, traderInventory;
	TradeInventory playerSell, traderSell; //Same as w\ the panes

	private final float WIDTH = 800;
	private final float HEIGHT = 500;
	private final float OFFSET = 25;

	public TradeInventoryUI(Inventory traderInventory, Inventory playerInventory) {
		traderSell = new TradeInventory(traderInventory, playerInventory); //Buy from trader
		playerSell = new TradeInventory(playerInventory, traderInventory); //Sell to trader
		this.playerInventory = playerInventory;
		this.traderInventory = traderInventory;

		sellHeader = new Label("Sell", Gui.skin);
		buyHeader = new Label("Buy", Gui.skin);
		sellHeader.setAlignment(Align.center);
		buyHeader.setAlignment(Align.center);
		setTitle("Trade");
		centerTitle();

		tradeUITable = new Table();

		tradeUITable.setSize(WIDTH, HEIGHT);
		sellTable = new Table();
		buyTable = new Table();
		sellTable.setHeight(HEIGHT);
		buyTable.setHeight(HEIGHT);

		exitBtn = new TextButton("Done", Gui.skin);

		setBackground(GuiSkin.getColorDrawable(Color.DARK_GRAY));
		align(Align.topLeft);

		sellPane = new AutoFocusScrollPane(sellTable);
		buyPane = new AutoFocusScrollPane(buyTable);
		setPaneParams(sellPane);
		setPaneParams(buyPane);

		Gui.setFontSize(getTitleLabel(), Gui.FONT_M);

		tradeUITable.add(sellHeader).pad(10).width(WIDTH / 2 - OFFSET).align(Align.center);
		tradeUITable.add(buyHeader).pad(10).width(WIDTH / 2 - OFFSET).align(Align.center);
		tradeUITable.row();
		tradeUITable.add(sellPane).prefHeight(HEIGHT).align(Align.topLeft);
		tradeUITable.add(buyPane).prefHeight(HEIGHT).align(Align.topRight);
		tradeUITable.row();

		setSize(WIDTH, HEIGHT);

		add(tradeUITable).row();
		add(exitBtn).align(Align.bottom).padBottom(5).row();

		refresher = () -> refresh();
		Gui.setAction(exitBtn, () -> remove());
	}

	private void setPaneParams(ScrollPane pane) {
		pane.setWidth(WIDTH / 2);
		pane.setHeight(HEIGHT);
		pane.setScrollingDisabled(true, false);
	}

	public void show() {
		super.show();
		refresh();
	}

	public void refreshCurrencyHeader(Inventory inventory, TradeAction action) {
		Item currency = inventory.getItem(playerSell.currency);
		int quantity = 0;
		String text = action.name() + Resources.LINEBREAK;
		text += action == TradeAction.Sell ? "Your " : "Trader's ";
		text += Items.all().getName(playerSell.currency) + "s: ";
		if (!currency.equals(Item.nullItem))
			quantity = currency.quantity;
		text += quantity;

		if (action == TradeAction.Sell)
			sellHeader.setText(text);
		else
			buyHeader.setText(text);
	}

	public void refresh() {
		Inventory playerInventory = playerSell.getSeller();
		Inventory traderInventory = playerSell.getBuyer();

		sellTable.clear();
		buyTable.clear();

		refresh(sellTable, playerInventory, TradeAction.Sell, Align.topLeft);
		refresh(buyTable, traderInventory, TradeAction.Buy, Align.topRight);
		refreshCurrencyHeader(playerInventory, TradeAction.Sell);
		refreshCurrencyHeader(traderInventory, TradeAction.Buy);
	}

	private void refresh(Table table, Inventory inventory, TradeAction tradeAction, int align) {
		TradeInventory tradeInventory;
		if (tradeAction.equals(TradeAction.Sell))
			tradeInventory = playerSell;
		else
			tradeInventory = traderSell;

		for (Item item : inventory.getItems()) {
			if (item.id() != tradeInventory.currency && item.cost > 0) {
				table.add(new TradeInventoryButton(tradeInventory, item, tradeAction, refresher)).align(align).row();
				table.add().row();
			}
		}
	}
}
