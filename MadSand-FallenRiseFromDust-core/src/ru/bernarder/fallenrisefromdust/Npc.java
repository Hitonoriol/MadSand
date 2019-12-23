package ru.bernarder.fallenrisefromdust;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.badlogic.gdx.graphics.g2d.Sprite;

import ru.bernarder.fallenrisefromdust.enums.Direction;
import ru.bernarder.fallenrisefromdust.enums.NpcState;
import ru.bernarder.fallenrisefromdust.enums.Skill;
import ru.bernarder.fallenrisefromdust.properties.NpcProp;

public class Npc extends Entity {
	public static int NULL_NPC = 0;

	public int id;
	public String questList;

	public boolean friendly;
	public boolean spawnOnce;

	public int attackDistance = 1;
	public boolean playerSpotted = false;

	public NpcState state = NpcState.Idle;

	public Npc(int id) {
		super();
		this.id = id;
		loadProperties();
		if (id != NULL_NPC)
			loadSprite();
	}
	
	public void loadSprite() {
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

	boolean attack(Direction dir) {
		Pair coords = new Pair(x, y).addDirection(dir);
		Player player = World.player;
		if (!(player.x == coords.x && player.y == coords.y))
			return false;
		else {
			player.damage(super.attack());
			return true;
		}
	}

	void act() {
		Player player = World.player;
		int ticksSpent = 0;
		tileDmg();
		stats.perTickCheck();

		if (!friendly)
			state = NpcState.FollowPlayer;

		Utils.out(stats.name + " acting. State: " + state);
		Utils.out("Player spotted: " + playerSpotted);

		switch (state) {
		case Still:
			break;

		case Idle:
			if (canAct(stats.AP_WALK)) {
				ticksSpent = doAction(stats.AP_WALK);
				randMove();
			} else
				rest();
			break;

		case FollowPlayer:
			int dist = distanceTo(player);
			Utils.out("Distance to player: " + dist);

			if (dist <= fov)
				playerSpotted = true;

			if (!playerSpotted)
				return;

			int dx = player.x - x;
			int dy = player.y - y;
			Direction dir = null;

			if (dx > 0)
				dir = Direction.RIGHT;
			else if (dx < 0)
				dir = Direction.LEFT;

			if (dy > 0)
				dir = Direction.UP;
			else if (dy < 0)
				dir = Direction.DOWN;

			if (dist > attackDistance) {
				if (canAct(stats.AP_WALK) && dir != null) {
					ticksSpent = doAction(stats.AP_WALK);
					Utils.out("Ticks spent walking: " + ticksSpent);
					move(dir);
				} else
					rest();
				return;
			}

			if (stats.actionPts >= stats.AP_ATTACK) {
				turn(dir);
				ticksSpent = doAction(stats.AP_ATTACK);
				Utils.out("Ticks spent attacking: " + ticksSpent);
				attack(stats.look);
			} else
				rest();

			break;

		}
	}

}