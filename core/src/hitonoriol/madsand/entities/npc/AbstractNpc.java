package hitonoriol.madsand.entities.npc;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import hitonoriol.madsand.GameSaver;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.containers.Storage;
import hitonoriol.madsand.dialog.TextSubstitutor;
import hitonoriol.madsand.entities.Damage;
import hitonoriol.madsand.entities.Entity;
import hitonoriol.madsand.entities.Faction;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.Reputation;
import hitonoriol.madsand.entities.Stat;
import hitonoriol.madsand.entities.Stats;
import hitonoriol.madsand.entities.inventory.item.Projectile;
import hitonoriol.madsand.entities.movement.Movement;
import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.gui.animation.Animations;
import hitonoriol.madsand.input.Keyboard;
import hitonoriol.madsand.input.Mouse;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.pathfinding.NodePair;
import hitonoriol.madsand.pathfinding.Path;
import hitonoriol.madsand.properties.NpcContainer;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.TimeUtils;
import hitonoriol.madsand.util.Utils;
import me.xdrop.jrand.JRand;
import me.xdrop.jrand.generators.basics.FloatGenerator;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY)
@JsonSubTypes({ @Type(Npc.class), @Type(Trader.class), @Type(QuestMaster.class), @Type(FarmAnimal.class) })
public abstract class AbstractNpc extends Entity {
	public static int NULL_NPC = 0;
	static double IDLE_MOVE_CHANCE = 15;
	static float MAX_FOLLOW_FACTOR = 1.65f;
	private final static int MELEE_DISTANCE = 2; // Must be < than this
	private final static int MAX_LIFETIME = 20;
	private final static FloatGenerator lifetimeGen = JRand.flt().range(0.65f, 7.5f);

	private static final float ACT_DURATION_DRIFT = 0.012575f;
	private static FloatGenerator randomActionDelay = JRand.flt().range(Float.MIN_NORMAL, ACT_DURATION_DRIFT);

	public int id;
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
	private float tickCharge = 0;
	private float prevTickCharge;
	private Pair futurePosition = new Pair();

	private Entity enemy;
	public State state = State.Idle;

	@JsonIgnore
	private Runnable onActionFinish;

	@JsonIgnore
	private Path path = new Path();
	@JsonIgnore
	private Pair prevDestination = new Pair();
	private int pathIdx = 0;

	public AbstractNpc(NpcContainer protoNpc) {
		id = protoNpc.id();
		setUid(MadSand.world().nextEntityUID());
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

	protected final NodePair findPath(int x, int y) {
		Map map = MadSand.world().getCurLoc();
		NodePair nextPair = new NodePair();
		Utils.dbg("Pathfinding to %d, %d...", x, y);
		if (!prevDestination.equals(x, y) || pathIdx + 1 >= path.getCount()) {
			Utils.dbg("Recalculating path...");
			path.clear();
			pathIdx = 0;
			prevDestination.set(x, y);
			if (!map.getPathfindingEngine().searchPath(futurePosition.x, futurePosition.y, x, y, path)) {
				Utils.dbg("Fail: Unable to build a path");
				return nextPair;
			}
		}

		if (path.nodes.size == 0) {
			Utils.dbg("Fail: Calculated path is empty");
			return nextPair;
		}

		nextPair.set(path.get(pathIdx), path.get(pathIdx + 1));
		Utils.dbg("Success! Returning the next node pair in path: (%d, %d) -> (%d, %d) [%s]",
				nextPair.l.x, nextPair.l.y, nextPair.r.x, nextPair.r.y, nextPair.relativeDirection().name());
		++pathIdx;
		return nextPair;
	}

	private void walkTo(int x, int y) {
		if (!canAct(stats.walkCost))
			return;

		Utils.tryTo(() -> {
			NodePair nextNodePair = findPath(x, y);
			if (!nextNodePair.isEmpty()) {
				if (!walk(nextNodePair.relativeDirection())) {
					Utils.dbg("Oopsie, can't move in calculated direction, moving randomly");
					randMove();
				}
			}
			doAction(stats.walkCost);
		});
	}

	public void pause() {
		pauseFlag = true;
	}

	public void unPause() {
		pauseFlag = false;
	}

	private int MAX_LVL_GAP = 4;

	private float DEX_PER_LVL = 0.225f;
	private float HP_PER_LVL = 4.25f;
	private float STR_PER_LVL = 1.55f;
	private float ACC_PER_LVL = 1.4f;
	private float EXP_PER_LVL = 3.4f;

	// Action cost penalties
	private float MELEE_SPD_PER_LVL = 0.05f, RANGED_SPD_PER_LVL = 0.5f;
	private float MOVE_SPD_PER_LVL = 0.075f;

	private String NAMED_NPC_STR = " the ";
	private int CAN_GIVE_QUESTS_CHANCE = 15;

	void loadProperties(NpcContainer properties) {
		int maxLvl = MadSand.player().getLvl() + MAX_LVL_GAP;
		int lvl = Utils.rand(properties.lvl, maxLvl);

		stats.randomize(lvl);
		this.lvl = lvl;
		lvl -= properties.lvl; // Stats start increasing if lvl is > than npc's default level
		stats.set(Stat.Dexterity, (int) (properties.dexterity + lvl * DEX_PER_LVL));
		stats.calcStats();
		stats.restore();

		stats.name = TextSubstitutor.replace(properties.name);
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
					.forEach(id -> inventory.putItem(id, (int) (Utils.rand(10, 30) * Math.sqrt(this.lvl))));

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

	public void provoke(Entity enemy) {
		if (this.enemy == enemy)
			return;

		provoked = true;

		/* Have a chance based on intelligence to lose sight of the current enemy
		 *  and target attacker when provoked -- higher intelligence = smaller chance to get distracted */
		if (!stats().roll(Stat.Intelligence)) {
			loseSightOfEnemy();
			targetEnemy(enemy);
		}
	}

	public abstract void interact(Player player);

	@Override
	public void queueMovement(Movement movement) {
		if (!MadSand.player().isInsideFov(this))
			movement.apply(screenPosition);
		else {
			addActDuration(movement.getDuration());
			super.queueMovement(movement);
			if (!isNeutral())
				Utils.dbg("%s is queueing movement (%s): %f",
						debugName(),
						movement.getClass().getSimpleName(),
						getMovementAnimationDuration());
		}
	}

	protected int futureDistanceTo(Entity entity) {
		return entity.distanceTo(futurePosition);
	}

	protected Direction futureRelativeDirection(Entity entity) {
		return Pair.getRelativeDirection(futurePosition.x, futurePosition.y, entity.x, entity.y, false);
	}

	protected boolean planToWalk(Direction direction) {
		Pair tmpPosition = futurePosition.copy().addDirection(direction);
		if (!isObstacle(tmpPosition) && !MadSand.player().at(tmpPosition)) {
			futurePosition.set(tmpPosition);
			return true;
		}
		return false;
	}

	protected void undoPlannedMove(Direction direction) {
		futurePosition.addDirection(direction);
	}

	private Movement createWalkMovement(Direction direction) {
		return Movement.walk(this, direction)
				.applyChanges(false)
				.onMovementFinish(movement -> {
					Pair newPosition = getPosition().addDirection(movement.direction());
					MadSand.world().getCurLoc().moveNpc(this, newPosition.x, newPosition.y);
					updCoords();
					if (!hasQueuedMovement() && !futurePosition.equals(newPosition))
						Utils.dbg("%s expected to be at (%s), but ended up at (%s)",
								debugName(), futurePosition, newPosition);
				});
	}

	@Override
	public boolean colliding(Direction direction) {
		return super.colliding(direction) || MadSand.player().at(coords.set(x, y).addDirection(direction));
	}

	@Override
	public boolean walk(Direction dir) {
		super.turn(dir);
		if (!planToWalk(dir))
			return false;

		if (!super.move(createWalkMovement(dir))) {
			undoPlannedMove(dir);
			return false;
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
		return new HashCodeBuilder(18899, 63839).append(uid()).toHashCode();
	}

	@Override
	public void meleeAttack(Direction dir) {
		meleeAttackAnimation(dir, () -> {
			Pair coords = getPosition().copy().addDirection(dir);
			Player player = MadSand.player();
			Entity target = player.at(coords) ? player : MadSand.world().getCurLoc().getNpc(coords);
			Damage damage = new Damage(this).melee(target.getDefense());
			if (!target.isEmpty())
				attack(target, damage);
		});
	}

	@Override
	public void acceptDamage(Damage damage) {
		Player player = MadSand.player();
		boolean attackedByPlayer = damage.dealtBy(player);

		provoke(damage.getDealer());
		if (attackedByPlayer) {
			if (damage.missed())
				MadSand.print("You miss " + getName());
			else {
				MadSand.print("You deal " + damage.getValueString() + " damage to " + getName());
				player.damageHeldEquipment();
			}
		}

		super.acceptDamage(damage);

		if (attackedByPlayer && isDead()) {
			Mouse.refreshTooltip();
			MadSand.notice("You kill %s! [+%d exp]",
					getName(),
					(int) player.addExp(rewardExp * Math.sqrt(getLvl() * 0.05)));
			player.getReputation().change(stats().faction, Reputation.KILL_PENALTY);

			if (player.addToKillCount(id)) // If killed for the first time
				MadSand.print("You now know more about " + getName());

			Map map = MadSand.world().getCurLoc();
			if (!map.editable && map.getHostileNpcCount() == 0 && MadSand.world().isUnderGround()) {
				MadSand.print("The curse of the dungeon has been lifted!" + Resources.LINEBREAK
						+ "You can now break objects on this floor of the dungeon.");
				map.editable = true;
			}
		}
	}

	public void die() {
		super.die();

		if (enemySpotted())
			loseSightOfEnemy();

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

	@Override
	protected void addActDuration(float actDuration) {
		super.addActDuration(actDuration);
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

		Utils.dbg("Adding %f days (%f ticks) of lifetime for %s", days, daysToTicks(days), debugName());
		lifetime += daysToTicks(days);
	}

	protected void addLifetime() {
		addLifetime(Math.sqrt((float) lvl * 0.1f + 0.15f) * 0.1f);
	}

	public boolean canBeDespawned() {
		return lifetime <= 0;
	}

	protected void despawnProcess() {}

	/* The farther player gets, the faster NPCs lifetime decreases */
	private float liveDistanceCoef() {
		float maxDst = MadSand.player().getEffectiveFov();
		float dst = distanceTo(MadSand.player());

		return Math.max(0.0125f, (dst / maxDst) * 0.33f);
	}

	protected void live(float time) {
		lifetime -= time * liveDistanceCoef();
	}

	void randMove() {
		walk(Direction.random());
	}

	public boolean enemySpotted() {
		return enemy != null;
	}

	protected boolean enemyIsSlower() {
		return enemySpotted() && enemy.getSpeed() < getSpeed();
	}

	void targetEnemy(Entity enemy) {
		Utils.out("[Aggro] %s is targeting %s", debugName(), enemy);
		this.enemy = enemy;
		enemy.target();
		playAnimation(Animations.detect);
	}

	void loseSightOfEnemy() {
		if (enemy == null)
			return;

		Utils.out("[Aggro] %s lost sight of %s", debugName(), enemy);

		enemy.unTarget();
		this.enemy = null;
	}

	@JsonSetter
	private void setEnemy(long uid) {
		if (uid == Entity.NULL_UID)
			enemy = null;
		else
			GameSaver.postLoadAction(() -> enemy = MadSand.world().getCurLoc().getNpc(uid));
	}

	@JsonGetter
	private long getEnemy() {
		return enemy != null ? enemy.uid() : Entity.NULL_UID;
	}

	public boolean isTargeting(Entity entity) {
		return enemy == entity;
	}

	private void getCloserTo(Entity entity) {
		walkTo(entity.x, entity.y);
	}

	private void actMeleeAttack(Entity enemy) {
		if (!enemySpotted())
			return;

		if (futureDistanceTo(enemy) >= MELEE_DISTANCE)
			getCloserTo(enemy);
		else {
			if (!canAct(stats.meleeAttackCost))
				return;

			Direction enemyDirection = futureRelativeDirection(enemy);
			look(enemyDirection);
			meleeAttack(enemyDirection);
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
		int dst = futureDistanceTo(enemy);
		Projectile projectile = inventory.getItem(Projectile.class).get();
		if (dst > MAX_DST)
			getCloserTo(enemy);

		else if (dst > OPTIMAL_DST) {
			if (!performRangedAttack(enemy, projectile))
				getCloserTo(enemy);
		}

		else if (dst <= OPTIMAL_DST && dst >= MELEE_DISTANCE)
			performRangedAttack(enemy, projectile);

		else
			actMeleeAttack(enemy);
	}

	private void stateLogic() {
		boolean badRep = MadSand.player().getReputation().isHostile(stats.faction);
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
	}

	@Override
	public final void act(float time) {
		if (stats.dead) {
			skipAction();
			return;
		}

		tickCharge += (timePassed = time);
		live(time);

		if (pauseFlag) {
			unPause();
			return;
		}

		prevTickCharge = -1;
		stateLogic();
		act();

		if (!isNeutral() && enemySpotted())
			Utils.dbg("%s (aggroed at %s) will act for %f secs / wait for %f secs",
					debugName(), enemy.getName(), getActDuration(), getActDelay());

		if (enemyIsSlower()) {
			Utils.dbg("%s's enemy (%s) is slower, so their action will be delayed by %f seconds",
					debugName(), enemy.getName(), getActDuration());
			enemy.waitFor(this);
		}

		TimeUtils.scheduleTask(() -> finishActing(), getActDuration());
	}

	private void act() {
		if (!canAct()) {
			tickCharge = 0;
			return;
		}

		if (tickCharge == prevTickCharge)
			return;
		else
			prevTickCharge = tickCharge;

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
			if (!enemySpotted()) {
				Entity enemy = findTarget();
				if (!enemy.isEmpty())
					targetEnemy(enemy);
				else {
					if (Utils.percentRoll(IDLE_MOVE_CHANCE)) {
						randMove();
						break;
					} else {
						skipAction();
						return;
					}
				}
			}

			if (enemySpotted() && futureDistanceTo(enemy) > getFov() * MAX_FOLLOW_FACTOR) {
				loseSightOfEnemy();
				break;
			}

			if (canPerformRangedAttack())
				actRangedAttack(enemy);
			else
				actMeleeAttack(enemy);
			break;

		default:
			break;
		}
		act();
	}

	private Entity findTarget() {
		Map map = MadSand.world().getCurLoc();
		Storage<Entity> potentialTarget = new Storage<>(canSee(MadSand.player()) ? MadSand.player() : Map.nullNpc);

		/* Have a chance based on intelligence to skip looking for other victims if player is already in FOV */
		if (!potentialTarget.get().isEmpty() && stats().roll(Stat.Intelligence))
			return potentialTarget.get();

		forEachInFov((x, y) -> {
			AbstractNpc npc = map.getNpc(x, y);
			if (npc.isEmpty() || !canSee(npc))
				return;

			if (npc.stats().faction == stats().faction)
				return;

			if (distanceTo(npc) < distanceTo(potentialTarget.get()))
				potentialTarget.set(npc);
		});

		return potentialTarget.get();
	}

	@Override
	public void prepareToAct() {
		super.prepareToAct();
		futurePosition.set(x, y);
		float delay = (float) (randomActionDelay.gen() * (Stats.max().calcSpeed() - stats().calcSpeed()));
		addActDelay(delay);

		Player player = MadSand.player();
		if (player.canSee(this) && !isNeutral() && isTargeting(player) && getSpeed() > player.getSpeed()) {
			Keyboard.ignoreInput();
			setOnActionFinish(() -> Keyboard.resumeInput());
		}
	}

	@Override
	public void prepareToAnimateAction() {
		if (enemyIsSlower())
			setActDelay(0);
	}

	private void finishActing() {
		if (onActionFinish != null) {
			onActionFinish.run();
			onActionFinish = null;
		}
	}

	private void setOnActionFinish(Runnable action) {
		onActionFinish = action;
	}

	public String interactButtonString() {
		return "Interact with ";
	}

	private String spottedMsg() {
		if (enemySpotted() && enemy == MadSand.player())
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