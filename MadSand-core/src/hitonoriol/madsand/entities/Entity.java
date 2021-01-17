package hitonoriol.madsand.entities;

import java.awt.Point;
import java.util.Comparator;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.fasterxml.jackson.annotation.JsonIgnore;
import hitonoriol.madsand.Keyboard;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.containers.Line;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.containers.PairFloat;
import hitonoriol.madsand.entities.inventory.Inventory;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.gui.dialogs.LootDialog;
import hitonoriol.madsand.map.Loot;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.MapObject;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.TileProp;

public abstract class Entity {
	private static final float MOVE_SPEED_POW = 1.35f;
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

	public int fov = 15;
	public int maxFov, minFov;

	protected Pair coords = new Pair();

	public Inventory inventory;
	public Stats stats;

	@JsonIgnore
	public float stepy = MadSand.TILESIZE;
	@JsonIgnore
	public float stepx = MadSand.TILESIZE;

	boolean stepping = false;

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
	void setSprites(Sprite s) {
		setSprites(s, s, s, s);
	}

	public Sprite getSprite() {
		return sprite;
	}

	public void initStatActions() {
		stats.setOwner(this);
	}

	public boolean at(int x, int y) {
		return this.x == x && this.y == y;
	}

	public boolean isStepping() {
		return stepping;
	}

	public void setStepping(boolean val) {
		stepping = val;
	}

	public void stopMovement() {
		stepping = false;
		stepx = stepy = MadSand.TILESIZE;
		if (Keyboard.inputIgnored())
			Keyboard.resumeInput();
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

	protected void attack(Entity target, int dmg) {
		target.damage(dmg);
	}

	abstract void meleeAttack(Direction dir);

	void rangedAttack(Entity target, Item projectile) {
		if (!canSee(target))
			return;

		int dmg = stats.calcBaseRangedAttack() + projectile.dmg;
		projectile.launchProjectile(globalPos, target.globalPos, () -> attack(target, dmg));
	}

	public boolean addItem(Item item) {
		return inventory.putItem(item);
	}

	public boolean addItem(int id, int quantity) {
		Item item = new Item(id, quantity);
		return addItem(item);
	}

	public boolean dropItem(Item item, int quantity) {
		if (!hasItem(item))
			return false;

		MadSand.world.getCurLoc().putLoot(x, y, new Item(item, quantity));
		inventory.delItem(item, quantity);
		return true;
	}

	public boolean hasItem(int id) {
		return inventory.getSameCell(id) != -1;
	}

	public boolean hasItem(Item item) {
		return hasItem(item.id);
	}

	public boolean pickUpLoot(Loot loot, Item item, int quantity) {
		boolean removeStack = quantity >= item.quantity;
		Item pickedUpItem = removeStack ? loot.remove(item) : new Item(item, quantity);

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
		Npc npc = loc.getNpc(nx, ny);

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

	public void heal(int to) {
		// stats.skills.getLvlReward(Skill.Survival, to)
		stats.hp += to;
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
			curLoc.putLoot(x, y, new Item(item));
			inventory.delItem(item);
		}
	}

	void die() {
		stats.dead = true;
		dropInventory();
	}

	float getActionLength(double ap) {
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
		setGridCoords(x, y);
		updCoords();
		MadSand.world.updateLight();
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

	public void calcMoveSpeed() {
		movementSpeed = (float) Math.pow(Math.sqrt(getSpeed()), MOVE_SPEED_POW);
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
			stepping = true;
			return true;
		}
		return false;
	}

	public int tileDmg() {
		int tid = MadSand.world.getTileId(x, y);
		int dmg = TileProp.getTileProp(tid).damage;
		if (dmg > 0)
			damage(dmg);
		return tid;
	}

	public void turn(Direction dir) {
		if (stepping)
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

		if (stepping)
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

	public double distanceTo(Entity entity) {
		return Line.calcDistance(x, y, entity.x, entity.y);
	}

	public boolean canSee(Entity entity) {
		int dist = (int) distanceTo(entity);
		Map loc = MadSand.world.getCurLoc();
		MapObject object;
		boolean viewObstructed = false;

		for (Point p : new Line(x, y, entity.x, entity.y))
			if ((object = loc.getObject(p.x, p.y)) != Map.nullObject) {
				if (viewObstructed = !object.nocollide)
					break;
			}

		return (dist <= fov) && !viewObstructed;
	}

	public void attackAnimation(Entity entity) {
		MadSand.queueAnimation(Resources.createAnimation(Resources.attackAnimStrip),
				entity.globalPos.x, entity.globalPos.y);
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

	@JsonIgnore
	public String getInfoString() {
		String ret = "";
		ret += "Faction: " + stats.faction + Resources.LINEBREAK;
		ret += "Health: " + getHealthState() + Resources.LINEBREAK;
		return ret;
	}

}
