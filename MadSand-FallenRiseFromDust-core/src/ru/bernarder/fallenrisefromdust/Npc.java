package ru.bernarder.fallenrisefromdust;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.badlogic.gdx.graphics.g2d.Sprite;

import ru.bernarder.fallenrisefromdust.enums.Direction;
import ru.bernarder.fallenrisefromdust.enums.Skill;
import ru.bernarder.fallenrisefromdust.properties.NpcProp;

public class Npc extends Entity {
	public static int NULL_NPC = 0;
	public int id;
	public String questList;
	public boolean friendly;
	public boolean spawnOnce;

	public Npc(int id) {
		super();
		this.id = id;
		loadProperties();
		if (id != NULL_NPC)
			setSprites(new Sprite(Resources.npc[id]));
	}

	public Npc(int id, int x, int y) {
		this(id);
		teleport(x, y);
	}

	public Npc() {
		this(NULL_NPC);
	}

	void loadProperties() {
		stats.roll();
		stats.name = NpcProp.name.get(id);
		stats.hp = NpcProp.hp.get(id);
		stats.mhp = stats.hp;
		stats.str = NpcProp.atk.get(id);
		stats.accur = NpcProp.accuracy.get(id);
		stats.skills.setExp(Skill.Level, NpcProp.rewardexp.get(id));
		stats.faction = NpcProp.faction.get(id);
		initInventory();
		inventory.setMaxWeight(stats.str * 10);
		Loot.addLootQ(NpcProp.drop.get(id), inventory);
		questList = NpcProp.qids.get(id);
		friendly = NpcProp.friendly.get(id);
		spawnOnce = NpcProp.spawnonce.get(id);
	}

	@Override
	public boolean move(Direction dir) { // just kill me
		super.turn(dir);
		if (isStepping())
			return false;
		int ox = this.x, oy = this.y;
		if (!super.move(dir))
			return false;
		int nx = x, ny = y;
		setGridCoords(ox, oy);
		MadSand.world.getCurLoc().moveNpc(this, nx, ny); // ykno, let's assume if npc moves, it means that it's in the
															// same location as player, so... this should work always
															// despite how ugly this shit looks
		setGridCoords(nx, ny);
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Npc))
			return false;
		if (obj == this)
			return true;

		Npc rhs = (Npc) obj;
		return new EqualsBuilder().append(id, rhs.id).isEquals();
	}

}