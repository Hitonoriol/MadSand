package hitonoriol.madsand.entities.inventory.trade;

import hitonoriol.madsand.entities.inventory.Inventory;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.properties.Globals;

public class TradeInventory {
	Inventory seller;
	Inventory buyer;
	int currency; //Currency item id

	public TradeInventory(Inventory seller, Inventory buyer, int currency) {
		this.buyer = buyer;
		this.seller = seller;
		this.currency = currency;
	}

	public TradeInventory(Inventory seller, Inventory buyer) {
		this(seller, buyer, Globals.getInt(Globals.CURRENCY));
	}

	public void setCurrency(int id) {
		this.currency = id;
	}

	public boolean sell(Item item, int quantity) { //Sell from Seller to Buyer
		int cost = item.getPrice() * quantity;
		Item buyerMoney = buyer.getItem(currency);

		if (buyerMoney.quantity < cost)
			return false;

		buyer.delItem(currency, cost);
		buyer.putItem(new Item(item).setQuantity(quantity));

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

}
