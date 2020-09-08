package hitonoriol.madsand.entities;

import java.util.ArrayList;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.badlogic.gdx.graphics.g2d.Sprite;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.enums.NpcState;
import hitonoriol.madsand.enums.NpcType;
import hitonoriol.madsand.properties.NpcContainer;
import hitonoriol.madsand.properties.NpcProp;
import hitonoriol.madsand.world.World;

public class Npc extends Entity {
	public static int NULL_NPC = 0;
	public static int LOOT_RANDOM_FACTOR = 2;

	public int id;
	public int rewardExp;
	public ArrayList<Integer> questList = new ArrayList<Integer>();

	public boolean canTrade = false;
	public boolean friendly;
	public boolean spawnOnce;
	private boolean pauseFlag = false;

	public int attackDistance = 1;
	public boolean enemySpotted = false;

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
		id = NULL_NPC;
	}

	public void pause() {
		pauseFlag = true;
	}

	public void unPause() {
		pauseFlag = false;
	}

	void loadProperties() {
		NpcContainer properties = NpcProp.npcs.get(id);
		stats.roll();
		stats.name = properties.name;
		stats.hp = properties.hp;
		stats.mhp = stats.hp;
		stats.strength = properties.strength;
		stats.accuracy = properties.accuracy;
		rewardExp = properties.rewardExp;
		stats.faction = properties.faction;
		initInventory();
		inventory.setMaxWeight(stats.calcMaxInventoryWeight());
		inventory.putItem(properties.loot);

		int removeAmt;
		for (Item item : inventory.items) {
			removeAmt = Utils.rand(0, item.quantity / LOOT_RANDOM_FACTOR);
			inventory.delItem(item.id, removeAmt);
		}

		if (properties.questList != null)
			questList = new ArrayList<>(properties.questList);

		friendly = properties.friendly;
		spawnOnce = properties.spawnOnce;
		type = properties.type;
	}

	@Override
	public boolean move(Direction dir) { // just kill me
		super.turn(dir);
		if (isStepping())
			return false;
		int originalX = this.x, originalY = this.y;

		if (!super.move(dir))
			return false;

		int newX = this.x, newY = this.y;
		setGridCoords(originalX, originalY);
		MadSand.world.getCurLoc().moveNpc(this, newX, newY); // ykno, let's assume if npc moves, it means that it's in the
																// same location as player, so... this should work always
																// despite how ugly this shit looks
		setGridCoords(newX, newY);
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

			if (!enemySpotted && canSee(player))
				enemySpotted = true;

			if (enemySpotted && dist > fov)
				enemySpotted = false;

			if (!enemySpotted)
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

	public String spottedMsg() {
		if (enemySpotted)
			return "Looks like " + stats.name + " spotted you";
		else
			return stats.name + " doesn't see you";
	}

	public String getInfoString() {
		String ret = super.getInfoString();
		ret += "Friendly: " + (friendly ? "yes" : "no") + Gui.LINEBREAK;
		return ret;
	}

}