package ru.bernarder.fallenrisefromdust;

import com.badlogic.gdx.graphics.g2d.Sprite;

import ru.bernarder.fallenrisefromdust.enums.Direction;
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

	@Override
	public boolean move(Direction dir) { // just kill me
		int ox = this.x, oy = this.y;
		if (!super.move(dir))
			return false;
		int nx = x, ny = y;
		teleport(ox, oy);
		MadSand.world.getCurLoc().moveNpc(this, nx, ny);
		teleport(nx, ny);
		return true;
	}
	
}