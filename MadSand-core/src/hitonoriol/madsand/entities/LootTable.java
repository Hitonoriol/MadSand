package hitonoriol.madsand.entities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
			return o1.compareTo(o2);
		}
	});

	private ArrayList<Item> roll(ArrayList<Item> itemList) {
		Item item;
		double roll = Utils.randPercent();

		for (Entry<Float, LootTableEntry> entry : lootTable.entrySet()) {
			item = Item.nullItem;
			if (!Utils.percentRoll(roll, entry.getKey()))
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

	static final String ENTRY_PROBABILITY_DELIM = "\\|";
	static final String TABLE_ENTRY_REGEX = "\\((.*?)\\)";
	static final Pattern tableEntryPattern = Pattern.compile(TABLE_ENTRY_REGEX);

	public static LootTable parse(String lootTblString) {
		LootTable table = new LootTable();
		Matcher entryMatcher = tableEntryPattern.matcher(lootTblString);
		String entryString;
		String entryArr[]; // <probability>|id/maxQuantity:id/maxQuantity:...

		float entryProbability;

		while (entryMatcher.find()) {
			entryString = entryMatcher.group(1);
			entryArr = entryString.split(ENTRY_PROBABILITY_DELIM);
			entryProbability = Float.parseFloat(entryArr[0]);
			table.lootTable.put(entryProbability, parseItemList(entryArr[1]));
		}

		return table;
	}

	private static LootTableEntry parseItemList(String listString) {
		ArrayList<LootItemEntry> lootEntries = new ArrayList<>();
		LootTableEntry tableEntry = new LootTableEntry(lootEntries);

		if (!listString.contains(Item.BLOCK_DELIM))
			listString += Item.BLOCK_DELIM;

		String items[] = listString.split(Item.BLOCK_DELIM);
		String itemArr[];
		for (String itemStr : items) {

			if (itemStr.equals(""))
				continue;

			itemArr = itemStr.split(Item.ITEM_DELIM);
			lootEntries.add(new LootItemEntry(Utils.val(itemArr[0]), Utils.val(itemArr[1])));
		}

		return tableEntry;
	}
}
