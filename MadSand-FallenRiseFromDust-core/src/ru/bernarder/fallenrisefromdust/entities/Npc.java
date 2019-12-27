package ru.bernarder.fallenrisefromdust.entities;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.badlogic.gdx.graphics.g2d.Sprite;

import ru.bernarder.fallenrisefromdust.MadSand;
import ru.bernarder.fallenrisefromdust.Resources;
import ru.bernarder.fallenrisefromdust.Utils;
import ru.bernarder.fallenrisefromdust.containers.Pair;
import ru.bernarder.fallenrisefromdust.enums.Direction;
import ru.bernarder.fallenrisefromdust.enums.NpcState;
import ru.bernarder.fallenrisefromdust.enums.NpcType;
import ru.bernarder.fallenrisefromdust.enums.Skill;
import ru.bernarder.fallenrisefromdust.properties.NpcProp;
import ru.bernarder.fallenrisefromdust.world.World;

public class Npc extends Entity {
	public static int NULL_NPC = 0;

	public int id;
	public ArrayList<Integer> questList = new ArrayList<Integer>();

	public boolean friendly;
	public boolean spawnOnce;
	private boolean pauseFlag = false;

	public int attackDistance = 1;
	public boolean playerSpotted = false;

	public NpcState state = NpcState.Idle;
	public NpcType type = NpcType.Regular;

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

	public void pause() {
		pauseFlag = true;
	}
	
	public void unPause() {
		pauseFlag = false;
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
		inventory.putItem(NpcProp.drop.get(id));

		String list = NpcProp.qids.get(id);
		StringTokenizer qTok = new StringTokenizer(list, ",");
		while (qTok.hasMoreTokens())
			questList.add(Utils.val(qTok.nextToken()));

		friendly = NpcProp.friendly.get(id);
		spawnOnce = NpcProp.spawnonce.get(id);
		type = NpcProp.type.get(id);
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

	public void act() {
		Player player = World.player;
		int ticksSpent = 0;
		tileDmg();
		stats.perTickCheck();

		if (pauseFlag) {
			unPause();
			return;
		}

		if (!friendly)
			state = NpcState.FollowPlayer;

		Utils.out(stats.name + " acting. State: " + state);
		Utils.out("Player spotted: " + playerSpotted);

		switch (state) {
		case Still:
			break;

		case Idle:
			if (canAct(stats.AP_WALK) && Utils.random.nextBoolean()) {
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