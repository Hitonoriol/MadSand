package hitonoriol.madsand.map;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonSetter;

import hitonoriol.madsand.entities.LootTable;
import hitonoriol.madsand.entities.inventory.item.Item;

public class FishingSpot {
	public int lvl;
	public LootTable fish;

	public ArrayList<Item> catchFish() {
		return fish.rollItems();
	}

	@JsonSetter("fish")
	public void setFish(String lootString) {
		if (lootString != null) {
			fish = LootTable.parse(lootString);
			fish.exclusiveRoll = true;
		}
	}
}
