package hitonoriol.madsand.entities;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Queue;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.fasterxml.jackson.annotation.JsonInclude;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.enums.NpcState;
import hitonoriol.madsand.enums.NpcType;
import hitonoriol.madsand.enums.TradeCategory;
import hitonoriol.madsand.map.ProductionStation;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.NpcContainer;
import hitonoriol.madsand.properties.NpcProp;
import hitonoriol.madsand.world.World;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Npc extends Entity {
	public static int NULL_NPC = 0;
	static double IDLE_NPC_MOVE_CHANCE = 27;

	public int id;
	public int rewardExp;
	public ArrayList<Integer> questList = new ArrayList<Integer>();

	public boolean canTrade = false;
	public boolean friendly;
	public boolean spawnOnce;
	private boolean pauseFlag = false;

	public float timePassed; // time passed since last action
	public float tickCharge = 0;

	public int attackDistance = 2; // Must be < than this
	public boolean enemySpotted = false;

	public NpcState state = NpcState.Idle;
	public NpcType type = NpcType.Regular;
	public TradeCategory tradeCategory;

	public ProductionStation animalProductWorker;

	private Queue<Direction> movementQueue = new ArrayDeque<>();

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
		stats.roll(properties.lvl);
		stats.skills.setLvl(properties.lvl);
		stats.dexterity = properties.dexterity;
		stats.calcStats();

		stats.name = properties.name;
		stats.hp = properties.hp;
		stats.mhp = stats.hp;
		stats.strength = properties.strength;
		stats.accuracy = properties.accuracy;
		rewardExp = properties.rewardExp;
		stats.faction = properties.faction;
		canTrade = properties.canTrade;

		if (properties.defaultState != null)
			state = properties.defaultState;

		initInventory();
		inventory.setMaxWeight(stats.calcMaxInventoryWeight());
		if (properties.loot != null)
			inventory.putItem(properties.loot.roll());

		if (properties.questList != null)
			questList = new ArrayList<>(properties.questList);

		friendly = properties.friendly;
		spawnOnce = properties.spawnOnce;
		type = properties.type;
		tradeCategory = properties.tradeCategory;
		initTrader();
		initFarmAnimal();
	}

	private void initFarmAnimal() {
		if (!type.equals(NpcType.FarmAnimal))
			return;

		animalProductWorker = new ProductionStation(-id);
	}

	private static int BASE_TRADER_COINS = 500;
	private static int TIER_COIN_MULTIPLIER = 300;

	private void initTrader() {
		if (!type.equals(NpcType.Trader) || tradeCategory == null)
			return;

		inventory.setMaxWeight(Integer.MAX_VALUE);

		int tier = NpcProp.tradeLists.rollTier();
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
		boolean outOfView = distanceTo(World.player) > World.player.fov;
		if (isStepping()) {
			if (!outOfView)
				movementQueue.add(dir);
			return false;
		}
		int originalX = this.x, originalY = this.y;

		coords.set(x, y).addDirection(dir);
		if (coords.x == World.player.x && coords.y == World.player.y)
			return false;

		if (!super.move(dir))
			return false;

		int newX = this.x, newY = this.y;
		setGridCoords(originalX, originalY);
		MadSand.world.getCurLoc().moveNpc(this, newX, newY); // ykno, let's assume if npc moves, it means that it's in the
																// same location as player, so... this should work always
																// despite how ugly this shit looks
		setGridCoords(newX, newY);

		if (outOfView)
			stepping = false;

		return true;
	}

	private void pollMovementQueue() {
		if (movementQueue.isEmpty())
			return;

		move(movementQueue.poll());
	}

	@Override
	public void stopMovement() {
		super.stopMovement();
		pollMovementQueue();
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
			else {
				MadSand.warn(stats.name + " deals " + atk + " damage to you");
				super.attackAnimation(player);
			}
			player.damage(atk);
			return true;
		}
	}

	public void die() {
		super.die();
		MadSand.world.delNpc(this);
	}

	boolean canAct(int ap) {
		return tickCharge >= getActionLength(ap);
	}

	boolean canAct() {
		return tickCharge > 0;
	}

	void skipAction() {
		tickCharge -= timePassed;
	}

	public int doAction(int ap) {
		tickCharge -= getActionLength(ap);
		return super.doAction(ap);
	}

	public void act(float time) {
		tickCharge += (timePassed = time);

		if (pauseFlag) {
			unPause();
			return;
		}

		if (!friendly)
			state = NpcState.FollowPlayer;

		act();
	}

	private void act() {
		if (!canAct()) {
			tickCharge = 0;
			return;
		}

		Player player = World.player;
		double dist = distanceTo(player);

		switch (state) {

		case Still:
			skipAction();
			return;

		case Idle:
			if (!canAct(stats.AP_WALK))
				return;

			if (Utils.percentRoll(IDLE_NPC_MOVE_CHANCE))
				randMove();

			doAction(stats.AP_WALK);
			break;

		case FollowPlayer:

			if (!enemySpotted && canSee(player))
				enemySpotted = true;

			if (enemySpotted && dist > fov * 1.5f)
				enemySpotted = false;

			if (!enemySpotted) {
				skipAction();
				return;
			}

			Direction dir;

			if (dist >= attackDistance) {

				if (!canAct(stats.AP_WALK))
					return;

				dir = Pair.getRelativeDirection(x, y, player.x, player.y, true);
				move(dir);
				doAction(stats.AP_WALK);
			} else {

				if (!canAct(stats.AP_ATTACK))
					return;

				dir = Pair.getRelativeDirection(x, y, player.x, player.y, false);
				turn(dir);
				attack(stats.look);
				doAction(stats.AP_ATTACK);
			}
			break;

		default:

			break;

		}
		act();
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

	public static Comparator<Npc> speedComparator = new Comparator<Npc>() {
		@Override
		public int compare(Npc o1, Npc o2) {
			return Double.compare(o2.getSpeed(), o1.getSpeed());
		}
	};

}