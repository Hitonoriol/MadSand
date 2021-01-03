package hitonoriol.madsand.entities.inventory.trade;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.gui.dialogs.SliderDialog;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.ItemProp;

public class TradeConfirmDialog extends SliderDialog {
	private static String chooseTextA = "Choose what amount of ";
	private static String chooseTextB = " to ";

	private static String titleBuyText = "Buy ";
	private static String titleSellText = "Sell ";

	private static String currencyName = ItemProp.getItemName(Globals.getInt(Globals.CURRENCY));

	private static String costFor = " for ";
	private String costLabelText;
	
	private int itemPrice;
	private TradeInventory tradeInventory;
	private TradeAction tradeAction;
	private Item item;
	private TradeUIRefresher refresher;

	public TradeConfirmDialog(TradeInventory trade, Item item, TradeAction tradeAction, TradeUIRefresher refresher) {
		super(item.quantity);
		this.tradeInventory = trade;
		this.tradeAction = tradeAction;
		this.item = item;
		this.itemPrice = item.getPrice();
		this.refresher = refresher;
		this.currentQuantity = minQuantity;
		setCostLabelText(currentQuantity);

		super.setSliderTitle(getTradeText());

		setTitle();
		setSliderListener();
		setConfirmButtonListener();
	}

	private String getTradeText() {
		return chooseTextA + item.name + chooseTextB + tradeAction.toString() + Resources.Colon;
	}

	private static String tradeError = " can't afford this quantity of ";

	private String getTradeErrorText() {
		return (tradeAction.equals(TradeAction.Buy) ? "You" : "Trader") + tradeError + item.name;
	}

	private void setSliderListener() {
		super.setSliderListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				currentQuantity = (int) slider.getValue();
				setCostLabelText(currentQuantity);
			}
		});
	}

	private void setConfirmButtonListener() {
		super.setConfirmListener(new ChangeListener() {

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

	private void setCostLabelText(int itemQuantity) {
		costLabelText = itemQuantity + Resources.Space + item.name
				+ costFor + (itemPrice * itemQuantity) + Resources.Space + currencyName;
		super.setSliderText(costLabelText);
	}

	private void setTitle() {
		if (tradeAction.equals(TradeAction.Buy))
			super.setTitle(titleBuyText + item.name);
		else
			super.setTitle(titleSellText + item.name);
	}

	public void show() {
		super.show(Gui.overlay);
	}

}
