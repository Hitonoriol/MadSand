package hitonoriol.madsand.map;

import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.enums.ItemType;
import hitonoriol.madsand.enums.Skill;
import hitonoriol.madsand.properties.ObjectProp;
import hitonoriol.madsand.properties.TileProp;

public class MapObject {
	public static final int CLEANUP_FLAG = -1337;
	@JsonIgnore
	public static final int NULL_OBJECT_ID = 0;
	@JsonIgnore
	public static final int COLLISION_MASK_ID = 666;

	@JsonIgnore
	public int id;

	public int hp;
	public int harvestHp;
	public int lvl;

	public boolean nocollide = false;
	public boolean isProductionStation = false;
	public boolean isCraftingStation = false;
	public boolean isWall = false;
	public boolean centered = false;

	public int maskWidth = 0, maskHeight = 0; // Collision mask dimensions for objects larger than 1x1 cell
	public HashMap<ItemType, Vector<Integer>> altItems;
	public String onInteract;
	public Skill skill;
	public String name;

	public MapObject(int id) {
		this.id = id;
		MapObject objectProp = ObjectProp.getObject(id);
		this.name = objectProp.name;
		this.hp = objectProp.hp;
		this.harvestHp = objectProp.harvestHp;
		this.skill = objectProp.skill;
		this.lvl = objectProp.lvl;
		this.nocollide = objectProp.nocollide;
		this.centered = objectProp.centered;
		this.isProductionStation = objectProp.isProductionStation;
		this.isWall = objectProp.isWall;
		this.isCraftingStation = objectProp.isCraftingStation;
		maskHeight = objectProp.maskHeight;
		maskWidth = objectProp.maskWidth;

	}

	public MapObject() {
		this.id = 0;
	}

	@JsonIgnore
	public boolean isCollisionMask() {
		return (id == Map.COLLISION_MASK_ID);
	}

	void destroy() {
		this.id = 0; // cleaned up later in map
		this.hp = CLEANUP_FLAG;
	}

	private boolean verify() {
		if (this.hp > 0)
			return true;
		else {
			destroy();
			return false;
		}
	}

	public boolean takeDamage(int amt) {
		if (amt <= 0)
			amt = 1;
		boolean dmg = false;
		harvestHp -= amt;
		if (harvestHp < 0) {
			--this.hp;
			harvestHp = ObjectProp.getObject(id).harvestHp;
			dmg = true;
		}
		this.verify();
		return dmg;
	}

	boolean takeDamage() {
		return takeDamage(0);
	}

	private static int getAltItem(int id, ItemType hand, HashMap<ItemType, Vector<Integer>> container) {
		HashMap<ItemType, Vector<Integer>> items = container;
		if (items == null)
			return -1;
		if (!items.containsKey(hand))
			hand = ItemType.None;
		if (!items.containsKey(hand) || items.get(hand) == null)
			return -1;
		Vector<Integer> aitems = items.get(hand);
		return aitems.get(Utils.random.nextInt(aitems.size()));
	}

	public static int getAltItem(int id, ItemType hand) {
		return getAltItem(id, hand, ObjectProp.getObject(id).altItems);
	}

	public static int getTileAltItem(int id, ItemType hand) {
		return getAltItem(id, hand, TileProp.getTileProp(id).altItems);
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
	public float getRenderOffset() {
		return Resources.objects[id].getWidth() / 4;
	}
}
