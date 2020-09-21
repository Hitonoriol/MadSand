package hitonoriol.madsand.entities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import hitonoriol.madsand.Utils;
import hitonoriol.madsand.entities.inventory.Item;

public class LootTable {
	public boolean exclusiveRoll = false; // Stop after the first successful roll
	public int rollCount = 1;
	public SortedMap<Float, LootTableEntry> lootTable = new TreeMap<Float, LootTableEntry>(new Comparator<Float>() {
		@Override
		public int compare(Float o1, Float o2) {
			return o2.compareTo(o1);
		}
	});

	private ArrayList<Item> roll(ArrayList<Item> itemList) {
		Item item;

		for (Entry<Float, LootTableEntry> entry : lootTable.entrySet()) {
			item = Item.nullItem;
			if (!rollEntry(entry.getKey()))
				continue;
			else {
				item = entry.getValue().rollItem();

				if (!item.equals(Item.nullItem))
					itemList.add(item);

				if (exclusiveRoll)
					break;
			}

		}

		return itemList;
	}

	public ArrayList<Item> roll(int times) {
		ArrayList<Item> itemList = new ArrayList<>();

		for (int i = 0; i < times; ++i)
			roll(itemList);

		return itemList;
	}

	public ArrayList<Item> roll() {
		return roll(rollCount);
	}

	private boolean rollEntry(float probability) {
		return Utils.percentRoll(probability);
	}
}
