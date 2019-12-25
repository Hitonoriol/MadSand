package ru.bernarder.fallenrisefromdust.map;

import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.lang3.builder.EqualsBuilder;

import ru.bernarder.fallenrisefromdust.Utils;
import ru.bernarder.fallenrisefromdust.enums.Skill;
import ru.bernarder.fallenrisefromdust.properties.ObjectProp;
import ru.bernarder.fallenrisefromdust.properties.TileProp;

public class MapObject {
	public static final int NULL_OBJECT_ID = 0;
	public static final int COLLISION_MASK_ID = 666;
	
	public int id;
	public int hp;
	public int harverstHp;
	public int lvl;
	public boolean nocollide = false;
	public Skill skill;
	public String name;

	public MapObject(int id) {
		this.id = id;
		this.name = ObjectProp.name.get(id);
		this.hp = ObjectProp.hp.getOrDefault(id, 1);
		this.harverstHp = ObjectProp.harvestHp.get(id);
		this.skill = ObjectProp.skill.getOrDefault(id, Skill.None);
		this.lvl = ObjectProp.minLvl.get(id);
		this.nocollide = (ObjectProp.nocollide.get(id) != -1);
	}

	public MapObject() {
		this(0);
	}

	public boolean isCollisionMask() {
		return (id == Map.COLLISION_MASK_ID);
	}

	void destroy() {
		this.id = 0; // cleaned up later in map
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
		harverstHp -= amt;
		if (harverstHp <= 0) {
			--this.hp;
			harverstHp = ObjectProp.harvestHp.get(id);
			dmg = true;
		}
		this.verify();
		return dmg;
	}

	boolean takeDamage() {
		return takeDamage(0);
	}

	private static int getAltItem(int id, int hand, HashMap<Integer, HashMap<Integer, Vector<Integer>>> container) {
		HashMap<Integer, Vector<Integer>> items = container.get(id);
		if (!items.containsKey(hand))
			hand = 0;
		if (!items.containsKey(hand) || items.get(hand).equals(null))
			return -1;
		Vector<Integer> aitems = items.get(hand);
		return aitems.get(Utils.random.nextInt(aitems.size()));
	}

	public static int getAltItem(int id, int hand) {
		return getAltItem(id, hand, ObjectProp.altitems);
	}

	public static int getTileAltItem(int id, int hand) {
		return getAltItem(id, hand, TileProp.altitems);
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
}
