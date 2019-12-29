package ru.bernarder.fallenrisefromdust.entities;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.fasterxml.jackson.annotation.JsonIgnore;

import ru.bernarder.fallenrisefromdust.Gui;
import ru.bernarder.fallenrisefromdust.MadSand;
import ru.bernarder.fallenrisefromdust.Resources;
import ru.bernarder.fallenrisefromdust.Utils;
import ru.bernarder.fallenrisefromdust.containers.Pair;
import ru.bernarder.fallenrisefromdust.entities.inventory.Item;
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
		stats.strength = NpcProp.atk.get(id);
		stats.accuracy = NpcProp.accuracy.get(id);
		stats.skills.setExp(Skill.Level, NpcProp.rewardexp.get(id));
		stats.faction = NpcProp.faction.get(id);
		initInventory();
		inventory.setMaxWeight(stats.calcMaxInventoryWeight());
		inventory.putItem(NpcProp.drop.get(id));

		int toRemove;
		for (Item item : inventory.items) {
			toRemove = Utils.rand(0, item.quantity);
			Utils.out("Removing " + toRemove + " of " + item.name);
			inventory.delItem(item.id, toRemove);
		}

		Utils.out(stats.name);
		inventory.dump();

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
			int atk = stats.calcAttack();
			if (atk == 0)
				MadSand.print(stats.name + " misses!");
			player.damage(atk);
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

	private String HEALTH_STATE_FULL = "full";
	private String HEALTH_STATE_75 = "couple of scratches";
	private String HEALTH_STATE_50 = "slightly damaged";
	private String HEALTH_STATE_25 = "severe injuries";
	private String HEALTH_STATE_10 = "at death's door";

	private float HEALTH_75 = 0.75f;
	private float HEALTH_50 = 0.5f;
	private float HEALTH_25 = 0.25f;
	private float HEALTH_10 = 0.1f;

	@JsonIgnore
	public String getHealthState() {
		float state = stats.hp / stats.mhp;
		if (state > HEALTH_75)
			return HEALTH_STATE_FULL;
		else if (state > HEALTH_50)
			return HEALTH_STATE_75;
		else if (state > HEALTH_25)
			return HEALTH_STATE_50;
		else if (state > HEALTH_10)
			return HEALTH_STATE_25;
		else
			return HEALTH_STATE_10;
	}

	@JsonIgnore
	public String getInfoString() {
		String ret = "";
		ret += "Faction: " + stats.faction + Gui.LINEBREAK;
		ret += "Health: " + getHealthState() + Gui.LINEBREAK;
		ret += "Friendly: " + (friendly ? "yes" : "no") + Gui.LINEBREAK;
		return ret;
	}

}