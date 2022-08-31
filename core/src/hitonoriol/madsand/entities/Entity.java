package hitonoriol.madsand.entities;

import static hitonoriol.madsand.resources.Resources.TILESIZE;
import static hitonoriol.madsand.screens.WorldRenderer.TARGET_FRAME_DELTA;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.Circle;
import hitonoriol.madsand.containers.Line;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.containers.PairFloat;
import hitonoriol.madsand.entities.inventory.Inventory;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.inventory.item.Projectile;
import hitonoriol.madsand.entities.movement.MeleeAttackMovement;
import hitonoriol.madsand.entities.movement.Movement;
import hitonoriol.madsand.entities.npc.AbstractNpc;
import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.gui.animation.Animations;
import hitonoriol.madsand.gui.animation.EntityAnimation;
import hitonoriol.madsand.gui.dialogs.LootDialog;
import hitonoriol.madsand.map.Loot;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.MapEntity;
import hitonoriol.madsand.map.object.MapObject;
import hitonoriol.madsand.pathfinding.Path;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.TileProp;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.Utils;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public abstract class Entity extends MapEntity {
	public static final long PLAYER_UID = 0, NULL_UID = -1;
	protected static final int DEFAULT_FOV = 15;

	private long uid;
	@JsonIgnore
	private Sprite[] sprites; // Same order as in Direction.baseValues

	public static final Comparator<Entity> speedComparator = (e1, e2) -> Double.compare(e2.getSpeed(), e1.getSpeed());

	public int x, y; // Grid coords
	public PairFloat screenPosition = new PairFloat(); // Screen space coords
	public float movementSpeed; // Visual movement speed in pixels per frame

	private float actDuration = 0; // Real time needed for this entity to finish all its actions
	private float actDelay = 0; // Real time seconds by which this entities action start will be delayed

	private int fov = DEFAULT_FOV;

	protected Pair coords = new Pair();

	public Inventory inventory;
	public Stats stats;

	private Queue<Movement> movementQueue = new ArrayDeque<>();

	protected boolean hasMoved = false;
	protected boolean running = false;
	private int targetedByEnemies = 0;

	public Entity(String name) {
		stats = isPlayer() ? new PlayerStats() : new Stats();
		stats.name = name;
		initStatActions();
		initInventory();
	}

	public Entity() {
		this("");
	}

	protected void setUid(long uid) {
		this.uid = uid;
	}

	public abstract void postLoadInit();

	public Stats stats() {
		return stats;
	}

	@JsonIgnore
	public boolean isPlayer() {
		return this instanceof Player;
	}

	public void setFov(int val) {
		fov = val;
	}

	public int getFov() {
		return fov;
	}

	@Override
	public void playAnimation(TextureRegion[] animation) {
		MadSand.getRenderer().queueAnimation(new EntityAnimation(this, animation));
	}

	void setSprites(Sprite[] sprites) {
		this.sprites = sprites;
	}

	@JsonIgnore
	void setSprites(Sprite r, Sprite u, Sprite l, Sprite d) {
		sprites = new Sprite[Direction.baseValues.length];
		sprites[Direction.RIGHT.baseOrdinal()] = r;
		sprites[Direction.UP.baseOrdinal()] = u;
		sprites[Direction.LEFT.baseOrdinal()] = l;
		sprites[Direction.DOWN.baseOrdinal()] = d;
	}

	@JsonIgnore
	protected void setSprites(Sprite s) {
		setSprites(s, s, s, s);
	}

	@JsonIgnore
	public TextureRegion getSprite() {
		return sprites[stats.look.baseOrdinal()];
	}

	/* Time required for the entity to move one tile in any direction */
	public float getMovementAnimationDuration() {
		return ((float) TILESIZE / getMovementSpeed()) * TARGET_FRAME_DELTA;
	}

	public float getMeleeAttackAnimationDuration() {
		return 2f * (((float) TILESIZE / (getMovementSpeed() * MeleeAttackMovement.SPEED_FACTOR))
				* TARGET_FRAME_DELTA);
	}

	protected void addActDuration(float actDuration) {
		this.actDuration += actDuration;
	}

	public float getActDuration() {
		return actDuration;
	}

	public void addActDelay(float actDelay) {
		this.actDelay += actDelay;
	}

	public void waitFor(Entity entity) {
		setActDelay(Math.max(actDelay, entity.getActDuration()));
	}

	public void setActDelay(float actDelay) {
		this.actDelay = actDelay;
	}

	public float getActDelay() {
		return actDelay;
	}

	public void prepareToAnimateAction() {}

	public void prepareToAct() {
		actDuration = actDelay = 0;
		hasMoved = false;
	}

	public boolean hasActDelay() {
		return actDelay > 0;
	}

	public void initStatActions() {
		stats.setOwner(this);
	}

	public boolean at(int x, int y) {
		return this.x == x && this.y == y;
	}

	public boolean at(Pair coords) {
		return at(coords.x, coords.y);
	}

	public Inventory initInventory() {
		inventory = new Inventory();
		return inventory;
	}

	@JsonIgnore
	public void setName(String name) {
		stats.name = name;
	}

	public boolean memberOf(Faction faction) {
		return stats.faction == faction;
	}

	public boolean canAfford(int cost) {
		return inventory.hasItem(Globals.values().currencyId, cost);
	}

	public long uid() {
		return uid;
	}

	public void unTarget() {
		--targetedByEnemies;
	}

	@JsonIgnore
	public boolean isTargeted() {
		return targetedByEnemies > 0;
	}

	public void target() {
		++targetedByEnemies;
	}

	public void forEachInFov(BiConsumer<Integer, Integer> action) {
		Circle.forEachPoint(x, y, fov, action);
	}

	protected void attack(MapEntity target, Damage damage) {
		target.acceptDamage(damage);
	}

	protected void meleeAttackAnimation(Direction dir, Runnable attackAction) {
		move(Movement.meleeAttack(this, dir, attackAction));
	}

	public abstract void meleeAttack(Direction dir);

	protected Pair rangedAttack(Pair targetPos, Projectile projectile) {
		Map map = MadSand.world().getCurLoc();
		Pair thisCoords = new Pair(x, y);
		Pair obstacleCoords = map.rayCast(thisCoords, targetPos);
		if (obstacleCoords.isEmpty())
			obstacleCoords.set(targetPos);

		addActDuration(Projectile.ANIMATION_DURATION);
		Damage damage = new Damage(this).ranged(projectile, distanceTo(obstacleCoords));
		projectile.launchProjectile(thisCoords.copy().toScreen(),
				obstacleCoords.copy().toScreen(),
				target -> attack(target, damage));
		MadSand.getRenderer().queuePath(Path.create(thisCoords, obstacleCoords), 0.675f, Color.RED);
		return obstacleCoords;
	}

	protected void dropOverflowingItem(Item item) {
		MadSand.world().getCurLoc().putLoot(x, y, item);
	}

	public boolean addItem(Item item) {
		boolean itemAdded = inventory.putItem(item);

		if (!itemAdded)
			dropOverflowingItem(item);

		return itemAdded;
	}

	public boolean addItem(String partialName, int quantity) {
		return addItem(Item.create(partialName, quantity));
	}

	public boolean addItem(String partialName) {
		return addItem(partialName, 1);
	}

	public void addItem(List<Item> items) {
		items.forEach(item -> addItem(item));
	}

	public boolean addItem(int id, int quantity) {
		Item overflowingItem = inventory.putItem(id, quantity);
		boolean itemAdded = overflowingItem.equals(Item.nullItem);

		if (!itemAdded)
			dropOverflowingItem(overflowingItem);

		return itemAdded;
	}

	public void delItem(Item item, int quantity) {
		inventory.delItem(item, quantity);
	}

	public void delItem(int id, int quantity) {
		inventory.delItem(id, quantity);
	}

	public boolean dropItem(Item item, int quantity) {
		if (!hasItem(item))
			return false;

		MadSand.world().getCurLoc().putLoot(x, y, item.copy().setQuantity(quantity));
		inventory.delItem(item, quantity);
		return true;
	}

	public boolean hasItem(int id) {
		return inventory.getIndex(id) != -1;
	}

	public boolean hasItem(Item item) {
		return hasItem(item.id());
	}

	public boolean pickUpLoot(Loot loot, Item item, int quantity) {
		boolean removeStack = quantity >= item.quantity;
		Item pickedUpItem = removeStack ? loot.remove(item) : Item.duplicate(item, quantity);

		if (!removeStack)
			item.quantity -= quantity;

		return addItem(pickedUpItem);
	}

	public boolean pickUpLoot(Loot loot, Item item) {
		return pickUpLoot(loot, item, item.quantity);
	}

	public void pickUpLoot(Loot loot) {
		for (int i = loot.getItemCount() - 1; i >= 0; --i)
			if (!pickUpLoot(loot, loot.get(i)))
				break;
	}

	public void pickUpLoot() {
		Loot loot = MadSand.world().getCurLoc().getLoot(x, y);

		if (loot.equals(Map.nullLoot))
			return;

		new LootDialog(loot).show();
	}

	protected static boolean isObstacle(Pair coords) {
		int nx = coords.x, ny = coords.y;
		Map loc = MadSand.world().getCurLoc();

		MapObject obj = loc.getObject(nx, ny);
		AbstractNpc npc = loc.getNpc(nx, ny);

		if (!npc.equals(Map.nullNpc))
			return true;

		return !(obj.isCollisionMask() || obj.nocollide || obj.equals(Map.nullObject));
	}
	
	public boolean colliding(Direction direction) {
		return isObstacle(new Pair(x, y).addDirection(direction));
	}

	@JsonIgnore
	public boolean isInBackground() {
		Map loc = MadSand.world().getCurLoc();
		return (loc.getObject(x, y).isCollisionMask() || loc.getTile(x, y).foreground);
	}

	public boolean standingOnLoot(int x, int y) {
		if (stats.dead)
			return false;
		if (MadSand.world().getCurLoc().getLoot(x, y).equals(Map.nullLoot))
			return false;
		else
			return true;
	}

	boolean standingOnLoot() {
		return standingOnLoot(x, y);
	}

	public void damage(float percent) {
		damage((int) (stats().mhp * percent));
	}

	@Override
	public void damage(int dmg) {
		if (dmg <= 0)
			return;

		playDamageAnimation();
		stats.hp -= dmg;
		stats.check();
	}

	@Override
	public void acceptDamage(Damage damage) {
		// TODO: Handle resists / weaknesses, etc
		super.acceptDamage(damage);
	}

	public void heal(int amt) {
		if (amt < 0) {
			damage(-amt);
			return;
		}

		if (stats.hp < stats.mhp)
			playAnimation(Animations.heal);

		stats.hp += amt;
		stats.check();

	}

	@JsonIgnore
	public int getDefense() {
		return stats.getDefense();
	}

	@JsonIgnore
	public abstract int getLvl();

	public void dropInventory() {
		Item item;
		Map curLoc = MadSand.world().getCurLoc();
		for (int i = inventory.items.size() - 1; i >= 0; --i) {
			item = inventory.items.get(i);
			curLoc.putLoot(x, y, item.copy());
			inventory.delItem(item);
		}
	}

	protected void die() {
		stats.dead = true;
		dropInventory();
	}

	protected float getActionLength(double ap) {
		return (float) (ap / getSpeed());
	}

	public int doAction(double ap) { // any action that uses AP \\ returns number of ticks spent
		double tmp = stats.actionPts;
		stats.actionPts -= ap;
		int ticks = 0;
		double absPts = Math.abs(stats.actionPts), absTmp = Math.abs(tmp);

		if (stats.actionPts <= 0) {
			ticks = (int) (absPts / stats.actionPtsMax);
			if (absPts < stats.actionPtsMax && stats.actionPts < 0)
				ticks = 1;

			if (absPts > stats.actionPtsMax)
				++ticks;

			if (absTmp < absPts)
				stats.actionPts = stats.actionPtsMax - absTmp;
			else
				stats.actionPts = (absPts % tmp);

			if (absPts > stats.actionPtsMax)
				stats.actionPts = stats.actionPtsMax - stats.actionPts;
		}

		if (stats.actionPts == 0) {
			stats.actionPts = stats.actionPtsMax;
			++ticks;
		}

		return ticks;
	}

	public void updCoords() {
		Map map = MadSand.world().getCurLoc();
		if (x >= map.getWidth())
			x = map.getWidth() - 1;
		if (y >= map.getHeight())
			y = map.getHeight() - 1;
		if (x < 0)
			x = 0;
		if (y < 0)
			y = 0;
		screenPosition.x = (x * Resources.TILESIZE);
		screenPosition.y = (y * Resources.TILESIZE);
	}

	public void teleport(int x, int y) {
		if (!MadSand.world().getCurLoc().isFreeTile(x, y))
			return;

		setGridCoords(x, y);
		updCoords();
	}

	public void teleport(Pair coords) {
		teleport(coords.x, coords.y);
	}

	public void setGridCoords(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public boolean isOnMapBound(Direction dir) {
		Map map = MadSand.world().getCurLoc();
		boolean ret = false;
		ret |= x >= map.getWidth() - 1 && (dir == Direction.RIGHT);
		ret |= y >= map.getHeight() - 1 && (dir == Direction.UP);
		ret |= x < 1 && (dir == Direction.LEFT);
		ret |= y < 1 && (dir == Direction.DOWN);
		return ret;
	}

	/* Movement animation speed (pixels per sec) */
	protected float getMovementSpeed() {
		float speed = getSpeed();
		return speed / 17f + (float) Math.pow(Math.sqrt(speed), 1.225f) * 0.985f;
	}

	public float calcMovementSpeed() {
		return movementSpeed = getMovementSpeed();
	}

	public float calcMeleeMovementSpeed() {
		return getMovementSpeed() * MeleeAttackMovement.SPEED_FACTOR;
	}

	public void speedUp(float by) {
		movementSpeed *= by;
		running = true;
	}

	public void stopRunning() {
		if (!running)
			return;

		calcMovementSpeed();
		running = false;
	}

	@JsonIgnore
	public PairFloat getScreenPosition() {
		return screenPosition;
	}

	public float worldX() {
		return screenPosition.x;
	}

	public float worldY() {
		return screenPosition.y;
	}

	public boolean hasMoved() {
		return hasMoved;
	}

	public boolean isMoving() {
		return !movementQueue.isEmpty();
	}
	
	public boolean hasQueuedMovement() {
		return movementQueue.size() > 1;
	}

	public void queueMovement(Direction dir) {
		movementQueue.add(Movement.walk(this));
	}

	public void queueMovement(Movement movement) {
		movementQueue.add(movement);
	}

	protected void ifMoving(Consumer<Movement> action) {
		if (isMoving())
			action.accept(currentMovement());
	}

	protected Movement currentMovement() {
		return movementQueue.peek();
	}
	
	protected Queue<Movement> getMovementQueue() {
		return movementQueue;
	}

	protected void nextMovement() {
		movementQueue.poll();
		ifMoving(movement -> {
			if (movement.isInvalid(this))
				nextMovement();
		});
	}

	public void stopMovement() {
		ifMoving(movement -> {
			if (movement.applyChanges())
				updCoords();
			nextMovement();
		});
		if (!isMoving()) {
			stopRunning();
			updCoords();
		}
	}

	public void animateMovement() {
		ifMoving(movement -> {
			if (!movement.update(screenPosition))
				stopMovement();
		});
	}

	public final boolean move(Movement movement) {
		Direction dir = movement.direction();
		if (isDead())
			return false;

		if (dir.isDiagonal() && !movement.isDiagonalAllowed())
			return false;

		if (isMoving()) {
			queueMovement(movement);
			return true;
		}

		if (!movement.ignoringObstacles())
			if (colliding(dir))
				return false;

		if (isOnMapBound(dir))
			return false;

		Pair coords = new Pair(x, y).addDirection(dir);
		if (movement.applyChanges())
			setPosition(coords);
		queueMovement(movement);
		return true;
	}

	public void move(Path path) {
		path.forEachDirection(direction -> queueMovement(direction));
	}

	public boolean isDead() {
		return stats.dead;
	}

	public int tileDmg() {
		int tid = MadSand.world().getTileId(x, y);
		int dmg = TileProp.getTileProp(tid).damage;
		if (dmg > 0)
			damage(dmg);
		return tid;
	}

	public void turn(Direction dir) {
		if (isMoving())
			return;

		look(dir);
	}
	
	protected void look(Direction dir) {
		stats.look = dir;
	}

	boolean canWalk(Direction dir) {
		if (dir.isDiagonal())
			return false;

		if (isMoving())
			return false;

		stats.look = dir;
		turn(stats.look);

		if (colliding(stats.look) || isOnMapBound(stats.look))
			return false;

		return true;
	}

	public boolean walk(Direction dir) {
		if (!canWalk(dir))
			return false;

		move(new Movement(stats.look, movementSpeed));
		return true;
	}

	public abstract void act(float time);

	public Direction getRelativeDirection(int x, int y, boolean fourWay) {
		return Pair.getRelativeDirection(this.x, this.y, x, y, fourWay);
	}

	public int distanceTo(int x, int y) {
		return (int) Line.calcDistance(this.x, this.y, x, y);
	}

	public int distanceTo(Pair coords) {
		return distanceTo(coords.x, coords.y);
	}

	public int distanceTo(Entity entity) {
		return distanceTo(entity.x, entity.y);
	}

	public boolean isInsideFov(Entity entity) {
		return distanceTo(entity) <= fov;
	}

	public Pair getPosition() {
		return new Pair(x, y);
	}

	public void setPosition(Pair position) {
		x = position.x;
		y = position.y;
	}

	public boolean rayCast(Entity entity, Predicate<MapEntity> obstaclePredicate) {
		MutableBoolean result = new MutableBoolean(true);
		Map map = MadSand.world().getCurLoc();
		Pair coords = new Pair();

		Line.rayCast(x, y, entity.x, entity.y, (x, y) -> {
			if (this.at(x, y))
				return true;

			result.setValue(obstaclePredicate.test(map.getMapEntity(coords.set(x, y))));
			return result.booleanValue();
		});

		return result.booleanValue();
	}

	// true if <obstacle> is not a solid object, or doesn't exist
	private static Predicate<MapEntity> canSeePredicate = obstacle -> {
		if (obstacle.isEmpty())
			return true;

		return obstacle.as(MapObject.class).map(object -> object.nocollide).orElse(true);
	};

	public int getEffectiveFov() {
		return fov;
	}

	public boolean canSee(Entity entity) {
		if (entity == this)
			return true;

		return distanceTo(entity) <= getEffectiveFov() && rayCast(entity, canSeePredicate);
	}

	public boolean canShoot(Entity entity) {
		return rayCast(entity, obstacle -> {
			if (!obstacle.isEmpty() && obstacle.is(AbstractNpc.class)) {
				Utils.out(getName() + " can't shoot: " + obstacle.getName() + " is in the way!");
				return false;
			}

			return canSeePredicate.test(obstacle);
		});
	}

	@Override
	public void playDamageAnimation() {
		playAnimation(Animations.attack);
	}

	@JsonIgnore
	public float getSpeed() {
		stats.calcSpeed();
		return (float) stats.actionPtsMax;
	}

	@JsonIgnore
	public float getSpriteWidth() {
		return getSprite().getRegionWidth();
	}

	@JsonIgnore
	public String getHealthState(int percent) {
		if (percent > 75)
			return "full";

		else if (percent > 50)
			return "couple of scratches";

		else if (percent > 25)
			return "slight injuries";

		else if (percent > 10)
			return "severe injuries";

		else
			return "at death's door";
	}

	public String getHealthState() {
		return getHealthState(stats.getHealthPercent());
	}

	public String getName() {
		return stats.name;
	}

	@JsonIgnore
	public String getInfoString() {
		int hpPercent = stats.getHealthPercent();
		String ret;
		ret = String.format("Health: %s (%d%%)", getHealthState(hpPercent), hpPercent);
		return ret;
	}

	public String toString() {
		return String.format("{%s uid: %d} %s (%d, %d) Lvl. %d [HP: %d/%d]",
				getClass().getSimpleName(),
				uid,
				getName(),
				x, y,
				getLvl(),
				stats.hp, stats.mhp);
	}
	
	protected final String debugName() {
		return String.format("%s (%d)", getName(), uid());
	}

	@Override
	public boolean isEmpty() {
		return this == Map.nullNpc;
	}
}
