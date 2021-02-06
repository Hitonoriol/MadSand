package hitonoriol.madsand.entities.inventory.item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import hitonoriol.madsand.DynamicallyCastable;
import hitonoriol.madsand.LuaUtils;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.entities.EquipSlot;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.ItemProp;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY)
@JsonSubTypes({ @Type(Armor.class), @Type(Consumable.class), @Type(CropSeeds.class), @Type(FishingBait.class),
		@Type(GrabBag.class), @Type(Placeable.class), @Type(Projectile.class), @Type(Tool.class), @Type(Weapon.class) })
public class Item implements DynamicallyCastable<Item>{

	public int id;
	public int quantity;
	public String name;
	@JsonProperty
	public float weight = DEFAULT_WEIGHT;
	public int cost;

	public String recipe;
	public int craftQuantity = 1;
	public String useAction;

	public final static Item nullItem = new Item();
	public static final int NULL_ITEM = 0;
	private static final float DEFAULT_WEIGHT = 0.25f;

	public final static String ITEM_DELIM = "/";
	public final static String BLOCK_DELIM = ":";
	public final static String EMPTY_ITEM = "n";
	public final static String CRAFTSTATION_DELIM = "|";

	public Item(Item protoItem) {
		id = protoItem.id;
		quantity = 1;
		loadProperties(protoItem);
	}

	public Item(Item item, int quantity) {
		this(item);
		setQuantity(quantity);
	}

	public Item() {
		this.id = NULL_ITEM;
		quantity = 0;
	}

	public Item copy() {
		return new Item(this);
	}

	public Item setQuantity(int quantity) {
		this.quantity = quantity;
		return this;
	}

	public void use(Player player) {
		if (useAction != null)
			LuaUtils.execute(useAction, this);
	}

	public void equip(Player player) {
		player.stats.equipment.equip(this);
	}

	@JsonIgnore
	public int getPrice() {
		return ItemProp.getCost(id);
	}

	@JsonIgnore
	public String getFullName() {
		return name;
	}

	@JsonIgnore
	protected String getMiscInfo() {
		return "";
	}

	@JsonIgnore
	public String getInfoString() {
		String info = "";
		info += getFullName()
				+ Resources.LINEBREAK + Resources.LINEBREAK;

		info += getMiscInfo();

		info += Resources.LINEBREAK;
		info += "Weight: " + Utils.round(weight) + " kg" + Resources.LINEBREAK;
		info += "Cost: " + cost + "$";
		return info;
	}

	public Item reinit() {
		loadProperties(ItemProp.getItem(id));
		return this;
	}

	protected void loadProperties(Item properties) {
		this.name = properties.name;
		this.weight = properties.weight;
		this.cost = properties.cost;
		this.useAction = properties.useAction;
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

	@JsonIgnore
	public float getWeight() {
		return weight * (float) quantity;
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

	public boolean equals(int id) {
		return id == this.id;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(14407, 7177).append(id).toHashCode();
	}

	// list string format: id1/quantity1:id2/quantity2:...
	public static void parseListString(String listString, BiConsumer<Integer, Integer> listItemConsumer) {
		if (!listString.contains(BLOCK_DELIM))
			listString += BLOCK_DELIM;

		String listItems[] = listString.split(BLOCK_DELIM);
		String itemAttrs[];
		for (String itemStr : listItems) {
			itemAttrs = itemStr.split(ITEM_DELIM);
			listItemConsumer.accept(Utils.val(itemAttrs[0]), Utils.val(itemAttrs[1]));
		}
	}

	public static ArrayList<Item> parseItemString(String itemListStr) {
		ArrayList<Item> items = new ArrayList<>();
		parseListString(itemListStr, (id, quantity) -> items.add(Item.create(id, quantity)));
		return items;
	}

	public static String createReadableItemList(String itemListStr) {
		StringBuilder ret = new StringBuilder();
		parseListString(itemListStr, (id, quantity) -> ret.append(quantity + " " + ItemProp.getItemName(id) + " "));
		return ret.toString();
	}

	public static ArrayList<Integer> parseCraftRequirements(String recipe) {
		ArrayList<Integer> requirements = new ArrayList<>();
		parseListString(recipe, (id, quantity) -> requirements.add(id));
		return requirements;

	}

	public EquipSlot getEquipSlot() {
		return EquipSlot.MainHand;
	}

	protected Item rollProperties() {
		return this;
	}

	public static int getAltObject(int id) {
		return ItemProp.getAltObject(id);
	}

	public static Item duplicate(Item item, int quantity) {
		return item.copy().setQuantity(quantity);
	}

	public static Item create(Item item, int quantity) {
		return duplicate(item, quantity).rollProperties();
	}

	public static Item create(int id, int quantity) {
		return create(ItemProp.getItem(id), quantity);
	}

	public static Item create(int id) {
		return create(id, 1);
	}

	public boolean isEquipment() {
		return this instanceof AbstractEquipment;
	}

	public static final Comparator<Item> quantityComparator = (item1, item2) -> {
		return Integer.compare(item1.quantity, item2.quantity);
	};
}
