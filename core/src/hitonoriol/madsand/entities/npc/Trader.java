package hitonoriol.madsand.entities.npc;

import java.util.List;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.inventory.item.category.ItemCategories;
import hitonoriol.madsand.entities.inventory.item.category.ItemCategory;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.NpcContainer;
import hitonoriol.madsand.util.Utils;
import hitonoriol.madsand.world.World;

public class Trader extends AbstractNpc {
	public ItemCategory tradeCategory;

	public Trader(NpcContainer protoNpc) {
		super(protoNpc);
		tradeCategory = protoNpc.tradeCategory;

		inventory.setMaxWeight(Integer.MAX_VALUE);
		lvl = ItemCategories.rollTier();
		List<Item> items = ItemCategories.get().roll(tradeCategory, lvl);

		int markup;
		for (Item item : items) {
			markup = (int) (item.cost * SELL_PRICE_COEF);
			item.cost += markup + Utils.rand(markup);
		}

		inventory.putItem(items);
		addCurrency();
		rollQuestGiveAbility();
	}

	public Trader() {}

	@Override
	protected void despawnProcess() {
		World world = MadSand.world();
		if (!world.isUnderGround() && !world.inEncounter() && !stats().luckRoll())
			damage(0.05f);
		Utils.dbg("Despawning Trader {%s}", this);
	}

	private static int BASE_TRADER_COINS = 500;
	private static int TIER_COIN_MULTIPLIER = 300;
	private static float SELL_PRICE_COEF = 0.75f;

	public int rollTraderCurrency() {
		int maxCoins = BASE_TRADER_COINS + lvl * TIER_COIN_MULTIPLIER;
		return Utils.rand(BASE_TRADER_COINS / 2, maxCoins);
	}

	private void addCurrency() {
		int currencyId = Globals.values().currencyId;
		inventory.putItem(Item.create(currencyId, rollTraderCurrency()));
	}

	@Override
	public void dropInventory() {
		/* Drop items on death only when killed, not auto-despawned */
		if (!isNeutral())
			super.dropInventory();
	}

	@Override
	public void interact(Player player) {
		player.interact(this);
		addLifetime();
	}

	@Override
	public String interactButtonString() {
		return "Trade with ";
	}
}
