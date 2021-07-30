package hitonoriol.madsand.entities.npc;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.dialog.GameTextSubstitutor;
import hitonoriol.madsand.entities.Entity;
import hitonoriol.madsand.entities.Faction;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.Stat;
import hitonoriol.madsand.entities.inventory.item.Projectile;
import hitonoriol.madsand.entities.skill.Skill;
import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.MapEntity;
import hitonoriol.madsand.pathfinding.Node;
import hitonoriol.madsand.pathfinding.Path;
import hitonoriol.madsand.properties.NpcContainer;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.Utils;
import me.xdrop.jrand.JRand;
import me.xdrop.jrand.generators.basics.FloatGenerator;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY)
@JsonSubTypes({ @Type(Npc.class), @Type(Trader.class), @Type(QuestMaster.class), @Type(FarmAnimal.class) })
public abstract class AbstractNpc extends Entity {
	public static int NULL_NPC = 0;
	static double IDLE_MOVE_CHANCE = 15;
	static float MAX_FOV_COEF = 1.5f;
	public static final float HOSTILE_SPEEDUP = 1.35f;
	private final static int meleeAttackDst = 2; // Must be < than this
	private final static int MAX_LIFETIME = 20;
	private final static FloatGenerator lifetimeGen = JRand.flt().range(0.35f, 15);

	public int id;
	public long uid;
	public int lvl = 1;
	public int rewardExp;
	@JsonProperty
	protected double lifetime;

	public boolean friendly;
	public boolean spawnOnce;
	public boolean provoked = false;
	private boolean pauseFlag = false;

	public boolean canGiveQuests = false;

	private float timePassed; // time passed since last action
	public float tickCharge = 0;

	public boolean enemySpotted = false;
	public State state = State.Idle;

	@JsonIgnore
	Path path = new Path();
	@JsonIgnore
	Pair prevDestination = new Pair();
	int pathIdx = 0;

	public AbstractNpc(NpcContainer protoNpc) {
		id = protoNpc.id;
		uid = MadSand.world().npcCounter().getAndIncrement();
		stats.spawnTime = MadSand.world().currentTick();
		stats.spawnRealTime = MadSand.world().currentActionTick();
		loadProperties(protoNpc);
		if (id != NULL_NPC)
			loadSprite();
		setLifetime(lifetimeGen.gen());
	}

	public AbstractNpc() {
		id = NULL_NPC;
	}

	public void loadSprite() {
		setSprites(new Sprite(Resources.getNpc(id)));
	}

	@Override
	public void postLoadInit() {
		loadSprite();
		initStatActions();
	}

	@Override
	public boolean add(Map map, Pair coords) {
		return map.add(coords, this);
	}

	public Node findPath(int x, int y) {
		Map map = MadSand.world().getCurLoc();

		if (!prevDestination.equals(x, y)) {
			path.clear();
			pathIdx = 0;
			prevDestination.set(x, y);
			if (!map.getPathfindingEngine().searchPath(this.x, this.y, x, y, path))
				return null;
		} else if (pathIdx + 1 >= path.getCount())
			return null;

		if (path.nodes.size == 0)
			return null;

		return path.get(++pathIdx);
	}

	public void pause() {
		pauseFlag = true;
	}

	public void unPause() {
		pauseFlag = false;
	}

	private int MAX_LVL_GAP = 3;

	private float DEX_PER_LVL = 0.125f;
	private float HP_PER_LVL = 3.5f;
	private float STR_PER_LVL = 0.25f;
	private float ACC_PER_LVL = 0.4f;
	private float EXP_PER_LVL = 3.4f;

	// Action cost penalties
	private float MELEE_SPD_PER_LVL = 0.1f, RANGED_SPD_PER_LVL = 0.5f;
	private float MOVE_SPD_PER_LVL = 0.15f;

	private String NAMED_NPC_STR = " the ";
	private int CAN_GIVE_QUESTS_CHANCE = 30;

	void loadProperties(NpcContainer properties) {
		int maxLvl = MadSand.player().getLvl() + MAX_LVL_GAP;
		int lvl = Utils.rand(properties.lvl, maxLvl);

		stats.roll(lvl);
		this.lvl = lvl;
		lvl -= properties.lvl; // Stats start increasing if lvl is > than npc's default level
		stats.set(Stat.Dexterity, (int) (properties.dexterity + lvl * DEX_PER_LVL));
		stats.calcStats();
		stats.restore();

		stats.name = GameTextSubstitutor.replace(properties.name);
		stats.hp = (int) (properties.hp + lvl * HP_PER_LVL);
		stats.mhp = stats.hp;
		stats.set(Stat.Strength, (int) (properties.strength + lvl * STR_PER_LVL));
		stats.set(Stat.Accuracy, (int) (properties.accuracy + lvl * ACC_PER_LVL));

		stats.meleeAttackCost += (float) lvl * MELEE_SPD_PER_LVL;
		stats.rangedAttackCost += (float) lvl * RANGED_SPD_PER_LVL;
		stats.walkCost += (float) lvl * MOVE_SPD_PER_LVL;

		rewardExp = properties.rewardExp + (int) (lvl * EXP_PER_LVL);

		if (lvl == maxLvl && !stats.name.contains(NAMED_NPC_STR))
			stats.name = Utils.randWord() + NAMED_NPC_STR + stats.name;

		stats.faction = properties.faction;

		if (properties.defaultState != null)
			state = properties.defaultState;

		initInventory();
		inventory.setMaxWeight(stats.calcMaxInventoryWeight());
		if (properties.loot != null)
			inventory.putItem(properties.loot.rollItems());

		if (properties.projectiles != null)
			properties.projectiles.stream()
					.forEach(id -> inventory.putItem(id, (int) (Utils.rand(10, 17) * Math.sqrt(this.lvl))));

		friendly = properties.friendly;
		spawnOnce = properties.spawnOnce;
	}

	protected void rollQuestGiveAbility() {
		canGiveQuests = Utils.percentRoll(CAN_GIVE_QUESTS_CHANCE);
	}

	@JsonIgnore
	public int getLvl() {
		return lvl;
	}

	public boolean isNeutral() {
		if (state == State.Hostile || provoked)
			return false;

		return friendly || state != State.Hostile;
	}

	public void provoke() {
		if (!provoked)
			provoked = true;
	}

	public abstract void interact(Player player);

	@Override
	public boolean move(Direction dir) {
		super.turn(dir);
		Entity player = MadSand.player();
		boolean outOfView = distanceTo(player) > player.getFov();
		if (!outOfView)
			outOfView |= !player.canSee(this) || MadSand.world().timeSkipInProgress();

		if (isMoving() && !outOfView) {
			queueMovement(dir);
			return false;
		}
		int originalX = this.x, originalY = this.y;

		coords.set(x, y).addDirection(dir);
		if (coords.x == player.x && coords.y == player.y)
			return false;

		if (!super.move(dir))
			return false;

		int newX = this.x, newY = this.y;
		setGridCoords(originalX, originalY);
		MadSand.world().getCurLoc().moveNpc(this, newX, newY);
		setGridCoords(newX, newY);

		if (outOfView) {
			stopMovement();
			updCoords();
		}

		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AbstractNpc))
			return false;
		if (obj == this)
			return true;

		AbstractNpc rhs = (AbstractNpc) obj;
		return new EqualsBuilder().append(id, rhs.id).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(18899, 63839).append(uid).toHashCode();
	}

	protected void attack(Player target, int dmg) {
		if (dmg == 0) {
			MadSand.print(stats.name + " misses!");
			target.increaseSkill(Skill.Evasion);
		} else
			MadSand.warn(stats.name + " deals " + dmg + " damage to you");
	}

	@Override
	protected void attack(MapEntity target, int dmg) {
		super.attack(target, dmg);

		if (target instanceof Player)
			attack((Player) target, dmg);

		else if (dmg > 0)
			MadSand.print(stats.name + " hits " + target.getName() + " dealing " + dmg + " damage");
	}

	@Override
	public void meleeAttack(Direction dir) {
		Pair coords = new Pair(x, y).addDirection(dir);
		Player player = MadSand.player();
		if (player.stats.dead)
			return;
		if (!(player.x == coords.x && player.y == coords.y))
			return;
		else {
			int atk = stats.calcMeleeAttack(player.getDefense());
			attack((MapEntity) player, atk);
		}
	}

	public void die() {
		super.die();

		if (enemySpotted)
			loseSightOf(MadSand.player());

		MadSand.world().delNpc(this);
	}

	boolean canAct(double ap) {
		return tickCharge >= getActionLength(ap);
	}

	boolean canAct() {
		return tickCharge > 0;
	}

	private void skipAction() {
		tickCharge -= timePassed;
	}

	public int doAction(double ap) {
		if (canBeDespawned())
			despawnProcess();
		tickCharge -= getActionLength(ap);
		return super.doAction(ap);
	}

	private double daysToTicks(double days) {
		return MadSand.world().ticksPerHour() * Utils.H_DAY * days;
	}

	@JsonIgnore
	public final void setLifetime(double days) {
		lifetime = daysToTicks(days);
	}

	public double getLifetime() {
		return lifetime;
	}

	protected final void addLifetime(double days) {
		if (lifetime < 0)
			lifetime = 0;
		if (lifetime > daysToTicks(MAX_LIFETIME))
			return;

		Utils.dbg("Adding %f days (%f ticks) of lifetime for %s", days, daysToTicks(days), getName());
		lifetime += daysToTicks(days);
	}

	protected void addLifetime() {
		addLifetime(Math.sqrt(lvl + 1) * (0.25f + Utils.random.nextFloat()));
	}

	public boolean canBeDespawned() {
		return lifetime <= 0;
	}

	protected void despawnProcess() {}

	protected void live(float time) {
		lifetime -= time;
	}

	void randMove() {
		move(Direction.random());
	}

	void detectEnemy(Entity enemy) {
		enemySpotted = true;

		if (enemy instanceof Player)
			((Player) enemy).target();

		playAnimation(Resources.createAnimation(Resources.detectAnimStrip));
	}

	void loseSightOf(Entity enemy) {
		enemySpotted = false;

		if (enemy instanceof Player)
			((Player) enemy).unTarget();
	}

	private void getCloserTo(Entity entity) {
		if (!canAct(stats.walkCost))
			return;

		Utils.tryTo(() -> {
			Node closestNode = findPath(entity.x, entity.y);
			if (closestNode != null)
				move(getRelativeDirection(closestNode.x, closestNode.y, true));
			doAction(stats.walkCost);
		});
	}

	private void actMeleeAttack(Entity enemy) {
		if (distanceTo(enemy) >= meleeAttackDst)
			getCloserTo(enemy);
		else {
			if (!canAct(stats.meleeAttackCost))
				return;

			turn(getRelativeDirection(enemy.x, enemy.y, false));
			meleeAttack(stats.look);
			doAction(stats.meleeAttackCost);
		}
	}

	private boolean canPerformRangedAttack() {
		return inventory.hasItem(Projectile.class);
	}

	private boolean performRangedAttack(Entity enemy, Projectile projectile) {
		if (!canShoot(enemy))
			return false;

		if (canAct(stats.rangedAttackCost)) {
			rangedAttack(enemy.getPosition(), projectile);
			doAction(stats.rangedAttackCost);
			return true;
		} else
			return false;
	}

	private int MAX_DST = 7, OPTIMAL_DST = 3;

	private void actRangedAttack(Entity enemy) {
		int dst = distanceTo(enemy);
		Projectile projectile = inventory.getItem(Projectile.class).get();
		if (dst > MAX_DST)
			getCloserTo(enemy);

		else if (dst > OPTIMAL_DST) {
			if (!performRangedAttack(enemy, projectile))
				getCloserTo(enemy);
		}

		else if (dst <= OPTIMAL_DST && dst >= meleeAttackDst)
			performRangedAttack(enemy, projectile);

		else
			actMeleeAttack(enemy);

	}

	@Override
	public final void act(float time) {
		boolean badRep = MadSand.player().getReputation().isHostile(stats.faction);
		tickCharge += (timePassed = time);
		live(time);

		if (pauseFlag) {
			unPause();
			return;
		}

		if (stats.faction == Faction.None)
			badRep = false;

		if ((!friendly || provoked) && state != State.Hostile)
			state = State.Hostile;

		if (friendly) { // Reputation affects only Neutral(friendly) NPCs
			if (badRep && state != State.Hostile)
				state = State.Hostile;

			if (!badRep && state == State.Hostile && !provoked)
				state = State.Idle;
		}

		prevTickCharge = -1;
		act();
	}

	float prevTickCharge;

	private void act() {
		if (!canAct()) {
			tickCharge = 0;
			return;
		}

		if (tickCharge == prevTickCharge)
			return;
		else
			prevTickCharge = tickCharge;

		Player player = MadSand.player();
		double dist = distanceTo(player);

		switch (state) {
		case Still:
			skipAction();
			return;

		case Idle:
			if (!canAct(stats.walkCost))
				return;

			if (Utils.percentRoll(IDLE_MOVE_CHANCE))
				randMove();

			doAction(stats.walkCost);
			break;

		case Hostile:
			if (!enemySpotted && canSee(player))
				detectEnemy(player);

			if (enemySpotted && dist > getFov() * MAX_FOV_COEF)
				loseSightOf(player);

			if (!enemySpotted) {
				skipAction();
				return;
			}

			if (canPerformRangedAttack())
				actRangedAttack(player);
			else
				actMeleeAttack(player);
			break;

		default:
			break;
		}
		act();
	}

	public String interactButtonString() {
		return "Interact with ";
	}

	private String spottedMsg() {
		if (enemySpotted)
			return "Looks like " + stats.name + " spotted you";
		else
			return stats.name + " doesn't see you";
	}

	public String getInfoString() {
		String info = super.getInfoString() + Resources.LINEBREAK;

		if (MadSand.player().knowsNpc(id))
			info += "Faction: " + stats.faction + Resources.LINEBREAK;

		if (canGiveQuests && isNeutral())
			info += "* Might need some help" + Resources.LINEBREAK;

		if (state == State.Hostile)
			info += spottedMsg();

		return info;
	}

	public boolean isEmpty() {
		return this == Map.nullNpc;
	}

	public static enum State {
		Still, Idle, Hostile
	}
}