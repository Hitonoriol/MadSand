package ru.bernarder.fallenrisefromdust;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import ru.bernarder.fallenrisefromdust.properties.ItemProp;

public class Item {
	String name, recipe, heal;
	public int id;
	public int quantity;
	int type, altobject, cost;
	boolean craftable;
	double weight;

	final static String ITEM_DELIM = "/";
	final static String BLOCK_DELIM = ":";
	final static String EMPTY_QUERY = "n";

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
		if (query == EMPTY_QUERY) {
			this.id = 0;
			loadProperties();
			return;
		}
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

	@Override
	public int hashCode() {
		return new HashCodeBuilder(14407, 7177).append(id).toHashCode();
	}

	Item reinit(int id, int q) {
		this.id = id;
		this.quantity = q;
		loadProperties();
		return this;
	}
	
	Item reinit() {
		loadProperties();
		return this;
	}

	private void loadProperties() {
		this.name = ItemProp.name.get(id);
		this.recipe = ItemProp.recipe.get(id);
		this.heal = ItemProp.heal.get(id);
		this.type = ItemProp.type.get(id);
		this.altobject = ItemProp.altObject.get(id);
		this.cost = ItemProp.cost.get(id);
		this.craftable = ItemProp.craftable.get(id);
	}

	
	String getString() {
		if (id == 0)
			return EMPTY_QUERY;
		else
			return id + ITEM_DELIM + quantity;
	}

	double getWeight() {
		return weight * quantity;
	}

	// Static functions for general item related needs:

	static String queryToName(String query) { // item query format: id1/quantity1:id2/quantity2:...
		int i = 0;
		String ret = "";
		if (!query.contains(BLOCK_DELIM))
			query += BLOCK_DELIM;
		try {
			String[] block = query.split(BLOCK_DELIM);
			String[] attr;
			while (i < block.length) {
				attr = block[i].split(ITEM_DELIM);
				ret = ret + attr[1] + " " + ItemProp.name.get(Integer.parseInt(attr[0])) + " ";
				i++;
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	static int getType(int id) {
		return ItemProp.type.get(id);
	}

	static int getAltObject(int id) {
		return ItemProp.altObject.get(id);
	}
}
