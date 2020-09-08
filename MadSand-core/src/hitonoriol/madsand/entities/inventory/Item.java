package hitonoriol.madsand.entities.inventory;

import java.util.UUID;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.enums.ItemType;
import hitonoriol.madsand.enums.Skill;
import hitonoriol.madsand.properties.ItemProp;

public class Item {
	public static final int NULL_ITEM = 0;
	private static final float DEFAULT_WEIGHT = 0.25f;

	public String name;
	String recipe;
	String heal;
	int dmg;
	public int hp = -1;
	public int id;
	public int quantity;

	public int satiationAmount, healAmount; // for consumables

	int altobject, cost;
	public ItemType type = ItemType.Item;
	Skill skill = Skill.None;
	boolean unlockable;
	double weight = 0;

	int lvl; // level of item (only for weapons/armor)

	public EquipStats equipStats;

	public String uid = "";

	public final static Item nullItem = new Item();

	public final static String ITEM_DELIM = "/";
	public final static String BLOCK_DELIM = ":";
	public final static String EMPTY_ITEM_STRING = "n";

	public Item(int id) {
		this.id = id;
		this.quantity = 1;
		loadProperties();
	}

	public Item(Item item) {
		this(item.id);
		quantity = item.quantity;
		hp = item.hp;

		if (item.type.isUnique()) {
			uid = item.uid;

			if (item.type.isWeapon() || item.type.isArmor())
				equipStats = new EquipStats(item.equipStats);
		}
	}

	public Item() {
		this(NULL_ITEM);
	}

	public Item(int id, int q) {
		this(id);
		this.quantity = q;
	}

	public Item(String query) {
		if (query == EMPTY_ITEM_STRING) {
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

	public void clear() {
		id = 0;
		loadProperties();
	}

	public Item setQuantity(int quantity) {
		this.quantity = quantity;
		return this;
	}

	@JsonIgnore
	public int getPrice() {
		return ItemProp.cost.get(id);
	}

	String getInfoString() {
		String info = "";
		info += name + Gui.LINEBREAK;

		if (type.isArmor() || type.isWeapon()) {
			info += equipStats.getString();
		}

		if (type == ItemType.Consumable) {
			info += "Satiation: " + satiationAmount + Gui.LINEBREAK;
			info += "Health: " + healAmount + Gui.LINEBREAK;
		}

		info += "Weight: " + Utils.round(weight) + " kg" + Gui.LINEBREAK;
		info += "Cost: " + cost + "$";
		return info;
	}

	public boolean damageTool(Skill skill) {
		if (type.isTool())
			return damage();
		else
			return false;
	}

	public boolean damageTool() {
		return damageTool(Skill.None);
	}

	public int getSkillDamage(Skill skill) {
		if (skill != this.skill)
			return 1;
		else {
			int damage = dmg / Skill.SKILL_DMG_DENOMINATOR;
			if (damage == 0)
				damage = 1;
			return damage;
		}
	}

	boolean damage(int amt) {
		hp -= amt;
		return (hp <= 0);
	}

	boolean damage() {
		return damage(1);
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
		this.weight = ItemProp.weight.getOrDefault(id, DEFAULT_WEIGHT);
		this.recipe = ItemProp.recipe.get(id);
		this.heal = ItemProp.heal.get(id);
		this.type = ItemProp.type.get(id);
		this.altobject = ItemProp.altObject.get(id);
		this.cost = ItemProp.cost.get(id);
		this.dmg = ItemProp.dmg.get(id);
		this.hp = ItemProp.hp.get(id);
		this.unlockable = ItemProp.unlockable.get(id);
		this.skill = ItemProp.skill.get(id);

		if (type == ItemType.Consumable) {
			String con[] = heal.split(Item.BLOCK_DELIM);
			healAmount = Utils.val(con[0]);
			satiationAmount = Utils.val(con[1]);
		}

		if (weight <= 0)
			weight = DEFAULT_WEIGHT;

		if (type.isUnique())
			uid = UUID.randomUUID().toString();

		if (type.isWeapon() || type.isArmor()) {
			this.lvl = ItemProp.lvl.get(id);
			equipStats = new EquipStats(lvl);
		}

		if (type.isWeapon())
			equipStats.strength = ItemProp.str.get(id);
	}

	@JsonIgnore
	public String getString() {
		if (id == 0)
			return EMPTY_ITEM_STRING;
		else
			return id + ITEM_DELIM + quantity;
	}

	double getWeight() {
		return weight * quantity;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Item))
			return false;
		if (obj == this)
			return true;

		Item rhs = (Item) obj;
		return new EqualsBuilder().append(id, rhs.id).append(uid, rhs.uid).isEquals();
	}

	public boolean equals(int id) {
		return id == this.id;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(14407, 7177).append(id).append(uid).toHashCode();
	}

	// Static functions for general item related needs:

	public static String queryToName(String query) { // item query format: id1/quantity1:id2/quantity2:...
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

	public static ItemType getType(int id) {
		return ItemProp.type.get(id);
	}

	public static int getAltObject(int id) {
		return ItemProp.altObject.get(id);
	}
}
