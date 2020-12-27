package hitonoriol.madsand.map;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.entities.inventory.Inventory;
import hitonoriol.madsand.entities.inventory.Item;

public class Loot {
	public ArrayList<Item> contents = new ArrayList<Item>();
	private String lootStr;

	public Loot(Item cont) {
		add(cont);
	}

	public Loot() {
		this(new Item());
	}

	@JsonIgnore
	public String getInfo() {
		if (lootStr == null)
			genInfo();
		return lootStr;
	}

	private void genInfo() { // generates human-readable contents of loot cell
		String ret = "";
		int i = 0, sz = contents.size();
		for (Item item : contents) {
			ret += item.quantity + " " + item.name;
			if (i < sz - 1)
				ret += ", ";
			else
				ret += " ";
			++i;
		}
		lootStr = ret;
	}

	boolean isEmpty() {
		for (int i = 0; i < contents.size(); ++i)
			if (contents.get(i).id != 0)
				return false;
		return true;
	}

	public Loot remove(int idx) {
		contents.remove(idx);
		genInfo();
		return this;
	}

	public Loot add(int id, int q) {
		if (id == 0)
			return Map.nullLoot;

		return add(new Item(id, q));
	}

	public Loot add(Item item) {

		if (item.equals(Item.nullItem))
			return Map.nullLoot;

		contents.add(item);
		genInfo();
		return this;
	}

	public static int addLootQ(String temp, Inventory inventory, int x, int y, Map map) { // Don't look here
		int i = 0;
		if (temp.equals(Item.EMPTY_ITEM) || temp.equals(Item.EMPTY_ITEM + Item.BLOCK_DELIM))
			return -1;
		if (temp.indexOf(Item.BLOCK_DELIM) == -1)
			temp += Item.BLOCK_DELIM;
		try {
			String[] block = temp.split(Item.BLOCK_DELIM);
			String[] attr = new String[0];
			int id, q;

			while (i < block.length) {
				attr = block[i].split(Item.ITEM_DELIM);
				id = Integer.parseInt(attr[0]);
				q = Integer.parseInt(attr[1]);
				if (inventory != null)
					inventory.putItem(id, q);
				else
					map.putLoot(x, y, id, q);
				i++;
			}

			return 0;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}
}
