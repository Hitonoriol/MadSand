package ru.bernarder.fallenrisefromdust;

public class Loot {
	int id, quantity;

	public Loot(int id, int quantity) {	// LootNode(Vector of Loot objs) <-- Loot
		this.id = id;					// + add constructor from string loot node(for craft recipes)
		this.quantity = quantity;
	}
}
