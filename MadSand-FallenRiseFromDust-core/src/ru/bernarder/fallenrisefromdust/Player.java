package ru.bernarder.fallenrisefromdust;

import java.util.Random;

import com.badlogic.gdx.Gdx;

import ru.bernarder.fallenrisefromdust.enums.*;
import ru.bernarder.fallenrisefromdust.properties.ItemProp;
import ru.bernarder.fallenrisefromdust.properties.ObjectProp;

public class Player {

	public int x = new Random().nextInt(World.MAPSIZE);
	public int y = new Random().nextInt(World.MAPSIZE);

	private String name;
	public Stats stats = new Stats();
	Inventory inventory;

	public PairFloat globalPos;

	public Player(String name) {
		this.name = name;
		init();
	}

	public Player() {
		this("");
	}

	void init() {
		stats.name = name;
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

	public void checkHands(int id) {
		if (inventory.getSameCell(id, 1) == -1)
			stats.hand = 0;
	}

	public boolean dropItem(int id, int quantity) {
		Utils.out("Item drop: " + id + " " + quantity);
		if (inventory.getSameCell(id, quantity) == -1)
			return false;
		doAction();
		inventory.delItem(id, quantity);
		Pair coord = new Pair(x, y).addDirection(stats.look);
		MadSand.world.getCurLoc().putLoot(coord.x, coord.y, id, quantity);
		return true;
	}

	void pickUpLoot() {
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

	void interact(final Direction direction) {
		int id = MadSand.world.getCurLoc().getObject(x, y, stats.look).id;
		Utils.out("Interacting with " + id);
		String action = ObjectProp.interactAction.get(id);
		doAction();
		if (action != "-1") {
			BuildScript.execute(action);
			return;
		}
		int item = MapObject.getAltItem(id, ItemProp.type.get(stats.hand).get());
		MapObject obj = MadSand.world.getCurLoc().getObject(x, y, stats.look);
		int mhp = ObjectProp.harvestHp.get(obj.id);
		boolean destroyed = obj.takeDamage();
		if (item != -1 && destroyed) {
			inventory.putItem(item, 1);
		}
		if (!destroyed)
			MadSand.print("Harvesting from " + obj.name + " [ " + obj.harverstHp + " / " + mhp + " ]");
		if (item == -1 && destroyed)
			MadSand.print("You damaged " + obj.name);
	}

	public boolean isCollision(Direction direction, int flag) {
		boolean collision = false;
		int oid = MadSand.world.getCurLoc().getObject(x, y, direction).id;
		if (((flag == 0) && (oid == 12)) || (oid == 0) || (oid == 666)) {
			collision = false;
		} else
			collision = true;
		return collision;
	}

	public static boolean isCollisionMask(int x, int y) {
		if (x < World.MAPSIZE && y < World.MAPSIZE) {
			if (MadSand.world.getCurLoc().getObject(x, y).id == 666) {
				return true;
			}
		}
		return false;
	}

	boolean standingOnLoot(int x, int y) {
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
			stats.hp += to;
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

	void die() {
		Gui.darkness.setVisible(true);
		Gdx.input.setInputProcessor(Gui.dead);
		MadSand.state = GameState.DEAD;
	}

	int doAction(int ap) { // any action that uses AP
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
		MadSand.world.ticks(ticks);
		return stats.actionPts;
	}

	int doAction() {
		return doAction(Stats.AP_MINOR);
	}

	public void useItem() {
		int id = stats.hand;
		int ptile = MadSand.world.getTileId(x, y);
		checkHands(id);
		String action = ItemProp.useAction.get(id);
		World.player.doAction();
		if (action != "-1") {
			BuildScript.execute(action);
			return;
		}
		if ((ptile == 6) || (ptile == 16)) {
			MadSand.print("You entered the dungeon.");
			MadSand.world.curlayer += 1;
			MadSand.world.delObj(World.player.x, World.player.y);
		}
		if (id == 6) {
			if (ptile == 0) {
				MadSand.world.putMapTile(World.player.y, World.player.x, 6);
				MadSand.print("You dug a hole.");
			}
			if (ptile == 3) {
				MadSand.world.putMapTile(World.player.y, World.player.x, 16);
				MadSand.print("You dug a hole.");
			}
			if (ptile == 1) {
				World.player.inventory.putItem(5, 1, true);
				MadSand.world.putMapTile(World.player.y, World.player.x, 0);
				MadSand.print("You dug some clay");
			}
			if (ptile == 2) {
				World.player.inventory.putItem(9, 1, true);
				MadSand.world.putMapTile(World.player.y, World.player.x, 0);
				MadSand.print("You dug some flint");
			}
		}
		if (Item.getType(id) == ItemType.Consumable) {
			MadSand.print("You ate one " + ItemProp.name.get(id));
			String cont[] = ItemProp.heal.get(id).split(":");
			heal(Integer.parseInt(cont[0]));
			satiate(Integer.parseInt(cont[1]));

		}
		if ((id == 9) && (World.player.inventory.getSameCell(9, 1) != -1)
				&& (World.player.inventory.getSameCell(1, 5) != -1)) {
			MadSand.print("You placed a campfire");
			World.player.inventory.delItem(9);
			World.player.inventory.delItem(1);
			MadSand.world.getCurLoc().addObject(World.player.x, World.player.y, World.player.stats.look, 6);
		}
		if (Item.getType(id) == ItemType.HeadArmor) {
			// equip helmet
		}
		if (Item.getType(id) == ItemType.ChestArmor) {
			// equip chestplate
		}
		if (Item.getType(id) == ItemType.Shield) {
			// equip shield
		}
		if (Item.getType(id) == ItemType.Crop) { // crop
			Pair coords = new Pair(x, y).addDirection(stats.look);
			if (MadSand.world.getCurLoc().putCrop(coords.x, coords.y, id)) {
				MadSand.print("You planted 1 " + new Item(id).name);
				World.player.inventory.delItem(id);
			}

		}
		if (Item.getType(id) == ItemType.PlaceableObject) {
			World.player.inventory.delItem(id);
			MadSand.world.getCurLoc().addObject(World.player.x, World.player.y, World.player.stats.look,
					Item.getAltObject(id));
		}
		if (Item.getType(id) == ItemType.PlaceableTile) {
			World.player.inventory.delItem(id);
			MadSand.world.getCurLoc().addTile(World.player.x, World.player.y, World.player.stats.look,
					Item.getAltObject(id));
		}

	}

	public void freeHands() {
		MadSand.print("You put your " + ObjectProp.name.get(stats.hand) + " back to your inventory");
		stats.hand = 0;
		Gui.setHandDisplay(stats.hand);
	}

	void updCoords() {
		World.player.globalPos.x = (x * MadSand.TILESIZE);
		World.player.globalPos.y = (y * MadSand.TILESIZE);
	}

	public void teleport(int x, int y) {
		this.x = x;
		this.y = y;
		updCoords();
	}

	public void respawn() {
		int wx = MadSand.world.curxwpos;
		int wy = MadSand.world.curywpos;
		MadSand.state = GameState.GAME;
		stats.food = stats.maxFood;
		stats.actionPts = stats.actionPtsMax;
		stats.hp = stats.mhp;
		stats.stamina = stats.maxstamina;
		stats.dead = false;
		this.init();

		if (stats.respawnX == -1) {
			x = Utils.rand(0, MadSand.world.getCurLoc().getWidth());
			y = Utils.rand(0, MadSand.world.getCurLoc().getHeight());
		} else {
			if (stats.respawnWX == wx && stats.respawnWY == wy) {
				x = stats.respawnX;
				y = stats.respawnY;
			} else {
				wx = MadSand.world.curxwpos = stats.respawnWX;
				wy = MadSand.world.curywpos = stats.respawnWY;
				if (GameSaver.verifyNextSector(wx, wy)) {
					MadSand.world.clearCurLoc();
					GameSaver.loadSector();
				} else {
					MadSand.world.Generate();
				}
			}
		}
		World.player.updCoords();
	}
}