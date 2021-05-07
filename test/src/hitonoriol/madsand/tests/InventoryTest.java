package hitonoriol.madsand.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.MutableFloat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.entities.Entity;
import hitonoriol.madsand.entities.inventory.CraftWorker;
import hitonoriol.madsand.entities.inventory.Inventory;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.inventory.trade.TradeInventory;
import hitonoriol.madsand.entities.npc.AbstractNpc;
import hitonoriol.madsand.map.Loot;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.properties.NpcProp;
import hitonoriol.madsand.util.Functional;
import hitonoriol.madsand.util.Utils;
import me.xdrop.jrand.JRand;
import me.xdrop.jrand.generators.basics.NaturalGenerator;

public class InventoryTest {
	Entity player = MadSand.player();
	AbstractNpc npc = NpcProp.spawnNpc(1);
	Inventory inventory = player.inventory;
	Inventory npcInv = npc.inventory;
	Map map = MadSand.world().getCurLoc();
	int currency = Globals.getCurrency().id;

	@BeforeEach
	void setUp() {
		inventory.clear();
		npcInv.clear();
		inventory.setMaxWeight(Float.MAX_VALUE);
		npcInv.setMaxWeight(Float.MAX_VALUE);
	}

	@RepeatedTest(10)
	void addRemoveItemTest() {
		MutableFloat expectedWeight = new MutableFloat(0);
		Functional.repeat(15, () -> {
			Item item = getRandomItemStack();
			if (inventory.putItem(item))
				expectedWeight.add(item.getWeight());
		});
		assertEquals(expectedWeight.getValue(), inventory.getTotalWeight(),
				"Total weight of inventory should be the same");

		Item itemToRemove = pickRandom(inventory);
		int remQuantity = Utils.rand(1, itemToRemove.quantity);
		float expectedDelta = itemToRemove.weight * (float) remQuantity;
		inventory.delItem(itemToRemove.id, remQuantity);
		/* if previous assert succeeded, expectedWeight still holds correct inventory weight */
		assertEquals(expectedWeight.getValue() - expectedDelta, inventory.getTotalWeight(),
				"Total weight should decrease by the weight of the item stack");
	}

	@RepeatedTest(10)
	void dropInventoryTest() {
		addRandomItems(inventory);
		List<Item> items = copyInventoryContents(inventory);
		player.dropInventory();
		assertTrue(inventory.isEmpty(), "Inventory should be empty after drop");

		Loot loot = map.getLoot(player.x, player.y);
		items.removeAll(loot.contents);
		Utils.out("Inventory \\ Loot:");
		items.forEach(item -> Utils.out(item.toString()));
		assertTrue(loot.contents.containsAll(items), "All dropped items must be present at the drop location");
	}

	@RepeatedTest(10)
	void tradingTest() {
		addRandomItems(inventory);
		addRandomItems(npcInv);

		inventory.putItem(currency, 1000);
		npcInv.putItem(currency, 1000);

		TradeInventory playerToNpc = new TradeInventory(inventory, npcInv);
		TradeInventory npcToPlayer = new TradeInventory(npcInv, inventory);

		Functional.repeat(10, () -> {
			Item playerSells = pickRandom(inventory);
			int pSellQuantity = pickQuantity(playerSells);
			Item npcSells = pickRandom(npcInv);
			int nSellQuantity = pickQuantity(npcSells);

			ensureAffordability(player, npcSells, nSellQuantity);
			ensureAffordability(npc, playerSells, pSellQuantity);
			sellTest(playerToNpc, playerSells, pSellQuantity);
			sellTest(npcToPlayer, npcSells, nSellQuantity);
		});
	}

	private void sellTest(TradeInventory seller, Item item, int quantity) {
		int totalCost = quantity * item.cost;

		Inventory sellerInv = seller.getSeller();
		int expSellerItem = item.quantity - quantity;
		int expSellerCurrency = sellerInv.getItem(currency).quantity + totalCost;

		Inventory buyerInv = seller.getBuyer();
		int expBuyerItem = buyerInv.getItem(item).quantity + quantity;
		int expBuyerCurrency = buyerInv.getItem(currency).quantity - totalCost;

		seller.sell(item, quantity);

		assertEquals(expSellerItem, sellerInv.getItem(item).quantity, "Seller's item quantity should decrease");
		assertEquals(expSellerCurrency, sellerInv.getItem(currency).quantity, "Seller's currency should increase");

		assertEquals(expBuyerItem, buyerInv.getItem(item).quantity, "Buyer's item quantity should increase");
		assertEquals(expBuyerCurrency, buyerInv.getItem(currency).quantity, "Buyer's currency should decrease");
	}

	private void ensureAffordability(Entity entity, Item item, int quantity) {
		int price = item.cost * quantity;
		if (!entity.canAfford(price))
			entity.addItem(currency, price);
	}

	@RepeatedTest(10)
	void craftingTest() {
		Item itemToCraft = getRandomCraftableItem();
		Utils.out("Trying to craft %s", itemToCraft);
		assertTrue(itemToCraft.isCraftable(), itemToCraft + " should be craftable");

		List<Item> craftRecipe = Item.parseItemString(itemToCraft.recipe);
		player.addItem(craftRecipe);
		inventory.dump();
		assertTrue(inventory.containsAll(craftRecipe), "All crafting ingredients should be present in inventory");

		CraftWorker craftWorker = new CraftWorker(player, itemToCraft);
		assertTrue(craftWorker.canBeCrafted(), "CraftWorker: " + itemToCraft + " can't be crafted");

		craftWorker.craftItem(1);
		assertTrue(player.hasItem(itemToCraft.id), "Failed to add crafted item to inventory");
		assertTrue(inventory.containsNone(craftRecipe), "Failed to remove ingredients from inventory");
	}

	private int pickQuantity(Item item) {
		return Utils.rand(1, item.quantity);
	}

	private Item pickRandom(Inventory inventory) {
		Item item;
		do
			item = Utils.randElement(inventory.items);
		while (item.isCurrency());

		return item;
	}

	private Item getRandomCraftableItem() {
		return Item.create(Utils.randElement(ItemProp.craftReq.keySet()));
	}

	private void addRandomItems(Inventory inventory) {
		Functional.repeat(Utils.rand(15, 50),
				() -> inventory.putItem(getRandomItemStack()));
	}

	NaturalGenerator itemQuantityGen = JRand.natural().range(1, 35);

	private Item getRandomItemStack() {
		return Item.createRandom().setQuantity(itemQuantityGen.gen());
	}

	private List<Item> copyInventoryContents(Inventory inventory) {
		return inventory.items.stream()
				.map(item -> item.copy())
				.collect(Collectors.toList());
	}
}
