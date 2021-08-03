package hitonoriol.madsand.entities.inventory.trade;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import hitonoriol.madsand.entities.inventory.ItemTooltip;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.gui.widgets.itembutton.ItemButton;
import hitonoriol.madsand.resources.Resources;

public class TradeInventoryButton extends ItemButton {
	TradeInventory trade;
	TradeAction action;
	TradeUIRefresher refresher;
	ItemTooltip tooltip;
	boolean isCurrencyButton;

	public TradeInventoryButton(TradeInventory trade, Item item, TradeAction action, TradeUIRefresher refresher) {
		super(item);

		if (item.isEquipment()) {
			tooltip = new ItemTooltip(item);
			super.addListener(tooltip);
		}

		this.trade = trade;
		this.action = action;
		this.refresher = refresher;

		super.setUpListeners();
	}

	protected String createButtonText() {
		String btnText = buttonItem.quantity + Resources.Space + buttonItem.getFullName();
		isCurrencyButton = buttonItem.isCurrency();

		if (isCurrencyButton)
			return btnText;

		return btnText + ", " + buttonItem.getPrice() + " each";
	}

	protected ClickListener setButtonPressListener() {
		return new ClickListener(Buttons.LEFT) {
			public void clicked(InputEvent event, float x, float y) {
				if (!isCurrencyButton && buttonItem.getPrice() > 0)
					new TradeConfirmDialog(trade, buttonItem, action, refresher).show();
			}
		};
	}

}
