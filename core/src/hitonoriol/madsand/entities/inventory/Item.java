package hitonoriol.madsand.entities.inventory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.LootTable;
import hitonoriol.madsand.entities.Skill;
import hitonoriol.madsand.entities.Stat;
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
	public int craftQuantity = 1;
	public String useAction;

	public int dmg;

	public int hp = -1;
	public int maxHp = hp;

	public int satiationAmount, healAmount, staminaAmount; // for consumables

	public int altObject, cost;
	public ItemType type = ItemType.None;
	public Skill skill = Skill.None;
	@JsonProperty
	public float weight = DEFAULT_WEIGHT;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	public LootTable contents;

	public int lvl; // level of item

	public EquipStats equipStats;
	public CropContainer cropContainer;

	public long uid = 0;

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

	public Item(Item item, int quantity) {
		this(item);
		setQuantity(quantity);
	}

	public Item() {
		this.id = NULL_ITEM;
		quantity = 0;
	}

	public Item(int id, int q) {
		this(id);
		this.quantity = q;

		if (q < 1) {
			this.id = 0;
			loadProperties();
		}

	}

	public Item(String itemStr) {
		parseListString(itemStr, (id, quantity) -> {
			this.id = id;
			this.quantity = quantity;
		});
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

		if (type.isArmor() || type.isWeapon())
			info += equipStats.getString();

		if (type == ItemType.Consumable) {
			info += "Satiation: " + satiationAmount + Resources.LINEBREAK;
			info += "Health: " + healAmount + Resources.LINEBREAK;
			info += "Stamina: " + staminaAmount + Resources.LINEBREAK;
			info += "Nutritional value: " + getNutritionalValue() + Resources.LINEBREAK;
		}

		if (type == ItemType.Projectile)
			info += "Projectile damage: [GREEN]" + dmg + Resources.COLOR_END + Resources.LINEBREAK;

		info += Resources.LINEBREAK;
		info += "Weight: " + Utils.round(weight) + " kg" + Resources.LINEBREAK;
		info += "Cost: " + cost + "$";
		return info;
	}

	public int getNutritionalValue() {
		return (int) (0.125f * satiationAmount + 0.275f * healAmount + 0.525f * staminaAmount);
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
		if (!type.isUnique())
			loadProperties();
		return this;
	}

	protected void loadProperties() {
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
			uid = MadSand.world.itemCounter++;

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

	static final float BASE_PROJECTILE_SPEED = 0.35f;

	public void launchProjectile(Pair from, Pair to, Runnable impactAction) {
		final int imgSize = (int) (MadSand.TILESIZE * MadSand.gameWorld.getCamZoom());
		Image projectileImg = new Image(Resources.item[id]);
		Vector3 screenCoords = new Vector3();
		projectileImg.setOrigin(Align.center);
		projectileImg.setSize(imgSize, imgSize);
		projectileImg
				.addAction(Actions.rotateTo((float) Math.toDegrees(Math.atan2(from.y - to.y, from.x - to.x)) + 90f));
		Gui.overlay.addActor(projectileImg);

		MadSand.getCamera().project(screenCoords.set(from.x, from.y, 0));
		projectileImg.setPosition(screenCoords.x, screenCoords.y);

		MadSand.getCamera().project(screenCoords.set(to.x, to.y, 0));
		projectileImg.addAction(
				Actions.sequence(
						Actions.moveTo(screenCoords.x, screenCoords.y, BASE_PROJECTILE_SPEED),
						Actions.run(impactAction),
						Actions.run(() -> projectileImg.remove())));
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
		return new EqualsBuilder().append(id, rhs.id).append(uid, rhs.uid).isEquals();
	}

	public boolean equals(int id) {
		return id == this.id;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(14407, 7177).append(id).append(uid).toHashCode();
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
		parseListString(itemListStr, (id, quantity) -> items.add(new Item(id, quantity)));
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

	public static ItemType getType(int id) {
		return ItemProp.getType(id);
	}

	public static int getAltObject(int id) {
		return ItemProp.getAltObject(id);
	}

	public static final Comparator<Item> quantityComparator = (item1, item2) -> {
		return Integer.compare(item1.quantity, item2.quantity);
	};
}