package hitonoriol.madsand.entities;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.function.Predicate;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.containers.Line;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.containers.PairFloat;
import hitonoriol.madsand.entities.inventory.Inventory;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.inventory.item.Projectile;
import hitonoriol.madsand.entities.npc.AbstractNpc;
import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.gui.dialogs.LootDialog;
import hitonoriol.madsand.map.Loot;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.MapEntity;
import hitonoriol.madsand.map.object.MapObject;
import hitonoriol.madsand.pathfinding.Path;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.TileProp;
import hitonoriol.madsand.util.Utils;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public abstract class Entity extends MapEntity {
	@JsonIgnore
	private Sprite upSpr, downSpr, leftSpr, rightSpr;

	public static final Comparator<Entity> speedComparator = (e1, e2) -> Double.compare(e2.getSpeed(), e1.getSpeed());

	@JsonIgnore
	private Sprite sprite;
	private float spriteWidth;

	public int x, y; // Grid coords
	public PairFloat globalPos = new PairFloat(); // Screen space coords
	public float movementSpeed;
	private float actDelay = 0;

	public int fov = 15;
	public int maxFov, minFov;

	protected Pair coords = new Pair();

	public Inventory inventory;
	public Stats stats;

	protected Queue<Direction> movementQueue = new ArrayDeque<>();

	@JsonIgnore
	public float stepy = MadSand.TILESIZE;
	@JsonIgnore
	public float stepx = MadSand.TILESIZE;

	protected boolean moving = false, hasMoved = false;
	protected boolean running = false;

	public Entity(String name) {
		stats = isPlayer() ? new PlayerStats() : new Stats();
		stats.name = name;
		initStatActions();
		initInventory();
	}

	public Entity() {
		this("");
	}

	public abstract void postLoadInit();

	public Stats stats() {
		return stats;
	}

	@JsonIgnore
	public boolean isPlayer() {
		return this instanceof Player;
	}

	@JsonIgnore
	void setFov(int val) {
		fov = val;
	}

	@JsonIgnore
	void setSprites(Sprite u, Sprite d, Sprite l, Sprite r) {
		upSpr = u;
		downSpr = d;
		leftSpr = l;
		rightSpr = r;
		sprite = d;

		spriteWidth = downSpr.getWidth();
	}

	@JsonIgnore
	protected void setSprites(Sprite s) {
		setSprites(s, s, s, s);
	}

	public TextureRegion getSprite() {
		return sprite;
	}

	public float getAnimationDuration() {
		return ((float) MadSand.TILESIZE / movementSpeed) * Gdx.graphics.getDeltaTime();
	}

	/* Act delay -- delay Entity's action execution during World.timeSubtick()
	 * (including movement animations)
	 */
	public void addActDelay(float actDelay) {
		Utils.dbg("%s: adding actDelay: +%f", getName(), actDelay);
		this.actDelay += actDelay;
	}

	public void setActDelay(float actDelay) {
		this.actDelay = actDelay;
	}

	public float getActDelay() {
		if (actDelay > 0)
			Utils.dbg("%s: actDelay = %f", getName(), actDelay);

		return actDelay;
	}

	public void prepareToAct() {
		actDelay = 0;
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

	public void reinit() {
		initInventory();
		inventory.setMaxWeight(stats.calcMaxInventoryWeight());
	}

	public boolean memberOf(Faction faction) {
		return stats.faction == faction;
	}

	public boolean canAfford(int cost) {
		return inventory.hasItem(Globals.values().currencyId, cost);
	}

	protected void attack(MapObject object, int dmg) {
		if (object.isDestroyed())
			MadSand.print(object.name + " shatters into pieces!");
	}

	protected void attack(MapEntity target, int dmg) {
		target.damage(dmg);

		if (dmg > 0)
			target.playDamageAnimation();

		if (target instanceof MapObject)
			attack((MapObject) target, dmg);
	}

	public abstract void meleeAttack(Direction dir);

	protected void rangedAttack(Pair targetPos, Projectile projectile) {
		Map map = MadSand.world().getCurLoc();
		Pair thisCoords = new Pair(x, y);
		Pair obstacleCoords = map.rayCast(thisCoords, targetPos);
		if (obstacleCoords.isEmpty())
			obstacleCoords.set(targetPos);

		int baseDmg = stats.calcBaseRangedAttack(distanceTo(obstacleCoords));
		int impactDmg = baseDmg != 0 ? baseDmg + projectile.calcDamage() : 0;

		projectile.launchProjectile(thisCoords.toScreen().copy(),
				obstacleCoords.toScreen().copy(),
				target -> attack(target, impactDmg));
		MadSand.getRenderer().queuePath(Path.create(thisCoords.toWorld(), obstacleCoords.toWorld()), 0.675f, Color.RED);
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
		return hasItem(item.id);
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

	public boolean colliding(Direction direction) {
		Pair coords = new Pair(x, y).addDirection(direction);
		int nx = coords.x, ny = coords.y;
		Map loc = MadSand.world().getCurLoc();

		MapObject obj = loc.getObject(nx, ny);
		AbstractNpc npc = loc.getNpc(nx, ny);

		if (!npc.equals(Map.nullNpc))
			return true;

		return !(obj.isCollisionMask() || obj.nocollide || obj.equals(Map.nullObject));
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

	public void damage(int to) {
		stats.hp -= to;
		stats.check();
	}

	public void heal(int by) {
		if (stats.hp < stats.mhp)
			playAnimation(Resources.createAnimation(Resources.healAnimStrip));

		stats.hp += by;
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
		globalPos.x = (x * MadSand.TILESIZE);
		globalPos.y = (y * MadSand.TILESIZE);
	}

	public void teleport(int x, int y) {
		if (!MadSand.world().getCurLoc().isFreeTile(x, y))
			return;

		setGridCoords(x, y);
		updCoords();
		MadSand.world().updateLight();
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

	public void calcMovementSpeed() {
		float speed = getSpeed();
		movementSpeed = speed / 17f + (float) Math.pow(Math.sqrt(speed), 1.225f) * 0.985f;
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
	public PairFloat getWorldPos() {
		if (!isMoving())
			return globalPos;

		PairFloat worldPos = new PairFloat(globalPos);

		if (stats.look == Direction.RIGHT)
			worldPos.x -= stepx;
		else if (stats.look == Direction.LEFT)
			worldPos.x += stepx;
		else if (stats.look == Direction.UP)
			worldPos.y -= stepy;
		else
			worldPos.y += stepy;

		return worldPos;
	}

	public boolean hasMoved() {
		return hasMoved;
	}

	public boolean isMoving() {
		return moving;
	}

	public void setMoving(boolean val) {
		if (moving = val)
			hasMoved = true;
	}

	public void queueMovement(Direction dir) {
		movementQueue.add(dir);
	}

	public boolean hasQueuedMovement() {
		return !movementQueue.isEmpty();
	}

	protected void pollMovementQueue() {
		if (hasQueuedMovement())
			move(movementQueue.poll());
	}

	public void stopMovement() {
		moving = false;
		stepx = stepy = MadSand.TILESIZE;
		if (!hasQueuedMovement())
			stopRunning();

		pollMovementQueue();
	}

	public void animateMovement() {
		stepx -= movementSpeed;
		stepy -= movementSpeed;

		if (stepx <= 0 && stepy <= 0)
			stopMovement();
	}

	public boolean move(Direction dir) {
		if (isDead())
			return false;

		if (dir.isDiagonal())
			return false;

		if (!colliding(dir)) {
			if (isOnMapBound(dir))
				return false;

			if (dir == Direction.UP) {
				++y;
				globalPos.y += MadSand.TILESIZE;
			}
			if (dir == Direction.DOWN) {
				--y;
				globalPos.y -= MadSand.TILESIZE;
			}
			if (dir == Direction.LEFT) {
				--x;
				globalPos.x -= MadSand.TILESIZE;
			}
			if (dir == Direction.RIGHT) {
				++x;
				globalPos.x += MadSand.TILESIZE;
			}
			setMoving(true);
			return true;
		}
		return false;
	}

	public void move(Path path) {
		path.forEachDirection(direction -> queueMovement(direction));

		if (!isMoving())
			pollMovementQueue();
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
		if (moving)
			return;

		stats.look = dir;

		if (dir == Direction.UP)
			sprite = upSpr;

		if (dir == Direction.DOWN)
			sprite = downSpr;

		if (dir == Direction.LEFT)
			sprite = leftSpr;

		if (dir == Direction.RIGHT)
			sprite = rightSpr;

	}

	boolean canWalk(Direction dir) {
		if (dir.isDiagonal())
			return false;

		if (moving)
			return false;

		stats.look = dir;
		turn(stats.look);

		if (colliding(stats.look) || isOnMapBound(stats.look))
			return false;

		return true;
	}

	boolean walk(Direction dir) {
		if (!canWalk(dir))
			return false;

		move(stats.look);
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

	public Pair getPosition() {
		return new Pair(x, y);
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

	public boolean canSee(Entity entity) {
		if (entity == this)
			return true;

		return distanceTo(entity) <= fov && rayCast(entity, canSeePredicate);
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
		addActDelay(Resources.ACTION_ANIM_DURATION);
		super.playAnimation(Resources.createAnimation(Resources.attackAnimStrip));
	}

	@JsonIgnore
	public float getSpeed() {
		float speed = (float) stats.actionPtsMax;
		if (speed < 1)
			return 1;
		return speed;
	}

	@JsonIgnore
	public float getSpriteWidth() {
		return spriteWidth;
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

	public String getName() {
		return stats.name;
	}

	@JsonIgnore
	public String getInfoString() {
		int hpPercent = (int) (((float) stats.hp / (float) stats.mhp) * 100);
		String ret;
		ret = String.format("Health: %s (%d%%)", getHealthState(hpPercent), hpPercent);
		return ret;
	}

	public String toString() {
		return String.format("{%s} %s (%d, %d) Lvl. %d [HP: %d/%d]",
				getClass().getSimpleName(),
				getName(),
				x, y,
				getLvl(),
				stats.hp, stats.mhp);
	}

	@Override
	public boolean isEmpty() {
		return this == Map.nullNpc;
	}
}
