package hitonoriol.madsand.entities.inventory.item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.node.ObjectNode;

import hitonoriol.madsand.Enumerable;
import hitonoriol.madsand.HotbarAssignable;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.dialog.TextSubstitutor;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.equipment.EquipSlot;
import hitonoriol.madsand.entities.inventory.item.category.ItemCategories;
import hitonoriol.madsand.entities.inventory.item.category.ItemCategory;
import hitonoriol.madsand.gamecontent.Globals;
import hitonoriol.madsand.gamecontent.Items;
import hitonoriol.madsand.gamecontent.Textures;
import hitonoriol.madsand.gfx.TextureProcessor;
import hitonoriol.madsand.lua.Lua;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.RomanNumber;
import hitonoriol.madsand.util.Utils;
import hitonoriol.madsand.util.cast.DynamicallyCastable;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY)
@JsonSubTypes(
	{ @Type(Armor.class), @Type(Consumable.class), @Type(CropSeeds.class), @Type(FishingBait.class),
		@Type(GrabBag.class), @Type(PlaceableItem.class), @Type(Projectile.class), @Type(Tool.class),
		@Type(Weapon.class),
		@Type(ScriptedConsumable.class) }
)
public class Item implements DynamicallyCastable<Item>, HotbarAssignable, Enumerable {
	public static final int MAX_TIER = 10;
	private final static char NON_UNLOCKABLE_CHAR = 'X';
	public final static String ITEM_DELIM = "/", BLOCK_DELIM = ":";
	public final static String EMPTY_ITEM = "n";
	public final static String CRAFTSTATION_DELIM = "|";

	public final static Item nullItem = new Item();
	public static final int NULL_ITEM = 0;
	private static final float DEFAULT_WEIGHT = 0.25f;

	private static Map<Item, Texture> dynamicTxPool = new HashMap<>();
	private static Map<Item, TextureProcessor> effectQueue = new HashMap<>();
	private static int lastId = 0;

	protected int id;
	public int quantity;
	public String name;
	@JsonProperty
	public float weight = DEFAULT_WEIGHT;
	public int cost;
	@JsonProperty
	private int tier = 1;
	@JsonIgnore
	private boolean textureFxModified = true;
	public String useAction;

	/* If false, recipe can only be learned by using the corresponding recipe ScriptedConsumable */
	private boolean unlockableRecipe = true;
	public String recipe;
	public int craftQuantity = 1;

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

	@Override
	public int id() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
		lastId = Math.max(id, lastId);
	}

	@Override
	public String name() {
		return name;
	}

	public static int getLastId() {
		return lastId;
	}

	public Item setQuantity(int quantity) {
		this.quantity = quantity;
		return this;
	}

	public int getQuantity() {
		return quantity;
	}

	public void use(Player player) {
		if (useAction != null)
			Lua.execute(useAction, this);
	}

	public void equip(Player player) {
		player.stats.equipment.equip(this);
	}

	protected void toggleEquipped() {
		var player = MadSand.player();
		if (player.stats().equipment.itemEquipped(this))
			player.unEquip(this);
		else
			equip(player);
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
	public boolean isRecipeUnlockable() {
		return unlockableRecipe;
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

	public String getTierString() {
		boolean special = isSpecialTier();
		float h = special ? 290 : (50 + 310 * ((float)tier / MAX_TIER));
		float s = special ? 0.7f : 1.0f;
		float v = special ? 1.0f : 0.64f;
		var color = new Color(Color.BLACK);
		
		String tierString;
		if (special) {
			tierString = Utils.colorizeText("Special", color.fromHsv(h, s, v));
		} else {
			tierString = Utils.colorizeText("Tier", color.fromHsv(h, 0.2f, v)) + " "
				+ Utils.colorizeText(RomanNumber.toRoman(tier), color.fromHsv(h, s, v));
		}
		
		return Utils.colorizeText("[[", color.fromHsv(h, 0.3f, 1.0f))
			+ tierString
			+ Utils.colorizeText("]", color.fromHsv(h, 0.3f, 1.0f)); 
	}
	
	@JsonIgnore
	public String getInfoString() {
		String info;
		info = getTierString() + " " + getFullName()
			+ Resources.LINEBREAK + Resources.LINEBREAK;

		info += getMiscInfo() + Resources.LINEBREAK;
		info += getStackInfo("Weight", "kg", Utils.round(weight), Utils.round(getTotalWeight())) + Resources.LINEBREAK;
		if (cost > 0)
			info += getStackInfo("Cost", "coins", Utils.str(cost), Utils.str(getTotalPrice()));
		return info;
	}

	public Item reinit() {
		loadProperties(Items.all().get(id));
		return this;
	}

	protected void loadProperties(Item properties) {
		if (properties.name != null)
			name = properties.name;

		recipe = properties.recipe;
		weight = properties.weight;
		cost = properties.cost;
		useAction = properties.useAction;
	}

	/* Generate craft requirement lists for this item if it has a recipe
	 * Recipes have a format of item list string (id/quantity:id/quantity:...) with additional modifiers
	 * 		for craft station recipes (station_id|id/quantity:...) & non-unlockable items (Xid/quantity:...)
	 */
	public void initRecipe() {
		if (recipe == null)
			return;

		recipe = TextSubstitutor.replace(recipe);
		/* If only craftable at a crafting station */
		if (recipe.contains(CRAFTSTATION_DELIM)) {
			String[] craftStationRecipe = recipe.split("\\" + Item.CRAFTSTATION_DELIM);
			recipe = craftStationRecipe[1];
			Items.all().addCraftStationRecipe(Utils.val(craftStationRecipe[0]), id);
		}
		/* If craftable by hand */
		else {
			/* Non-unlockable recipes begin with X character, e.g. X1/3:2/5:... */
			unlockableRecipe = recipe.charAt(0) != NON_UNLOCKABLE_CHAR;
			if (!unlockableRecipe)
				recipe = recipe.substring(1);

			Items.all().craftRequirements().put(id, parseCraftRequirements(recipe));
		}

	}

	public void setSpecialTier() {
		this.tier = -1;
	}
	
	public boolean isSpecialTier() {
		return tier == -1;
	}
	
	protected final void setTier(int tier) {
		this.tier = Math.min(MAX_TIER, Math.max(1, tier));
	}
	
	public int getTier() {
		return tier;
	}
	
	protected final void setCategory(ItemCategory category, int tier) {
		setTier(tier);
		ItemCategories.get().addItem(id, category, tier);
	}

	protected final void setCategory(ItemCategory category) {
		setCategory(category, cost / 30);
	}

	public void initCategory() {}

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
		return weight * quantity;
	}

	public float getWeight() {
		return weight;
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

		var newStack = copy().setQuantity((int) (weight / this.weight));
		Utils.dbg("new stack: %s", newStack);
		return newStack;
	}

	public void clear() {
		id = NULL_ITEM;
		quantity = 0;
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
			createDynamicTexture();

		return dynamicTxPool.get(this);
	}

	public void applyEffects(Consumer<TextureProcessor> effectApplier) {
		if (effectApplier == null)
			return;

		Texture texture = null;
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
		var dynamicTx = TextureProcessor.createTexture(Textures.getItem(id));
		dynamicTxPool.put(this, dynamicTx);
		return dynamicTx;
	}

	@JsonIgnore
	public Drawable getDrawable() {
		if (isProto())
			return new TextureRegionDrawable(Textures.getItem(id));

		return new TextureRegionDrawable(getTexture());
	}

	protected Item rollProperties() {
		return this;
	}

	protected boolean isProto() {
		return Items.all().get(id) == this;
	}

	public boolean isEquipment() {
		return this instanceof AbstractEquipment;
	}

	/*
	 * ItemCategories are stored as Map<Category, Tier> entries
	 * (to make it possible to include 1 item in multiple categories)
	 * "category" : { "CategoryName" : <tier>, ... }
	 */
	@JsonSetter("category")
	private void setCategory(ObjectNode categoryNodeList) {
		Items.deferInit(
			() -> categoryNodeList.fieldNames()
				.forEachRemaining(
					categoryName -> ItemCategories.get()
						.addItem(
							id,
							ItemCategory.valueOf(categoryName),
							categoryNodeList.get(categoryName).intValue()
						)
				)
		);
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
		return String.format(
			"[%X] {%s} [id: %d] %d %s (%.2f kg)",
			hashCode(),
			getClass().getSimpleName(),
			id,
			quantity,
			name,
			getTotalWeight()
		);
	}

	// list string format: id1/quantity1:id2/quantity2:...
	public static void parseListString(String listString, BiConsumer<Integer, Integer> listItemConsumer) {
		if (listString.equals(EMPTY_ITEM) || listString.isEmpty())
			return;

		if (!listString.contains(BLOCK_DELIM))
			listString += BLOCK_DELIM;

		listString = TextSubstitutor.replace(listString);
		String listItems[] = listString.split(BLOCK_DELIM);
		String itemAttrs[];
		for (String itemStr : listItems) {
			itemAttrs = itemStr.split(ITEM_DELIM);
			listItemConsumer.accept(Utils.val(itemAttrs[0]), Utils.val(itemAttrs[1]));
		}
	}

	public static ArrayList<Item> parseItemString(String itemListStr) {
		var items = new ArrayList<Item>();
		parseListString(itemListStr, (id, quantity) -> items.add(Item.create(id, quantity)));
		return items;
	}

	public static String createReadableItemList(String itemListStr, boolean countItems) {
		final var delim = ", ";
		var ret = new StringBuilder();
		parseListString(itemListStr, (id, quantity) -> {
			if (countItems)
				ret.append(MadSand.player().inventory.countItems(id) + "/");
			ret.append(quantity + " " + Items.all().getName(id) + ", ");
		});

		if (ret.length() > 0) {
			ret.setLength(ret.length() - delim.length());
			int lastComma = ret.lastIndexOf(delim);
			if (lastComma != -1) {
				ret.delete(lastComma, lastComma + delim.length());
				ret.insert(lastComma, " and ");
			}
		}

		return ret.toString();
	}

	public static String createReadableItemList(String itemListStr) {
		return createReadableItemList(itemListStr, false);
	}

	public static ArrayList<Integer> parseCraftRequirements(String recipe) {
		var requirements = new ArrayList<Integer>();
		parseListString(recipe, (id, quantity) -> requirements.add(id));
		return requirements;
	}

	public static int getAltObject(int id) {
		return Items.all().getAltObject(id);
	}

	public static Item getProto(int id) {
		if (Items.all().get().containsKey(id))
			return Items.all().get(id);

		return nullItem;
	}

	public static Item duplicate(Item item, int quantity) {
		return item.copy().setQuantity(quantity);
	}

	public static Item create(Item item, int quantity) {
		return duplicate(item, quantity).rollProperties();
	}

	public static Item create(int id, int quantity) {
		return create(Items.all().get(id), quantity);
	}

	public static Item create(int id) {
		return create(id, 1);
	}

	public static Item create(String partialName, int quantity) {
		return create(Enumerable.find(Items.all().get(), partialName), quantity);
	}

	public static Item create(String partialName) {
		return create(partialName, 1);
	}

	public static Item createRandom() {
		return create(Utils.randElement(Items.all().get().keySet(), 1));
	}

	public static int dynamicTextureCacheSize() {
		return dynamicTxPool.size();
	}

	public static final Comparator<Item> quantityComparator = Comparator.comparing(item1 -> item1.quantity);
}
