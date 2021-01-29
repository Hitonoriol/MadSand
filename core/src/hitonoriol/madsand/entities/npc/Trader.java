package hitonoriol.madsand.entities.npc;

import java.util.ArrayList;

import hitonoriol.madsand.Utils;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.enums.TradeCategory;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.NpcContainer;
import hitonoriol.madsand.properties.NpcProp;

public class Trader extends AbstractNpc {
	public TradeCategory tradeCategory;

	public Trader(NpcContainer protoNpc) {
		tradeCategory = protoNpc.tradeCategory;

		inventory.setMaxWeight(Integer.MAX_VALUE);
		lvl = NpcProp.tradeLists.rollTier();
		ArrayList<Item> items = NpcProp.tradeLists.roll(tradeCategory, lvl);

		int markup;
		for (Item item : items) {
			markup = (int) (item.cost * SELL_PRICE_COEF);
			item.cost += markup + Utils.rand(markup);
		}

		inventory.putItem(items);
		addCurrency();
		rollQuestGiveAbility();
	}

	public Trader() {
		super();
	}

	private static int BASE_TRADER_COINS = 500;
	private static int TIER_COIN_MULTIPLIER = 300;
	private static float SELL_PRICE_COEF = 0.5f;

	public int rollTraderCurrency() {
		int maxCoins = BASE_TRADER_COINS + lvl * TIER_COIN_MULTIPLIER;
		return Utils.rand(BASE_TRADER_COINS / 2, maxCoins);
	}

	private void addCurrency() {
		int currencyId = Globals.getInt(Globals.CURRENCY);
		inventory.putItem(new Item(currencyId, rollTraderCurrency()));
	}

	@Override
	public void interact(Player player) {
		player.interact(this);
	}

	@Override
	public String interactButtonString() {
		return "Trade with ";
	}
}
