package ru.bernarder.fallenrisefromdust;

import ru.bernarder.fallenrisefromdust.properties.ObjectProp;

public class MapObject {
	int id, hp;
	String name;

	public MapObject(int id) {
		this.id = id;
		this.name = ObjectProp.name.get(id);
		this.hp = ObjectProp.hp.get(id);
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

	MapObject takeDamage() {
		this.hp--;
		this.verify();
		return this;
	}

	static int getAltItem(int id) {
		Utils.out("Getting altitem for obj " + id);
		String items = ObjectProp.altitems.get(new Tuple<Integer, String>(id, "altitem"));
		if (items.indexOf(',') != -1) {
			String it[] = items.split("\\,");
			return Integer.parseInt(it[Utils.random.nextInt(it.length)]);
		} else
			return Integer.parseInt(items);
	}
}
