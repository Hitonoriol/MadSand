package hitonoriol.madsand.entities;

import java.util.ArrayList;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.badlogic.gdx.graphics.g2d.Sprite;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.enums.NpcState;
import hitonoriol.madsand.enums.NpcType;
import hitonoriol.madsand.enums.TradeCategory;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.NpcContainer;
import hitonoriol.madsand.properties.NpcProp;
import hitonoriol.madsand.world.World;

public class Npc extends Entity {
	public static int NULL_NPC = 0;
	public static int LOOT_RANDOM_FACTOR = 2;
	static double IDLE_NPC_MOVE_CHANCE = 30;

	public int id;
	public int rewardExp;
	public ArrayList<Integer> questList = new ArrayList<Integer>();

	public boolean canTrade = true;
	public boolean friendly;
	public boolean spawnOnce;
	private boolean pauseFlag = false;

	public int attackDistance = 1;
	public boolean enemySpotted = false;

	public NpcState state = NpcState.Idle;
	public NpcType type = NpcType.Regular;
	public TradeCategory tradeCategory;

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
		stats.skills.setLvl(properties.lvl);
		stats.dexterity = stats.AP_WALK;
		stats.name = properties.name;
		stats.hp = properties.hp;
		stats.mhp = stats.hp;
		stats.strength = properties.strength;
		stats.accuracy = properties.accuracy;
		rewardExp = properties.rewardExp;
		stats.faction = properties.faction;
		stats.calcStats();
		
		if (properties.defaultState != null)
			state = properties.defaultState;

		initInventory();
		inventory.setMaxWeight(stats.calcMaxInventoryWeight());
		if (properties.loot != null)
			inventory.putItem(properties.loot.roll());

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
		tradeCategory = properties.tradeCategory;
		initTrader();
	}

	private static int BASE_TRADER_COINS = 450;
	private static int TIER_COIN_MULTIPLIER = 100;

	private void initTrader() {
		if (!type.equals(NpcType.Trader) || tradeCategory == null)
			return;

		inventory.setMaxWeight(Integer.MAX_VALUE);

		int tier = stats.skills.getLvl();
		int currencyId = Globals.getInt(Globals.CURRENCY_FIELD);
		int maxCoins = BASE_TRADER_COINS + tier * TIER_COIN_MULTIPLIER;
		int quantity = Utils.rand(BASE_TRADER_COINS / 2, maxCoins);

		inventory.putItem(NpcProp.tradeLists.roll(tradeCategory, tier));
		inventory.putItem(new Item(currencyId, quantity));
		canTrade = true;
	}

	@Override
	public boolean move(Direction dir) {
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
			int atk = stats.calcAttack(player.getDefense());
			if (atk == 0)
				MadSand.print(stats.name + " misses!");
			else
				MadSand.print(stats.name + " deals " + atk + " damage to you");
			player.damage(atk);
			return true;
		}
	}

	public void die() {
		super.die();
		MadSand.world.delNpc(this);
	}

	public void act() {
		Player player = World.player;
		int ticksSpent = 0;
		tileDmg();
		//stats.perTickCheck(); I don't think NPCs need this

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
			if (canAct(stats.AP_WALK) && Utils.percentRoll(IDLE_NPC_MOVE_CHANCE)) {
				ticksSpent = doAction(stats.AP_WALK);
				randMove();
			} else
				rest();
			break;

		case FollowPlayer:
			int dist = distanceTo(player);

			if (!enemySpotted && canSee(player))
				enemySpotted = true;

			if (enemySpotted && dist > fov)
				enemySpotted = false;

			if (!enemySpotted)
				return;

			Direction dir = Pair.getRelativeDirection(x, y, player.x, player.y, true);

			if (dist > attackDistance) {
				if (canAct(stats.AP_WALK) && dir != null) {
					ticksSpent = doAction(stats.AP_WALK);
					Utils.out("Ticks spent walking: " + ticksSpent);
					move(dir);
				} else
					rest();
				return;
			}

			dir = Pair.getRelativeDirection(x, y, player.x, player.y, false);
			if (canAct(stats.AP_ATTACK)) {
				turn(dir);

				do {
					Utils.out("Ticks spent attacking: " + ticksSpent);
					attack(stats.look);
				} while ((ticksSpent = doAction(stats.AP_ATTACK)) < 1);

			} else
				rest();

			break;

		default:

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
		ret += "Friendly: " + (friendly ? "yes" : "no") + Resources.LINEBREAK;
		return ret;
	}

}