package hitonoriol.madsand.map.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.Damage;
import hitonoriol.madsand.entities.Entity;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.PlayerStats;
import hitonoriol.madsand.entities.inventory.ItemUI;
import hitonoriol.madsand.entities.inventory.item.PlaceableItem;
import hitonoriol.madsand.entities.inventory.item.Tool;
import hitonoriol.madsand.entities.skill.Skill;
import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.gfx.TextureProcessor;
import hitonoriol.madsand.gui.animation.Animations;
import hitonoriol.madsand.lua.Lua;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.MapEntity;
import hitonoriol.madsand.map.Placeable;
import hitonoriol.madsand.properties.ObjectProp;
import hitonoriol.madsand.properties.TileProp;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.Utils;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY)
@JsonSubTypes({ @Type(CraftingStation.class), @Type(ItemFactory.class), @Type(ResourceObject.class),
		@Type(ItemPipeline.class), @Type(Waypoint.class) })
public class MapObject extends MapEntity implements Placeable {
	public static final int NULL_OBJECT_ID = 0;
	public static final int COLLISION_MASK_ID = 666;

	protected int id;
	public String name;

	public int hp = 1, maxHp = 1;
	public int harvestHp = 1;
	public int lvl;

	public boolean nocollide = false;
	public boolean isWall = false;
	public boolean centered = false;

	public int maskWidth = 0, maskHeight = 0; // Collision mask dimensions for objects larger than 1x1 cell

	private int dropOnDestruction;
	protected Direction directionFacing = Direction.RIGHT;
	public String onInteract = Resources.emptyField;

	public MapObject(MapObject protoObject) {
		setLuminosity(protoObject.getLuminosity());
		initProperties(protoObject);
	}

	public MapObject copy() {
		return new MapObject(this);
	}

	public MapObject() {
		id = 0;
	}

	public void initProperties(MapObject protoObject) {
		id = protoObject.id;
		name = protoObject.name;
		harvestHp = protoObject.harvestHp;
		lvl = protoObject.lvl;
		hp = maxHp = protoObject.hp;
		onInteract = protoObject.onInteract;

		nocollide = protoObject.nocollide;
		centered = protoObject.centered;
		isWall = protoObject.isWall;

		maskHeight = protoObject.maskHeight;
		maskWidth = protoObject.maskWidth;
	}

	@Override
	public int id() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String name() {
		return getName();
	}

	@Override
	public boolean add(Map map, Pair coords) {
		return map.add(coords, this);
	}

	private void interactIfPossible(Runnable interaction) {
		if (MadSand.world().getCurLoc().editable)
			interaction.run();
		else
			MadSand.notice("You try to interact with " + name + "..." + Resources.LINEBREAK
					+ "But suddenly, you feel that it's protected by some mysterious force");
	}

	protected void interact(Player player, Runnable interaction) {
		if (player.stats().isToolEquipped(Tool.Type.Hammer))
			interactIfPossible(() -> player.startResourceGathering(this));

		else if (!onInteract.equals(Resources.emptyField))
			Lua.execute(onInteract, this);

		else
			interactIfPossible(() -> interaction.run());
	}

	public void interact(Player player) {
		interact(player, () -> {});
	}

	@JsonIgnore
	public boolean isCollisionMask() {
		return (id == Map.COLLISION_MASK_ID);
	}

	public int getDropOnDestruction() {
		return dropOnDestruction;
	}

	public void setDropOnDestruction(int dropOnDestruction) {
		this.dropOnDestruction = dropOnDestruction;
	}

	public void destroy() {
		this.id = 0; // cleaned up later in map

		Pair coords = getPosition();
		if (dropOnDestruction != 0)
			MadSand.world().getCurLoc().putLoot(coords.x, coords.y, dropOnDestruction);
	}

	public boolean isDestroyed() {
		return id == 0;
	}

	public boolean isTransparent() {
		return nocollide || isDestroyed();
	}

	private boolean verify() {
		if (this.hp > 0)
			return true;
		else {
			destroy();
			return false;
		}
	}

	public boolean takeHarvestDamage(int amt) {
		if (amt <= 0)
			amt = 1;
		boolean dmg = false;
		harvestHp -= amt;
		if (harvestHp < 0) {
			--hp;
			int excDmg = Math.abs(harvestHp) - 1;
			harvestHp = ObjectProp.getObject(id).harvestHp;
			if (excDmg > 0)
				takeHarvestDamage(excDmg);
			dmg = true;
		}
		verify();
		return dmg;
	}

	@Override
	public void damage(int amt) {
		if (amt == 0)
			return;

		playDamageAnimation();
		takeHarvestDamage(amt + 1);
	}

	@Override
	public void acceptDamage(Damage damage) {
		super.acceptDamage(damage);

		Entity dealer = damage.getDealer();
		if (dealer != MadSand.player()) {
		if (!damage.missed())
			MadSand.print(dealer.getName() + " hits " + name + " dealing " + damage.getValueString() + " damage");
		}
		else {
			if (damage.missed())
				MadSand.print("You hit " + name + " but deal no damage");
			else
				MadSand.print("You deal " + damage.getValueString() + " damage to " + name);
		}

		if (isDestroyed())
			MadSand.print(name + " shatters into pieces!");
	}

	/* Used when precalculating hits during the resource gathering */
	protected final int simulateHit(Player player, Skill skill) {
		PlayerStats stats = player.stats();
		return stats.skills.getBaseSkillDamage(skill) + stats.getEquippedToolDamage(skill);
	}

	public int simulateHit(Player player) {
		return simulateHit(player, Skill.None);
	}

	public int acceptHit(Player player, Supplier<Integer> dmgSupplier) {
		int damage = dmgSupplier.get();
		if (damage < 0)
			return -1;

		int maxHp = ObjectProp.getObject(id).harvestHp;
		if (!takeHarvestDamage(damage))
			MadSand.print("You hit " + name + " [ " + harvestHp + " / " + maxHp + " ]");
		else
			MadSand.print("You damaged " + name);
		return damage;
	}

	// returns amount of damage done to object's harvestHp or negative value on fail
	public int acceptHit(Player player) {
		return acceptHit(player, () -> player.stats.getEquippedToolDamage(Skill.None));
	}

	public void takeFullDamage() {
		takeHarvestDamage(harvestHp + 1);
	}

	public double getHpPercent() {
		return ((double) hp / (double) maxHp) * 100d;
	}

	public Direction getDirection() {
		return directionFacing;
	}

	public void setDirection(Direction direction) {
		directionFacing = direction;
	}

	@Override
	public Pair getPosition() {
		return new Pair(MadSand.world().getCurLoc().locateObject(this));
	}

	@Override
	public void playDamageAnimation() {
		super.playAnimation(Animations.objectHit);
	}

	@JsonIgnore
	public String getBuildInfo() {
		return "";
	}

	protected static int rollResource(int id, Tool.Type heldTool, HashMap<Tool.Type, ArrayList<Integer>> items) {
		if (items == null)
			return -1;
		if (!items.containsKey(heldTool))
			heldTool = Tool.Type.None;
		if (!items.containsKey(heldTool) || items.get(heldTool) == null)
			return -1;

		return Utils.randElement(items.get(heldTool));
	}

	public static int rollTileResource(int id, Tool.Type heldTool) {
		return rollResource(id, heldTool, TileProp.getTileProp(id).altItems);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MapObject))
			return false;
		if (obj == this)
			return true;

		MapObject rhs = (MapObject) obj;
		return new EqualsBuilder().append(id, rhs.id).isEquals();
	}

	public String getName() {
		return name;
	}

	@JsonIgnore
	public Skill getInteractionSkill() {
		return as(ResourceObject.class)
				.map(resource -> resource.skill)
				.orElse(Skill.None);
	}

	@JsonIgnore
	public float getRenderOffset() {
		return getTexture().getRegionWidth() / 4;
	}

	@JsonIgnore
	public TextureRegion getTexture() {
		return Resources.getObject(id);
	}

	@Override
	public boolean isEmpty() {
		return this == Map.nullObject;
	}

	public static MapObject create(int id) {
		return ObjectProp.getObject(id).copy();
	}

	@Override
	public void createPlaceable(PlaceableItem item) {
		createPlaceable(item, () -> {
			item.name = getName();
			item.setType(PlaceableItem.Type.Object);
			setDropOnDestruction(item.id());
		});
	}

	@Override
	public TextureRegion createPlaceableTexture() {
		return new TextureRegion(new Texture(
				TextureProcessor.extractPixmap(getTexture(), ItemUI.SIZE)));
	}

	@Override
	public String toString() {
		return String.format("{%s} %s [%d/%d]",
				getClass().getSimpleName(),
				getName(),
				hp, maxHp);
	}
}
