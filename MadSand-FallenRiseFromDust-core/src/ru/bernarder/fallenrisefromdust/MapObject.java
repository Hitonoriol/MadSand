package ru.bernarder.fallenrisefromdust;

import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.lang3.builder.EqualsBuilder;

import ru.bernarder.fallenrisefromdust.properties.ObjectProp;

public class MapObject {
	int id, hp, harverstHp;
	String name;

	public MapObject(int id) {
		this.id = id;
		this.name = ObjectProp.name.get(id);
		this.hp = ObjectProp.hp.get(id);
		this.harverstHp = ObjectProp.harvestHp.get(id);
	}

	public MapObject() {
		this(0);
	}

	void destroy() {
		this.id = 0;
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

	static int getAltItem(int id, int hand) {
		Utils.out("Getting altitem for obj " + id + "hand: " + hand);
		HashMap<Integer, Vector<Integer>> items = ObjectProp.altitems.get(new Tuple<Integer, String>(id, "altitem"));
		if (!items.containsKey(hand))
			hand = 0;
		if (!items.containsKey(hand) || items.get(hand).equals(null))
			return -1;
		Vector<Integer> aitems = items.get(hand);
		return aitems.get(Utils.random.nextInt(aitems.size()));
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
