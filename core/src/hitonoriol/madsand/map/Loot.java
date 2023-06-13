package hitonoriol.madsand.map;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.entities.inventory.item.Item;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class Loot {
	private List<Item> contents = new ArrayList<>();
	@JsonIgnore
	private String lootStr;

	public Loot(Item cont) {
		add(cont);
	}

	public Loot() {
		this(Item.nullItem);
	}

	@JsonIgnore
	public String getInfo() {
		if (lootStr == null)
			genInfo();
		return lootStr;
	}

	private void genInfo() { // generates human-readable contents of loot cell
		var ret = "";
		int i = 0, sz = contents.size();
		for (Item item : contents) {
			ret += item.quantity + " " + item.name;
			if (i < sz - 1)
				ret += ", ";
			else
				ret += " ";
			++i;
		}
		lootStr = ret.trim();
	}

	public boolean isEmpty() {
		for (Item content : contents)
			if (content.id() != 0)
				return false;
		return true;
	}

	public Item remove(int idx) {
		var removedItem = contents.remove(idx);
		genInfo();
		return removedItem;
	}

	public Item remove(Item item) {
		int i = 0;
		for (Item itemStack : contents) {
			if (itemStack == item)
				return remove(i);

			if (!itemStack.equals(item))
				continue;

			if (itemStack.quantity > item.quantity) {
				itemStack.quantity -= item.quantity;
				return item;
			}
			++i;
		}
		genInfo();
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

	public void mergeItemStacks() {
		var it = contents.iterator();
		int idx;
		Item item, foundItem;
		while (it.hasNext()) {
			item = it.next();
			if ((idx = contents.indexOf(item)) != -1 && (foundItem = contents.get(idx)) != item) {
				foundItem.quantity += item.quantity;
				it.remove();
			}
		}
		genInfo();
	}

	public int getItemCount() {
		return contents.size();
	}

	public Item get(int idx) {
		return contents.get(idx);
	}

	public List<Item> getContents() {
		return contents;
	}

	public void setContents(List<Item> contents) {
		this.contents = contents;
	}
}
