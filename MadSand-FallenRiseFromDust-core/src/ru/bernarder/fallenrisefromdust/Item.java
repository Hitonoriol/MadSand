package ru.bernarder.fallenrisefromdust;

import ru.bernarder.fallenrisefromdust.strings.InventoryNames;

public class Item {
	String name, recipe, heal;
	int id, type, altobject, cost;
	boolean craftable;
	int quantity;

	public Item(int id) {
		this.id = id;
		this.name = InventoryNames.name.get(id);
		this.recipe = InventoryNames.recipe.get(id);
		this.heal = InventoryNames.heal.get(id);
		this.type = InventoryNames.type.get(id);
		this.altobject = InventoryNames.altObject.get(id);
		this.cost = InventoryNames.cost.get(id);
		this.craftable = InventoryNames.craftable.get(id);
		this.quantity = 1;
	}
}
