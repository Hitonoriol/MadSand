package hitonoriol.madsand.entities;

import java.awt.Point;
import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.containers.Line;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.containers.PairFloat;
import hitonoriol.madsand.entities.inventory.Inventory;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.map.Loot;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.MapObject;
import hitonoriol.madsand.properties.TileProp;

public abstract class Entity {
	@JsonIgnore
	private Sprite upSpr, downSpr, leftSpr, rightSpr;

	@JsonIgnore
	private Sprite sprite;

	public int x, y; // Grid coords
	public PairFloat globalPos = new PairFloat(); // Screen space coords

	public int fov = 15;
	public int maxFov, minFov;

	public Inventory inventory;
	public Stats stats;

	@JsonIgnore
	public int movespeed = 2; // on-screen move speed (for smooth movement)
	@JsonIgnore
	public int stepy = MadSand.TILESIZE;
	@JsonIgnore
	public int stepx = MadSand.TILESIZE;

	boolean stepping = false;

	public Entity(String name) {
		stats = new Stats();
		stats.name = name;
		initStatActions();
		initInventory();
	}

	public Entity() {
		this("");
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
	}

	@JsonIgnore
	void setSprites(Sprite s) {
		setSprites(s, s, s, s);
	}

	public Sprite getSprite() {
		return sprite;
	}

	public void initStatActions() {
		stats.owner = new StatAction() {
			@Override
			public void _die() {
				die();
			}

			@Override
			public void _damage(int amt) {
				damage(amt);
			}

			@Override
			public void _heal(int amt) {
				heal(amt);
			}

			@Override
			public Item _getItem(int id) {
				return inventory.getItem(id);
			}
		};
	}

	public boolean isStepping() {
		return stepping;
	}

	public void setStepping(boolean val) {
		stepping = val;
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

	boolean attack(Direction dir) {
		return false;
	}

	public boolean addItem(Item item) {
		return inventory.putItem(item);
	}

	public boolean addItem(int id, int quantity) {
		Item item = new Item(id, quantity);
		return addItem(item);
	}

	public boolean dropItem(Item item) {

		if (inventory.getSameCell(item) == -1)
			return false;

		Pair coord = new Pair(x, y).addDirection(stats.look);
		MadSand.world.getCurLoc().putLoot(coord.x, coord.y, new Item(item));
		inventory.delItem(item);

		doAction();
		return true;
	}

	public boolean hasItem(int id) {
		return inventory.getSameCell(id) != -1;
	}

	public void pickUpLoot() {

		if (stats.dead)
			return;

		Loot loot = MadSand.world.getCurLoc().getLoot(x, y);

		if (loot.equals(Map.nullLoot))
			return;

		for (int i = loot.contents.size() - 1; i >= 0; --i) {
			if (addItem(new Item(loot.contents.get(i))))
				loot.remove(i);
			else
				break;
		}

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

		if (stats.hp > stats.mhp)
			stats.hp = stats.mhp;

	}

	void satiate(int amt) {
		stats.food += amt;
		stats.check();
	}

	void increaseStamina(int to) {
		if (stats.stamina + to < stats.maxstamina) {
			stats.stamina += to;
		} else {
			stats.stamina = stats.maxstamina;
		}
	}

	@JsonIgnore
	public int getDefense() {
		return stats.defense;
	}

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

	int doAction(int ap) { // any action that uses AP \\ returns number of ticks spent
		int tmp = stats.actionPts;
		stats.actionPts -= ap;
		int ticks = 0, absPts = Math.abs(stats.actionPts), absTmp = Math.abs(tmp);

		if (stats.actionPts <= 0) {
			ticks = (absPts / stats.actionPtsMax);
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

	boolean equip(Item item) {
		return stats.equip(item);
	}

	boolean rest() {
		if (++stats.actionPts >= stats.actionPtsMax) {
			stats.actionPts = stats.actionPtsMax;
			return true;
		} else
			return false;
	}

	boolean canAct(int ap) {
		return (ap <= stats.actionPts);
	}

	public int doAction() {
		return doAction(stats.AP_MINOR);
	}

	public void freeHands() {
		stats.setHand(Item.nullItem);
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

	public boolean move(Direction dir) {
		if (dir.isDiagonal())
			return false;
		if ((!colliding(dir))) {
			boolean canMove = !isOnMapBound(dir);
			if ((dir == Direction.UP) && (canMove)) {
				++y;
				globalPos.y += MadSand.TILESIZE;
			}
			if ((dir == Direction.DOWN) && (canMove)) {
				--y;
				globalPos.y -= MadSand.TILESIZE;
			}
			if ((dir == Direction.LEFT) && (canMove)) {
				--x;
				globalPos.x -= MadSand.TILESIZE;
			}
			if ((dir == Direction.RIGHT) && (canMove)) {
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
		stats.look = dir;
		if (!stepping && dir.isBase()) {
			if (dir == Direction.UP) {
				sprite = upSpr;
			}
			if (dir == Direction.DOWN) {
				sprite = downSpr;
			}
			if (dir == Direction.LEFT) {
				sprite = leftSpr;
			}
			if (dir == Direction.RIGHT) {
				sprite = rightSpr;
			}
		}
	}

	boolean walk(Direction dir) {

		if (dir.isDiagonal())
			return false;

		if (stepping)
			return false;

		stats.look = dir;
		turn(stats.look);

		if (colliding(stats.look) || isOnMapBound(stats.look))
			return false;

		move(stats.look);
		doAction(stats.AP_WALK);
		return true;
	}

	void randMove() {
		move(Direction.random());
	}

	int distanceTo(Entity entity) {
		return (int) Line.calcDistance(x, y, entity.x, entity.y);
	}

	public boolean canSee(Entity entity) {
		int dist = distanceTo(entity);
		Map loc = MadSand.world.getCurLoc();
		boolean viewObstructed = false;

		for (Point p : new Line(x, y, entity.x, entity.y))
			if (loc.getObject(p.x, p.y) != Map.nullObject) {
				viewObstructed = true;
				break;
			}

		if ((dist <= fov) && !viewObstructed)
			return true;

		return false;
	}

	@JsonIgnore
	public int getSpeed() {
		return stats.actionPtsMax;
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
		ret += "Faction: " + stats.faction + Resources.LINEBREAK;
		ret += "Health: " + getHealthState() + Resources.LINEBREAK;
		return ret;
	}

	@JsonGetter("Equipment")
	public ArrayList<String> getEquipment() { // For serializer
		ArrayList<String> list = stats.equipment.getUidList();
		return list;
	}

	@JsonSetter("Equipment")
	public void setEquipment(ArrayList<String> list) { // For deserializer
		int len = list.size();
		for (int i = 0; i < len - 1; ++i)
			stats.equip(inventory.getItem(list.get(i)));

		int handId = Utils.val(list.get(len - 1));
		stats.setHand(inventory.getItem(handId));
	}

	@JsonGetter("isPlayer")
	public boolean isPlayer() {
		return stats.equipment.getIsPlayer();
	}

	@JsonSetter("isPlayer")
	public void setIsPlayer(boolean val) {
		stats.equipment.setIsPlayer(val);
	}
}
