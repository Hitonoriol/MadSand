package ru.bernarder.fallenrisefromdust;

import org.apache.commons.lang3.builder.EqualsBuilder;

import ru.bernarder.fallenrisefromdust.strings.InventoryNames;

public class Item {
	String name, recipe, heal;
	int id, type, altobject, cost;
	boolean craftable;
	int quantity;
	double weight;

	private final static String ITEM_DELIM = "/";
	private final static String BLOCK_DELIM = ":";

	public Item(int id) {
		this.id = id;
		this.weight = 0.5; // TODO: loadable property
		this.quantity = 1;
		loadProperties();
	}

	public Item() {
		this(0);
	}

	public Item(int id, int q) {
		this(id);
		this.quantity = q;
	}

	public Item(String query) {
		String blocks[];
		blocks = query.split("\\" + ITEM_DELIM);
		id = Integer.parseInt(blocks[0]);
		quantity = Integer.parseInt(blocks[1]);
		loadProperties();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Item))
			return false;
		if (obj == this)
			return true;

		Item rhs = (Item) obj;
		return new EqualsBuilder().append(id, rhs.id).isEquals();
	}

	Item reinit(int id, int q) {
		this.id = id;
		this.quantity = q;
		loadProperties();
		return this;
	}

	private void loadProperties() {
		this.name = InventoryNames.name.get(id);
		this.recipe = InventoryNames.recipe.get(id);
		this.heal = InventoryNames.heal.get(id);
		this.type = InventoryNames.type.get(id);
		this.altobject = InventoryNames.altObject.get(id);
		this.cost = InventoryNames.cost.get(id);
		this.craftable = InventoryNames.craftable.get(id);
	}

	public String getString() {
		return id + ITEM_DELIM + quantity;
	}

	double getWeight() {
		return weight * quantity;
	}

	// Static functions for general item related needs:

	static String queryToName(String query) { // item query format: id1:quantity1/id2:quantity2/...
		int i = 0;
		String ret = "";
		if (!query.contains(BLOCK_DELIM))
			query += BLOCK_DELIM;
		try {
			String[] block = query.split(BLOCK_DELIM);
			String[] attr;
			while (i < block.length) {
				attr = block[i].split(ITEM_DELIM);
				ret = ret + attr[1] + " " + InventoryNames.name.get(Integer.parseInt(attr[0])) + " ";
				i++;
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace(Resource.eps);
		}
		return "";
	}

	static int getType(int id) {
		return InventoryNames.type.get(id);
	}

	static int getAltObject(int id) {
		return InventoryNames.altObject.get(id);
	}
}
