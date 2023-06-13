package hitonoriol.madsand.entities.inventory.trade;

import hitonoriol.madsand.entities.inventory.Inventory;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.gamecontent.Globals;

public class TradeInventory {
	Inventory seller;
	Inventory buyer;
	int currency;

	public TradeInventory(Inventory seller, Inventory buyer, int currency) {
		this.buyer = buyer;
		this.seller = seller;
		this.currency = currency;
	}

	public TradeInventory(Inventory seller, Inventory buyer) {
		this(seller, buyer, Globals.values().currencyId);
	}

	public void setCurrency(int id) {
		currency = id;
	}

	public boolean sell(Item item, int quantity) { //Sell from Seller to Buyer
		int cost = item.getPrice() * quantity;
		var buyerMoney = buyer.getItem(currency);

		if (buyerMoney.quantity < cost)
			return false;

		buyer.delItem(currency, cost);
		buyer.putItem(Item.duplicate(item, quantity));

		seller.putItem(currency, cost);
		seller.delItem(item, quantity);

		return true;
	}

	public Inventory getSeller() {
		return seller;
	}

	public Inventory getBuyer() {
		return buyer;
	}

	static int maxAffordableQuantity(Inventory buyer, Item item) {
		int buyerMoney = buyer.getItem(Globals.values().currencyId).quantity;
		return Math.min(item.quantity, buyerMoney / item.getPrice());
	}

	static String getBuyerString(TradeAction tradeAction) {
		return tradeAction == TradeAction.Buy ? "You" : "Trader";
	}
}
