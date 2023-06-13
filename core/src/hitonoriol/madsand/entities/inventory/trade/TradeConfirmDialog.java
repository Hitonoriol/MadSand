package hitonoriol.madsand.entities.inventory.trade;

import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.gamecontent.Globals;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.gui.dialogs.SliderDialog;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.Utils;

public class TradeConfirmDialog extends SliderDialog {
	private static String chooseTextA = "Choose what amount of ";
	private static String chooseTextB = " to ";

	private static String titleBuyText = "Buy ";
	private static String titleSellText = "Sell ";

	private static String currencyName = Globals.getCurrency().name;

	private static String costFor = " for ";
	private String costLabelText;

	private int itemPrice;
	private TradeInventory tradeInventory;
	private TradeAction tradeAction;
	private Item item;
	private TradeUIRefresher refresher;

	public TradeConfirmDialog(TradeInventory seller, Item item, TradeAction tradeAction, TradeUIRefresher refresher) {
		super(TradeInventory.maxAffordableQuantity(seller.buyer, item));
		tradeInventory = seller;
		this.tradeAction = tradeAction;
		this.item = item;
		itemPrice = item.getPrice();
		this.refresher = refresher;
		currentQuantity = minValue;
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
		return TradeInventory.getBuyerString(tradeAction) + tradeError + item.name;
	}

	private void setSliderListener() {
		super.setSliderAction(value -> setCostLabelText(currentQuantity = value));
	}

	private void setConfirmButtonListener() {
		super.setConfirmAction(quantity -> {
			if (!tradeInventory.sell(item, quantity)) {
				Gui.drawOkDialog(getTradeErrorText());
				return;
			}

			Utils.out(
				"%s sold %d %s for %d coins",
				TradeInventory.getBuyerString(tradeAction), quantity, item.name,
				item.getPrice() * quantity
			);
			refresher.refreshUI();
			remove();
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

	@Override
	public void show() {
		super.show(Gui.overlay);
	}

}
