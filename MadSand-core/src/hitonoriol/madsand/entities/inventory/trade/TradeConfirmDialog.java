package hitonoriol.madsand.entities.inventory.trade;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.enums.TradeAction;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.ItemProp;

public class TradeConfirmDialog extends Dialog {
	private static final float TITLE_YPADDING = 18;
	private static final float TITLE_XPADDING = 3;

	private static float BUTTON_WIDTH = 100;
	private static float BUTTON_HEIGHT = 35;
	private static float BUTTON_PADDING = 10;

	private static float WIDTH = 400;
	private static float DEFAULT_HEIGHT = 75;

	private static float BUTTONS_PAD_TOP = DEFAULT_HEIGHT / 3;
	private static float LABEL_PAD_BOTTOM = 15;

	private static float SLIDER_WIDTH = WIDTH;
	private static float SLIDER_HEIGHT = DEFAULT_HEIGHT / 2;

	private static int SLIDER_STEP = 1;

	private static String chooseTextA = "Choose what amount of ";
	private static String chooseTextB = " to ";

	private static String cancelText = "Cancel";
	private static String confirmText = "Confirm";

	private static String titleBuyText = "Buy ";
	private static String titleSellText = "Sell ";

	private static String currencyName = ItemProp.getItemName(Globals.getInt(Globals.CURRENCY));

	private Label titleLabel; // Dialog title
	private Label chooseLabel; // Label above the slider
	private Label costLabel; // Label with item quantity and total cost (under the slider)
	private String costLabelText;
	private static String costFor = " for ";

	private Slider quantitySlider; // Item quantity slider

	private Table buttonTable;
	private TextButton cancelButton;
	private TextButton confirmButton;

	private static final int minQuantity = 1; // Min/max slider values
	private int maxQuantity;
	private int itemPrice;
	private int currentQuantity;

	private TradeInventory tradeInventory;
	private TradeAction tradeAction;
	private Item item;
	private TradeUIRefresher refresher;

	public TradeConfirmDialog(String title, Skin skin) { // Init all needed widgets
		super(title, skin);
		super.row();
		super.setWidth(WIDTH);
		Table titleTbl = super.getTitleTable();
		titleLabel = super.getTitleLabel();
		titleLabel.setAlignment(Align.center);

		titleTbl.padTop(TITLE_YPADDING).padLeft(TITLE_XPADDING);

		cancelButton = new TextButton(cancelText, Gui.skin);
		confirmButton = new TextButton(confirmText, Gui.skin);
		buttonTable = new Table();
		buttonTable.add(confirmButton).size(BUTTON_WIDTH, BUTTON_HEIGHT).pad(BUTTON_PADDING);
		buttonTable.add(cancelButton).size(BUTTON_WIDTH, BUTTON_HEIGHT).pad(BUTTON_PADDING);

		chooseLabel = new Label("", Gui.skin);
		costLabel = new Label("", Gui.skin);

	}

	public TradeConfirmDialog(TradeInventory trade, Item item, TradeAction tradeAction, TradeUIRefresher refresher) {
		this("", Gui.skin);
		maxQuantity = item.quantity;
		this.tradeInventory = trade;
		this.tradeAction = tradeAction;
		this.item = item;
		this.itemPrice = item.getPrice();
		this.refresher = refresher;
		this.currentQuantity = minQuantity;
		setCostLabelText(currentQuantity);

		chooseLabel.setText(getTradeText());
		chooseLabel.setWrap(true);
		chooseLabel.setAlignment(Align.bottomLeft);
		quantitySlider = new Slider(minQuantity, maxQuantity, SLIDER_STEP, false, Gui.skin);
		quantitySlider.setHeight(SLIDER_HEIGHT);

		super.add(chooseLabel).size(WIDTH, DEFAULT_HEIGHT).align(Align.left).padBottom(LABEL_PAD_BOTTOM).row();
		super.add(quantitySlider).size(SLIDER_WIDTH, DEFAULT_HEIGHT).align(Align.top).row();
		super.add(costLabel).align(Align.center).row();
		super.add(buttonTable).width(WIDTH).align(Align.center).padTop(BUTTONS_PAD_TOP).row();

		setTitle();
		setSliderListener();
		setConfirmButtonListener();
		setCancelButtonListener();
	}

	private String getTradeText() {
		return chooseTextA + item.name + chooseTextB + tradeAction.toString() + Resources.Colon;
	}

	private static String tradeError = " can't afford this quantity of ";

	private String getTradeErrorText() {
		return (tradeAction.equals(TradeAction.Buy) ? "You" : "Trader") + tradeError + item.name;
	}

	private void setSliderListener() {
		quantitySlider.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				currentQuantity = (int) quantitySlider.getValue();
				setCostLabelText(currentQuantity);
			}
		});
	}

	private void setConfirmButtonListener() {
		confirmButton.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (!tradeInventory.sell(item, currentQuantity)) {
					Gui.drawOkDialog(getTradeErrorText(), Gui.overlay);
					return;
				}

				refresher.refreshUI();
				remove();
			}

		});
	}

	private void setCancelButtonListener() {
		cancelButton.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				remove();
			}

		});
	}

	private void setCostLabelText(int itemQuantity) {
		costLabelText = itemQuantity + Resources.Space + item.name
				+ costFor + (itemPrice * itemQuantity) + Resources.Space + currencyName;
		costLabel.setText(costLabelText);
	}

	private void setTitle() {
		if (tradeAction.equals(TradeAction.Buy))
			titleLabel.setText(titleBuyText + item.name);
		else
			titleLabel.setText(titleSellText + item.name);
	}

	public void show() {
		super.show(Gui.overlay);
	}

}
