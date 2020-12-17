package hitonoriol.madsand.entities.inventory;

import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.entities.LootTable;
import hitonoriol.madsand.enums.ItemType;
import hitonoriol.madsand.enums.Skill;
import hitonoriol.madsand.enums.Stat;
import hitonoriol.madsand.properties.CropContainer;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.world.World;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Item {
	public static final int NULL_ITEM = 0;
	private static final float DEFAULT_WEIGHT = 0.25f;
	private static final int EQUIPMENT_HP_PER_LVL = 500;

	public int id;
	public String name;
	public int quantity;

	public String recipe;
	public int craftQuantity;
	public String useAction;

	public int dmg;

	public int hp = -1;
	public int maxHp = hp;

	public int satiationAmount, healAmount, staminaAmount; // for consumables

	public int altObject, cost;
	public ItemType type = ItemType.None;
	public Skill skill = Skill.None;
	public float weight = DEFAULT_WEIGHT;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	public LootTable contents;

	public int lvl; // level of item

	public EquipStats equipStats;
	public CropContainer cropContainer;

	public String uid = "";

	public final static Item nullItem = new Item();

	public final static String ITEM_DELIM = "/";
	public final static String BLOCK_DELIM = ":";
	public final static String EMPTY_ITEM = "n";
	public final static String CRAFTSTATION_DELIM = "|";

	public Item(int id) {
		this.id = id;
		this.quantity = 1;
		loadProperties();
	}

	public Item(Item item) {
		this(item.id);
		quantity = item.quantity;
		hp = item.hp;
		name = item.name;

		if (item.type.isUnique()) {
			uid = item.uid;

			if (item.type.isEquipment())
				equipStats = new EquipStats(item.equipStats);

		}
	}

	public Item() {
		this.id = NULL_ITEM;
		quantity = 1;
	}

	public Item(int id, int q) {
		this(id);
		this.quantity = q;

		if (q < 1) {
			this.id = 0;
			loadProperties();
		}

	}

	public Item(String query) {
		if (query.equals(EMPTY_ITEM)) {
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
		return ItemProp.getCost(id);
	}

	String getInfoString() {
		String info = "";
		info += name
				+ (type.isUnique() ? (" [ORANGE][[Lvl " + lvl + "][]") : "")
				+ Resources.LINEBREAK + Resources.LINEBREAK;

		if (type.isArmor() || type.isWeapon()) {
			info += equipStats.getString() + Resources.LINEBREAK;
		}

		if (type.equals(ItemType.Consumable)) {
			info += "Satiation: " + satiationAmount + Resources.LINEBREAK;
			info += "Health: " + healAmount + Resources.LINEBREAK;
			info += "Stamina: " + staminaAmount + Resources.LINEBREAK;
		}

		info += "Weight: " + Utils.round(weight) + " kg" + Resources.LINEBREAK;
		info += "Cost: " + cost + "$";
		return info;
	}

	public boolean damageTool(Skill skill) {
		if (type.isTool() || type.isEquipment())
			return damage();
		else
			return false;
	}

	public boolean damageTool() {
		return damageTool(Skill.None);
	}

	// Item skill damage - roll amount of damage to do to objects
	// Min: 1
	public int getSkillDamage(Skill skill) {
		if (skill != this.skill)
			return 1;
		else
			return Utils.rand(1, dmg / Skill.SKILL_DMG_DENOMINATOR);
	}

	boolean damage(int amt) {
		hp -= amt;
		return (hp <= 0);
	}

	boolean damage() {
		return damage(1);
	}

	Item reinit() {
		EquipStats stats = equipStats;
		String name = this.name;

		loadProperties();

		this.equipStats = stats;
		this.name = name;
		return this;
	}

	private void loadProperties() {
		Item properties = ItemProp.items.get(id);

		this.name = properties.name;
		this.weight = properties.weight;

		this.type = properties.type;
		this.altObject = properties.altObject;
		this.cost = properties.cost;
		this.dmg = properties.dmg;
		this.hp = properties.hp;
		this.maxHp = hp;
		this.skill = properties.skill;
		this.useAction = properties.useAction;
		this.contents = properties.contents;

		boolean isSpecial = false;

		if (type.equals(ItemType.Consumable)) {
			healAmount = properties.healAmount;
			satiationAmount = properties.satiationAmount;
			staminaAmount = properties.staminaAmount;
		}

		if (type.isUnique())
			uid = UUID.randomUUID().toString();

		if (type.isWeapon() || type.isArmor()) {
			this.lvl = properties.lvl;

			if (isSpecial = World.player.stats.luckRoll()) {
				++this.lvl;
				name += " of " + Utils.randWord();
			}

			equipStats = new EquipStats(lvl, type);
			hp = ((lvl == 0 ? 1 : lvl) * EQUIPMENT_HP_PER_LVL);
			maxHp = hp;
		}

		if (type.isWeapon() && !isSpecial)
			equipStats.stats.set(Stat.Strength, properties.equipStats.stats);

	}

	@JsonIgnore
	public float getHpPercent() {
		return 100 * ((float) hp / (float) maxHp);
	}

	@JsonSetter("contents")
	public void setContents(String contents) {
		if (contents == null)
			return;

		if (contents.contains("|"))
			this.contents = LootTable.parse(contents);
		else
			try {
				this.contents = Resources.mapper.readValue(contents, LootTable.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	@JsonIgnore
	public boolean isCurrency() {
		return id == Globals.getInt(Globals.CURRENCY);
	}

	@JsonIgnore
	public String getString() {
		if (id == 0)
			return EMPTY_ITEM;
		else
			return id + ITEM_DELIM + quantity;
	}

	public float getWeight() {
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
				ret = ret + attr[1] + " " + ItemProp.getItemName(Integer.parseInt(attr[0])) + " ";
				i++;
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static ArrayList<Integer> parseCraftRequirements(String recipe) {

		ArrayList<Integer> requirements = new ArrayList<>();

		if (!recipe.contains(BLOCK_DELIM))
			recipe += BLOCK_DELIM;

		String[] itemBlocks = recipe.split(BLOCK_DELIM);

		for (String itemBlock : itemBlocks)
			requirements.add(Utils.val(itemBlock.split(ITEM_DELIM)[0]));

		return requirements;

	}

	public static ItemType getType(int id) {
		return ItemProp.getType(id);
	}

	public static int getAltObject(int id) {
		return ItemProp.getAltObject(id);
	}
}
