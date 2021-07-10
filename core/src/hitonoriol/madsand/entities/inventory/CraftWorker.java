package hitonoriol.madsand.entities.inventory;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import hitonoriol.madsand.entities.Entity;
import hitonoriol.madsand.entities.inventory.item.Item;

public class CraftWorker {
	private Entity entity;

	private Item itemToCraft;
	private int maxCraftQuantity;

	public CraftWorker(Entity entity, Item itemToCraft) {
		this.entity = entity;
		this.itemToCraft = itemToCraft;
		calcMaxCraftQuantity();
	}

	private void calcMaxCraftQuantity() {
		if (!entity.inventory.itemsExist(itemToCraft.recipe)) {
			maxCraftQuantity = 0;
			return;
		}

		List<Integer> reqQuantities = Item.parseItemString(itemToCraft.recipe)
				.stream()
				.mapToInt(item -> entity.inventory.getItem(item).quantity / item.quantity)
				.boxed()
				.collect(Collectors.toList());
		maxCraftQuantity = itemToCraft.isEquipment() ? 1 : Collections.min(reqQuantities);
	}

	public int getMaxCraftQuantity() {
		return maxCraftQuantity;
	}

	public boolean canBeCrafted() {
		return maxCraftQuantity > 0;
	}

	public Item craftItem(int quantity) {
		if (quantity > maxCraftQuantity)
			return Item.nullItem;

		for (int i = 0; i < quantity; ++i)
			entity.inventory.delItem(itemToCraft.recipe);

		Item craftedItem = Item.create(itemToCraft.id, quantity * itemToCraft.craftQuantity);
		entity.addItem(craftedItem);
		return craftedItem;
	}

	public Item getItem() {
		return itemToCraft;
	}
}
