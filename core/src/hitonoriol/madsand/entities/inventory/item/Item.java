package hitonoriol.madsand.entities.inventory.item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.badlogic.gdx.graphics.Texture;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import hitonoriol.madsand.DynamicallyCastable;
import hitonoriol.madsand.HotbarAssignable;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.equipment.EquipSlot;
import hitonoriol.madsand.gfx.TextureProcessor;
import hitonoriol.madsand.lua.Lua;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.util.Utils;
import me.xdrop.fuzzywuzzy.FuzzySearch;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY)
@JsonSubTypes({ @Type(Armor.class), @Type(Consumable.class), @Type(CropSeeds.class), @Type(FishingBait.class),
		@Type(GrabBag.class), @Type(Placeable.class), @Type(Projectile.class), @Type(Tool.class), @Type(Weapon.class),
		@Type(Scroll.class), @Type(Pill.class) })
public class Item implements DynamicallyCastable<Item>, HotbarAssignable {
	public int id;
	public int quantity;
	public String name;
	@JsonProperty
	public float weight = DEFAULT_WEIGHT;
	public int cost;
	@JsonIgnore
	private boolean textureFxModified = true;

	public String recipe;
	public int craftQuantity = 1;
	public String useAction;

	public final static Item nullItem = new Item();
	public static final int NULL_ITEM = 0;
	private static final float DEFAULT_WEIGHT = 0.25f;

	private static Map<Item, Texture> dynamicTxPool = new HashMap<>();
	private static Map<Item, TextureProcessor> effectQueue = new HashMap<>();

	public final static String ITEM_DELIM = "/";
	public final static String BLOCK_DELIM = ":";
	public final static String EMPTY_ITEM = "n";
	public final static String CRAFTSTATION_DELIM = "|";

	public Item(Item protoItem) {
		id = protoItem.id;
		quantity = protoItem.quantity > 1 ? protoItem.quantity : 1;
		loadProperties(protoItem);
	}

	public Item(Item item, int quantity) {
		this(item);
		setQuantity(quantity);
	}

	public Item() {
		clear();
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
			Lua.execute(useAction, this);
	}

	public void equip(Player player) {
		player.stats.equipment.equip(this);
	}

	protected void toggleEquipped() {
		MadSand.player().inventory.getUI().equipItem(this);
	}

	/* Some Items may perform different actions when left-clicked from InventoryUI
	 * 		and when used with inventory closed
	 */
	public void leftClickAction() {
		use(MadSand.player());
	}

	@JsonIgnore
	public int getPrice() {
		return cost;
	}

	@JsonIgnore
	public int getTotalPrice() {
		return cost * quantity;
	}

	@JsonIgnore
	public String getFullName() {
		return name;
	}

	@JsonIgnore
	protected String getMiscInfo() {
		return "";
	}

	private String getStackInfo(String propertyName, String units, String singleVal, String stackVal) {
		String info;
		info = String.format("%s: %s %s", propertyName, singleVal, units);

		if (quantity > 1 && !singleVal.equals("0"))
			info += String.format(" (%s %s all)", stackVal, units);

		return info;
	}

	@JsonIgnore
	public String getInfoString() {
		String info;
		info = getFullName()
				+ Resources.LINEBREAK + Resources.LINEBREAK;

		info += getMiscInfo() + Resources.LINEBREAK;
		info += getStackInfo("Weight", "kg", Utils.round(weight), Utils.round(getTotalWeight())) + Resources.LINEBREAK;
		if (cost > 0)
			info += getStackInfo("Cost", "coins", Utils.str(cost), Utils.str(getTotalPrice()));
		return info;
	}

	public Item reinit() {
		loadProperties(ItemProp.getItem(id));
		return this;
	}

	protected void loadProperties(Item properties) {
		if (properties.name != null)
			this.name = properties.name;

		this.recipe = properties.recipe;
		this.weight = properties.weight;
		this.cost = properties.cost;
		this.useAction = properties.useAction;
	}

	@JsonIgnore
	public boolean isCurrency() {
		return id == Globals.values().currencyId;
	}

	@JsonIgnore
	public String getString() {
		if (id == 0)
			return EMPTY_ITEM;
		else
			return id + ITEM_DELIM + quantity;
	}

	@JsonIgnore
	public float getTotalWeight() {
		return weight * (float) quantity;
	}

	public boolean isCraftable() {
		return recipe != null;
	}

	/*
	 *	Returns a new stack with max weight of <weight>
	 *	Current stack is unchanged 
	 */
	public Item split(float weight) {
		Utils.dbg("Split item %s || weight %f", this, weight);
		if (this.weight == 0 || getTotalWeight() <= weight)
			return this;

		Item newStack = copy().setQuantity((int) (weight / this.weight));
		Utils.dbg("new stack: %s", newStack);
		return newStack;
	}

	public void clear() {
		id = NULL_ITEM;
		quantity = 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Item))
			return false;
		if (obj == this)
			return true;

		return equals(((Item) obj).id);
	}

	public boolean equals(int id) {
		return id == this.id;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(14407, 7177).append(id).toHashCode();
	}

	@Override
	public String toString() {
		return String.format("[%d] {%s} [id: %d] %d %s (%.2f kg)",
				hashCode(),
				getClass().getSimpleName(),
				id,
				quantity,
				name,
				getTotalWeight());
	}

	@JsonIgnore
	@Override
	public String getHotbarString() {
		return getFullName();
	}

	@Override
	public void hotbarAction() {
		use(MadSand.player());
	}

	// list string format: id1/quantity1:id2/quantity2:...
	public static void parseListString(String listString, BiConsumer<Integer, Integer> listItemConsumer) {
		if (listString.equals(EMPTY_ITEM))
			return;

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

	protected void refreshTextureEffects() {
		textureFxModified = false;
	}

	@JsonIgnore
	public Texture getTexture() {
		boolean dynTexExists = dynamicTxPool.containsKey(this);

		if (!dynTexExists && textureFxModified) {
			refreshTextureEffects();
			dynTexExists = dynamicTxPool.containsKey(this);
		}

		if (!dynTexExists)
			return Resources.item[id];

		return dynamicTxPool.get(this);
	}

	public void applyEffects(Consumer<TextureProcessor> effectApplier) {
		if (effectApplier == null)
			return;

		Texture texture = Resources.item[id];
		if (!dynamicTxPool.containsKey(this))
			texture = createDynamicTexture();

		TextureProcessor txProc;

		if (effectQueue.containsKey(this))
			txProc = effectQueue.get(this);
		else
			effectQueue.put(this, txProc = new TextureProcessor(texture));

		effectApplier.accept(txProc);
		if (txProc.isDone())
			effectQueue.remove(this);
	}

	protected Texture createDynamicTexture() {
		Texture dynamicTx = TextureProcessor.copyTexture(Resources.item[id]);
		dynamicTxPool.put(this, dynamicTx);
		return dynamicTx;
	}

	protected Item rollProperties() {
		return this;
	}

	public static int getAltObject(int id) {
		return ItemProp.getAltObject(id);
	}

	public static Item getProto(int id) {
		if (ItemProp.items.containsKey(id))
			return ItemProp.getItem(id);

		return nullItem;
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

	public static Item create(String partialName, int quantity) {
		return create(
				FuzzySearch.extractOne(partialName,
						ItemProp.items.values(), item -> item.name == null ? "" : item.name,
						(str1, str2) -> FuzzySearch.tokenSortRatio(str1, str2))
						.getReferent(),
				quantity);
	}

	public static Item create(String partialName) {
		return create(partialName, 1);
	}

	public static Item createRandom() {
		return create(Utils.randElement(ItemProp.items.keySet(), 1));
	}

	public boolean isEquipment() {
		return this instanceof AbstractEquipment;
	}

	public static final Comparator<Item> quantityComparator = (item1, item2) -> {
		return Integer.compare(item1.quantity, item2.quantity);
	};
}
