package ru.bernarder.fallenrisefromdust;

import ru.bernarder.fallenrisefromdust.properties.NpcProp;

public class Npc extends Player {
	int id, qid;
	String loot;

	public Npc(int id) {
		this.id = id;
		init();
	}

	void init() {
		stats.hp = NpcProp.hp.get(id);
		stats.mhp = stats.hp;
	}
}