package ru.bernarder.fallenrisefromdust;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.fasterxml.jackson.annotation.JsonIgnore;

import ru.bernarder.fallenrisefromdust.enums.Direction;
import ru.bernarder.fallenrisefromdust.enums.Skill;
import ru.bernarder.fallenrisefromdust.properties.TileProp;

public abstract class Entity {
	@JsonIgnore
	private Sprite upSpr, downSpr, leftSpr, rightSpr;

	@JsonIgnore
	private Sprite sprite;

	public int x = World.MAPSIZE / 2;
	public int y = World.MAPSIZE / 2;

	public int fov;
	public int maxFov, minFov;

	public Stats stats;
	Inventory inventory;

	public PairFloat globalPos = new PairFloat(x * MadSand.TILESIZE, y * MadSand.TILESIZE);

	int movespeed = 2; // on-screen move speed (for smooth movement)
	int stepy = MadSand.TILESIZE;
	int stepx = MadSand.TILESIZE;

	boolean stepping = false;

	public Entity(String name) {
		stats = new Stats();
		stats.name = name;
		initStatActions();
	}

	public Entity() {
		this("");
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

	private void initStatActions() {
		stats.actions = new StatAction() {
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
		};
		stats.hand = new Item();
	}

	public boolean isStepping() {
		return stepping;
	}

	public void setStepping(boolean val) {
		stepping = val;
	}

	void initInventory() {
		inventory = new Inventory();
	}

	void setName(String name) {
		stats.name = name;
	}

	void reinit() {
		inventory = new Inventory(stats.str * Stats.STR_WEIGHT_MULTIPLIER);
	}

	public boolean dropItem(int id, int quantity) {
		if (inventory.getSameCell(id, quantity) == -1)
			return false;
		doAction();
		inventory.delItem(id, quantity);
		Pair coord = new Pair(x, y).addDirection(stats.look);
		MadSand.world.getCurLoc().putLoot(coord.x, coord.y, id, quantity);
		return true;
	}

	void pickUpLoot() {
		if (stats.dead)
			return;
		Loot loot = MadSand.world.getCurLoc().getLoot(x, y);
		if (loot != Map.nullLoot) {
			for (int i = loot.contents.size() - 1; i >= 0; --i) {
				if (inventory.putItem(loot.contents.get(i)))
					loot.remove(i);
				else
					break;
			}
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

	boolean isInBackground() {
		Map loc = MadSand.world.getCurLoc();
		return (loc.getObject(x, y).isCollisionMask() || loc.getTile(x, y).foreground);
	}

	boolean standingOnLoot(int x, int y) {
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

	void skillBonusItems(int x, int y, String direction, int id) {
		// Idk what's this thing
	}

	void damage(int to) {
		stats.hp -= to;
		stats.check();
	}

	void heal(int to) {
		if (stats.hp + to < stats.mhp) {
			stats.hp += stats.skills.getLvlReward(Skill.Survival, to);
		} else {
			stats.hp = stats.mhp;
		}
	}

	void starve() {
		--stats.food;
		stats.check();
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

	void dropInventory() {
		Item item;
		for (int i = inventory.items.size() - 1; i >= 0; --i) {
			item = inventory.items.get(i);
			MadSand.world.getCurLoc().putLoot(x, y, item);
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
	
	int attack() {
		return stats.str;
	}

	int doAction() {
		return doAction(stats.AP_MINOR);
	}

	public void freeHands() {
		stats.hand = Item.nullItem;
	}

	void updCoords() {
		globalPos.x = (x * MadSand.TILESIZE);
		globalPos.y = (y * MadSand.TILESIZE);
	}

	public void teleport(int x, int y) {
		setGridCoords(x, y);
		updCoords();
	}

	public void setGridCoords(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public boolean isOnMapBound(Direction dir) {
		boolean ret = false;
		if (x >= World.MAPSIZE - 1 && (dir == Direction.RIGHT)) {
			ret = true;
		}
		if (y >= World.MAPSIZE - 1 && (dir == Direction.UP)) {
			ret = true;
		}
		if (x <= 1 && (dir == Direction.LEFT)) {
			ret = true;
		}
		if (y <= 1 && (dir == Direction.DOWN)) {
			ret = true;
		}
		return ret;
	}

	public boolean move(Direction dir) {
		if ((!colliding(dir))) {
			boolean onm = !isOnMapBound(dir);
			if ((dir == Direction.UP) && (onm)) {
				++y;
				globalPos.y += MadSand.TILESIZE;
			}
			if ((dir == Direction.DOWN) && (onm)) {
				--y;
				globalPos.y -= MadSand.TILESIZE;
			}
			if ((dir == Direction.LEFT) && (onm)) {
				--x;
				globalPos.x -= MadSand.TILESIZE;
			}
			if ((dir == Direction.RIGHT) && (onm)) {
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
		int dmg = TileProp.damage.getOrDefault(tid, 0);
		if (dmg > 0)
			damage(dmg);
		return tid;
	}

	public void turn(Direction dir) {
		stats.look = dir;
		if (!stepping) {
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
		if (stepping)
			return false;
		stats.look = dir;
		turn(stats.look);
		if (colliding(stats.look) || isOnMapBound(stats.look))
			return false;
		doAction(stats.AP_WALK);
		move(stats.look);
		return true;
	}

	void randMove() {
		move(Direction.random());
	}

	int distanceTo(Entity entity) {
		return (int) MadSand.calcDistance(x, y, entity.x, entity.y);
	}
}
