package hitonoriol.madsand.entities.inventory.trade;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.entities.inventory.Inventory;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.enums.GameState;
import hitonoriol.madsand.enums.TradeAction;
import hitonoriol.madsand.gui.widgets.AutoFocusScrollPane;
import hitonoriol.madsand.properties.ItemProp;

public class TradeInventoryUI {
	TradeUIRefresher refresher;

	private AutoFocusScrollPane sellPane, buyPane;
	private Table sellTable, buyTable;
	private Table tradeUITable;
	private Table containerTable;
	private Label header;
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
		header = new Label("Trade", Gui.skin);

		containerTable = new Table();
		tradeUITable = new Table();

		//tradeUITable.setDebug(true, true);

		tradeUITable.setSize(WIDTH, HEIGHT);
		sellTable = new Table();
		buyTable = new Table();
		sellTable.setHeight(HEIGHT);
		buyTable.setHeight(HEIGHT);

		exitBtn = new TextButton("Done", Gui.skin);

		containerTable.setBackground(Gui.darkBackground);
		containerTable.align(Align.topLeft);

		sellPane = new AutoFocusScrollPane(sellTable);
		buyPane = new AutoFocusScrollPane(buyTable);
		setPaneParams(sellPane);
		setPaneParams(buyPane);

		header.setFontScale(1.5f);

		containerTable.add(header).align(Align.center).row();

		tradeUITable.add(sellHeader).pad(10).width(WIDTH / 2 - OFFSET).align(Align.center);
		tradeUITable.add(buyHeader).pad(10).width(WIDTH / 2 - OFFSET).align(Align.center);
		tradeUITable.row();
		tradeUITable.add(sellPane).prefHeight(HEIGHT).align(Align.topLeft);
		tradeUITable.add(buyPane).prefHeight(HEIGHT).align(Align.topRight);
		tradeUITable.row();

		containerTable.setSize(WIDTH, HEIGHT);
		containerTable.setPosition(Gdx.graphics.getWidth() / 2 - WIDTH / 2, Gdx.graphics.getHeight() / 2 - HEIGHT / 2);

		containerTable.add(tradeUITable).row();
		containerTable.add(exitBtn).align(Align.bottom).padBottom(5).row();
		containerTable.setVisible(false);

		Gui.overlay.addActor(containerTable);

		refresher = new TradeUIRefresher() {
			@Override
			public void refreshUI() {
				refresh();
			}
		};

		exitBtn.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				remove();
				MadSand.reset();
			}
		});
	}

	private void setPaneParams(ScrollPane pane) {
		pane.setWidth(WIDTH / 2);
		pane.setHeight(HEIGHT);
		pane.setScrollingDisabled(true, false);
	}

	public void show() {
		Gui.overlay.hideActionBtn();
		refresh();
		containerTable.setVisible(true);
		Gui.gameUnfocus();
		MadSand.switchStage(GameState.TRADE, Gui.overlay);
	}

	public void refreshCurrencyHeader(Inventory inventory, TradeAction action) {
		Item currency = inventory.getItem(playerSell.currency);
		int quantity = 0;
		String text = action.name() + Resources.LINEBREAK;
		text += action == TradeAction.Sell ? "Your " : "Trader's ";
		text += ItemProp.getItemName(playerSell.currency) + "s: ";
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

		for (Item item : inventory.items) {
			if (item.id != tradeInventory.currency) {
				table.add(new TradeInventoryButton(tradeInventory, item, tradeAction, refresher)).align(align).row();
				table.add().row();
			}
		}
	}

	public void remove() {
		Gui.gameResumeFocus();
		containerTable.remove();
	}

	public void toggleVisible() {
		containerTable.setVisible(!containerTable.isVisible());
	}
}
