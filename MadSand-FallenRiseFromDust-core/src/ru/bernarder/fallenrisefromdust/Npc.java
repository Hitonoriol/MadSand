package ru.bernarder.fallenrisefromdust;

import ru.bernarder.fallenrisefromdust.properties.NpcProp;

public class Npc extends Player {
	int id, qid;

	public Npc(int id) {
		isMain = false;
		this.id = id;
		init();
	}

	void init() {
		stats.hp = NpcProp.hp.get(id);
		stats.mhp = stats.hp;
	}
}