package hitonoriol.madsand.entities;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Queue;
import java.util.function.Predicate;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.Keyboard;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
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

public abstract class Entity extends MapEntity {
	@JsonIgnore
	private Sprite upSpr, downSpr, leftSpr, rightSpr;

	public static Comparator<Entity> speedComparator = new Comparator<Entity>() {
		@Override
		public int compare(Entity o1, Entity o2) {
			return Double.compare(o2.getSpeed(), o1.getSpeed());
		}
	};

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

	boolean moving = false;

	public Entity(String name) {
		stats = isPlayer() ? new PlayerStats() : new Stats();
		stats.name = name;
		initStatActions();
		initInventory();
	}

	public Entity() {
		this("");
	}

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

	public void setActDelay(float actDelay) {
		Utils.out(getName() + ": adding actDelay: +" + actDelay);
		this.actDelay += actDelay;
	}

	public float getActDelay() {
		float delay = actDelay;

		if (delay > 0)
			Utils.out(getName() + ": actDelay=" + delay);

		actDelay = 0;
		return delay;
	}

	public void initStatActions() {
		stats.setOwner(this);
	}

	public boolean at(int x, int y) {
		return this.x == x && this.y == y;
	}

	public boolean isMoving() {
		return moving;
	}

	public void setMoving(boolean val) {
		moving = val;
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
		pollMovementQueue();
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
		return inventory.hasItem(Globals.getInt(Globals.CURRENCY), cost);
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
		Map map = MadSand.world.getCurLoc();
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

	public boolean addItem(Item item) {
		return inventory.putItem(item);
	}

	public boolean addItem(int id, int quantity) {
		return addItem(Item.create(id, quantity));
	}

	public void delItem(Item item, int quantity) {
		inventory.delItem(item, quantity);
	}

	public void delItem(int id, int quantity) {
		delItem(Item.create(id), quantity);
	}

	public boolean dropItem(Item item, int quantity) {
		if (!hasItem(item))
			return false;

		MadSand.world.getCurLoc().putLoot(x, y, Item.duplicate(item, quantity));
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
		for (int i = loot.contents.size() - 1; i >= 0; --i)
			if (!pickUpLoot(loot, loot.contents.get(i)))
				break;
	}

	public void pickUpLoot() {
		Loot loot = MadSand.world.getCurLoc().getLoot(x, y);

		if (loot.equals(Map.nullLoot))
			return;

		new LootDialog(loot).show();
	}

	public boolean colliding(Direction direction) {
		Pair coords = new Pair(x, y).addDirection(direction);
		int nx = coords.x, ny = coords.y;
		Map loc = MadSand.world.getCurLoc();

		MapObject obj = loc.getObject(nx, ny);
		AbstractNpc npc = loc.getNpc(nx, ny);

		if (!npc.equals(Map.nullNpc))
			return true;

		return !(obj.isCollisionMask() || obj.nocollide || obj.equals(Map.nullObject));
	}

	@JsonIgnore
	public boolean isInBackground() {
		Map loc = MadSand.world.getCurLoc();
		return (loc.getObject(x, y).isCollisionMask() || loc.getTile(x, y).foreground);
	}

	public boolean standingOnLoot(int x, int y) {
		if (stats.dead)
			return false;
		if (MadSand.world.getCurLoc().getLoot(x, y).equals(Map.nullLoot))
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

	void dropInventory() {
		Item item;
		Map curLoc = MadSand.world.getCurLoc();
		for (int i = inventory.items.size() - 1; i >= 0; --i) {
			item = inventory.items.get(i);
			curLoc.putLoot(x, y, new Item(item)); // TODO: Item.copy
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
		Map map = MadSand.world.getCurLoc();
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
		if (!MadSand.world.getCurLoc().isFreeTile(x, y))
			return;

		setGridCoords(x, y);
		updCoords();
		MadSand.world.updateLight();
	}

	public void teleport(Pair coords) {
		teleport(coords.x, coords.y);
	}

	public void setGridCoords(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public boolean isOnMapBound(Direction dir) {
		Map map = MadSand.world.getCurLoc();
		boolean ret = false;
		if (x >= map.getWidth() - 1 && (dir == Direction.RIGHT)) {
			ret = true;
		}
		if (y >= map.getHeight() - 1 && (dir == Direction.UP)) {
			ret = true;
		}
		if (x < 1 && (dir == Direction.LEFT)) {
			ret = true;
		}
		if (y < 1 && (dir == Direction.DOWN)) {
			ret = true;
		}
		return ret;
	}

	public void calcMovementSpeed() {
		float speed = getSpeed();
		movementSpeed = speed / 17f + (float) Math.pow(Math.sqrt(speed), 1.225f) * 0.975f;
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

	public void animateMovement() {
		stepx -= movementSpeed;
		stepy -= movementSpeed;

		if (stepx <= 1)
			stopMovement();
	}

	public boolean move(Direction dir) {
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
			moving = true;
			return true;
		}
		return false;
	}

	public void move(Path path) {
		path.forEachDirection(direction -> queueMovement(direction));

		if (!isMoving())
			pollMovementQueue();
	}

	public int tileDmg() {
		int tid = MadSand.world.getTileId(x, y);
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
		Map map = MadSand.world.getCurLoc();
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
		setActDelay(Resources.ACTION_ANIM_DURATION);
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
		float state = (float) stats.hp / (float) stats.mhp;
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

	public String getName() {
		return stats.name;
	}

	@JsonIgnore
	public String getInfoString() {
		String ret = "";
		ret += "Health: " + getHealthState() + Resources.LINEBREAK;
		return ret;
	}

}
