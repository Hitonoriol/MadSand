package ru.bernarder.fallenrisefromdust;

import com.badlogic.gdx.graphics.g2d.Sprite;

import ru.bernarder.fallenrisefromdust.properties.NpcProp;

public class Npc extends Entity {
	int id;

	public Npc(int id) {
		super();
		this.id = id;
		loadProperties();
		setSprites(new Sprite(Resources.npc[id]));
	}

	void loadProperties() {
		stats.roll();
		stats.hp = NpcProp.hp.get(id);
		stats.mhp = stats.hp;
	}
}