package ru.bernarder.fallenrisefromdust;

import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.lang3.builder.EqualsBuilder;

import ru.bernarder.fallenrisefromdust.enums.Skill;
import ru.bernarder.fallenrisefromdust.properties.ObjectProp;
import ru.bernarder.fallenrisefromdust.properties.TileProp;

public class MapObject {
	int id, hp, harverstHp;
	Skill skill;
	String name;

	public MapObject(int id) {
		this.id = id;
		this.name = ObjectProp.name.get(id);
		this.hp = ObjectProp.hp.getOrDefault(id, 1);
		this.harverstHp = ObjectProp.harvestHp.get(id);
		this.skill = ObjectProp.skill.getOrDefault(id, Skill.None);
	}

	public MapObject() {
		this(0);
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

	boolean takeDamage() {
		boolean dmg = false;
		if (--harverstHp <= 0) {
			this.hp--;
			harverstHp = ObjectProp.harvestHp.get(id);
			dmg = true;
		}
		this.verify();
		return dmg;
	}

	private static int getAltItem(int id, int hand, HashMap<Integer, HashMap<Integer, Vector<Integer>>> container) {
		Utils.out("Getting altitem for obj " + id + "hand: " + hand);
		HashMap<Integer, Vector<Integer>> items = container.get(id);
		if (!items.containsKey(hand))
			hand = 0;
		if (!items.containsKey(hand) || items.get(hand).equals(null))
			return -1;
		Vector<Integer> aitems = items.get(hand);
		return aitems.get(Utils.random.nextInt(aitems.size()));
	}

	static int getAltItem(int id, int hand) {
		return getAltItem(id, hand, ObjectProp.altitems);
	}

	static int getTileAltItem(int id, int hand) {
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
