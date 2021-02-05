package hitonoriol.madsand.map.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import hitonoriol.madsand.DynamicallyCastable;
import hitonoriol.madsand.LuaUtils;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.Skill;
import hitonoriol.madsand.entities.inventory.item.Tool;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.MapEntity;
import hitonoriol.madsand.properties.ObjectProp;
import hitonoriol.madsand.properties.TileProp;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY)
@JsonSubTypes({ @Type(CraftingStation.class), @Type(ItemFactory.class), @Type(ResourceObject.class) })
public class MapObject extends MapEntity implements DynamicallyCastable<MapObject> {
	private static final int CLEANUP_FLAG = -1337;
	public static final int NULL_OBJECT_ID = 0;
	public static final int COLLISION_MASK_ID = 666;

	public int id;
	public String name;

	public int hp, maxHp;
	public int harvestHp;
	public int lvl;

	public boolean nocollide = false;
	public boolean isWall = false;
	public boolean centered = false;

	public int maskWidth = 0, maskHeight = 0; // Collision mask dimensions for objects larger than 1x1 cell

	public int dropOnDestruction;
	public String onInteract = Resources.emptyField;

	public MapObject(MapObject protoObject) {
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

	public MapObject copy() {
		return new MapObject(this);
	}

	public MapObject() {
		id = 0;
	}

	public void interactIfPossible(Runnable interaction) {
		if (MadSand.world.getCurLoc().editable)
			interaction.run();
		else
			MadSand.notice("You try to interact with " + name + "..." + Resources.LINEBREAK
					+ "But suddenly, you feel that it's protected by some mysterious force");
	}

	public void interact(Player player, Runnable interaction) {
		if (player.stats.isToolEquipped(Tool.Type.Hammer))
			interactIfPossible(() -> player.startResourceGathering(this));

		else if (!onInteract.equals(Resources.emptyField))
			LuaUtils.execute(onInteract);

		else
			interaction.run();
	}

	public void interact(Player player) {
		interact(player, () -> {
		});
	}

	@JsonIgnore
	public boolean isCollisionMask() {
		return (id == Map.COLLISION_MASK_ID);
	}

	void destroy() {
		this.id = 0; // cleaned up later in map
		this.hp = CLEANUP_FLAG;

		Pair coords = getPosition();
		if (dropOnDestruction != 0)
			MadSand.world.getCurLoc().putLoot(coords.x, coords.y, dropOnDestruction);
	}

	public boolean isDestroyed() {
		return hp == CLEANUP_FLAG;
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

		takeHarvestDamage(amt + 1);
	}

	protected int acceptHit(Player player, Supplier<Integer> dmgSupplier) {
		int damage = dmgSupplier.get();
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

	@Override
	public Pair getPosition() {
		return new Pair(MadSand.world.getCurLoc().locateObject(this));
	}

	@Override
	public void playDamageAnimation() {
		super.playAnimation(Resources.createAnimation(Resources.objectHitAnimStrip));
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

	@JsonIgnore
	public Skill getInteractionSkill() {
		return as(ResourceObject.class)
				.map(resource -> resource.skill)
				.orElse(Skill.None);
	}

	@JsonIgnore
	public float getRenderOffset() {
		return Resources.objects[id].getWidth() / 4;
	}

	public static MapObject create(int id) {
		return ObjectProp.getObject(id).copy();
	}
}
