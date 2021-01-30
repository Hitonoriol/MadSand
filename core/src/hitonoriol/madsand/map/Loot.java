package hitonoriol.madsand.map;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.entities.inventory.item.Item;

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

	public Item remove(int idx) {
		Item removedItem = contents.remove(idx);
		genInfo();
		return removedItem;
	}

	public Item remove(Item item) {
		int i = 0;
		for (Item itemStack : contents) {
			if (itemStack == item)
				return remove(i);
			++i;
		}
		return Item.nullItem;
	}

	public Loot add(int id, int q) {
		if (id == 0)
			return Map.nullLoot;

		return add(Item.create(id, q));
	}

	public Loot add(Item item) {
		if (item.equals(Item.nullItem))
			return Map.nullLoot;

		contents.add(item);
		genInfo();
		return this;
	}
}
