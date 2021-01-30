package hitonoriol.madsand.entities;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.entities.inventory.item.Item;

public class LootTable extends RollTable<LootTable.LootEntry> {
	public boolean allowEmptyRoll = false;

	public ArrayList<Item> rollItems() {
		ArrayList<Item> items = new ArrayList<>();
		ArrayList<LootEntry> protoItems = super.roll();
		for (LootEntry protoItem : protoItems)
			items.add(protoItem.makeItem(allowEmptyRoll));
		return items;
	}

	static final String ENTRY_PROBABILITY_DELIM = "\\|";
	static final String TABLE_ENTRY_REGEX = "\\((.*?)\\)";
	static final Pattern tableEntryPattern = Pattern.compile(TABLE_ENTRY_REGEX);

	public static LootTable parse(String lootTblString) {
		if (!lootTblString.contains("|")) {
			try {
				return Resources.mapper.readValue(lootTblString, LootTable.class);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

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

	private static RollTable.Entry<LootEntry> parseItemList(String listString) {
		RollTable.Entry<LootEntry> tableEntry = new RollTable.Entry<>(new ArrayList<>());
		Item.parseListString(listString, (id, quantity) -> tableEntry.items.add(new LootEntry(id, quantity)));
		return tableEntry;
	}

	public static class LootEntry {
		public int id;
		public int maxQuantity;

		public LootEntry(int id, int maxQuantity) {
			this.id = id;
			this.maxQuantity = maxQuantity;
		}

		public LootEntry() {
			this(0, 0);
		}

		public Item makeItem(boolean allowEmptyRoll) {
			int minQuantity = allowEmptyRoll ? 0 : 1;
			return Item.create(id, Utils.rand(minQuantity, maxQuantity));
		}
	}
}
