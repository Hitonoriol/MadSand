package ru.bernarder.fallenrisefromdust;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;

import ru.bernarder.fallenrisefromdust.enums.*;
import ru.bernarder.fallenrisefromdust.properties.ItemProp;
import ru.bernarder.fallenrisefromdust.properties.ObjectProp;
import ru.bernarder.fallenrisefromdust.properties.TileProp;

public class Player {

	public int x = World.MAPSIZE / 2;
	public int y = World.MAPSIZE / 2;

	public int fov;
	public int maxFov, minFov;

	private String name;
	public Stats stats = new Stats();
	Inventory inventory;

	public PairFloat globalPos = new PairFloat(x * MadSand.TILESIZE, y * MadSand.TILESIZE);

	int movespeed = 2;
	int stepy = MadSand.TILESIZE;
	int stepx = MadSand.TILESIZE;

	public boolean isMain = true;
	private boolean stepping = false;

	public Player(String name) {
		this.name = name;
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
		stats.hand = new Item();
	}

	public boolean isStepping() {
		return stepping;
	}

	public void setStepping(boolean val) {
		stepping = val;
	}

	void increaseSkill(Skill skill) {
		stats.skills.increaseSkill(skill);
		stats.skills.increaseSkill(Skill.Level);
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
		int itemIdx = inventory.getSameCell(id);
		if (itemIdx == -1) {
			stats.hand = Item.nullItem;
			Gui.setHandDisplay(0);
			return;
		}
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

	public boolean colliding(Direction direction, int flag) {
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

	public boolean craftItem(int id) {
		if (inventory.delItem(ItemProp.recipe.get(id))) {
			increaseSkill(Skill.Crafting);
			inventory.putItem(id, stats.skills.getItemReward(Skill.Crafting));
			Gui.drawOkDialog("Crafted " + ItemProp.name.get(id), Gui.craft);
			doAction(Stats.AP_MINOR);
			return true;
		}
		Gui.drawOkDialog("Not enough resources to craft " + ItemProp.name.get(id), Gui.craft);
		return false;
	}

	void interact(final Direction direction) {
		int id = MadSand.world.getCurLoc().getObject(x, y, stats.look).id;
		if (id == 0)
			return;
		String action = ObjectProp.interactAction.get(id);
		doAction();
		if (action != "-1") {
			BuildScript.execute(action);
			return;
		}
		int item = MapObject.getAltItem(id, ItemProp.type.get(stats.hand.id).get());
		MapObject obj = MadSand.world.getCurLoc().getObject(x, y, stats.look);
		int mhp = ObjectProp.harvestHp.get(obj.id);
		Skill skill = obj.skill;
		int curLvl = stats.skills.getLvl(skill);
		inventory.damageTool(stats.hand, skill);
		if (curLvl < obj.lvl) {
			MadSand.print("You are not experienced enough.");
			MadSand.print(skill + " level required: " + obj.lvl);
			MadSand.print("Your " + skill + ": " + curLvl);
			return;
		}

		boolean destroyed = obj.takeDamage(stats.skills.getLvl(skill) + stats.hand.getSkillDamage(skill));
		if (item != -1 && destroyed) {
			inventory.putItem(item, stats.skills.getItemReward(skill));
			increaseSkill(skill);
		}
		if (!destroyed)
			MadSand.print("You hit a " + obj.name + " [ " + obj.harverstHp + " / " + mhp + " ]");
		if (item == -1 && destroyed)
			MadSand.print("You damaged " + obj.name);
	}

	public void useItem() {
		int id = stats.hand.id;
		int ptile = MadSand.world.getTileId(x, y);
		int item = MapObject.getTileAltItem(ptile, stats.hand.type.get());
		checkHands(id);
		inventory.damageTool(stats.hand);
		if (item != -1) {
			MadSand.world.getCurLoc().delTile(x, y);
			World.player.inventory.putItem(item);
			increaseSkill(Skill.Digging);
		}
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
		if (Item.getType(id) == ItemType.Consumable) {
			increaseSkill(Skill.Survival);
			MadSand.print("You ate one " + ItemProp.name.get(id));
			String cont[] = ItemProp.heal.get(id).split(":");
			int healAmt = Integer.parseInt(cont[0]);
			int satAmt = Integer.parseInt(cont[1]);
			heal(healAmt);
			satiate(satAmt);
			inventory.delItem(id);
		}
		if ((id == 9) && (World.player.inventory.getSameCell(9, 1) != -1) // TODO script this or make campfire craftable
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
		if (Item.getType(id) == ItemType.Crop) {
			Pair coords = new Pair(x, y).addDirection(stats.look);
			if (MadSand.world.getCurLoc().putCrop(coords.x, coords.y, id)) {
				increaseSkill(Skill.Farming);
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
		checkHands(id);
	}

	public void freeHands() {
		MadSand.print("You put your " + stats.hand.name + " back to your inventory");
		stats.hand = Item.nullItem;
		Gui.setHandDisplay(stats.hand.id);
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

	public boolean VerifyPosition(Direction dir) {
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

	public void move(Direction dir) {
		if ((!colliding(dir, 0)) && (MadSand.dialogflag)) {
			if ((dir == Direction.UP) && (!VerifyPosition(dir))) {
				++y;
				globalPos.y += MadSand.TILESIZE;
			}
			if ((dir == Direction.DOWN) && (!VerifyPosition(dir))) {
				--y;
				globalPos.y -= MadSand.TILESIZE;
			}
			if ((dir == Direction.LEFT) && (!VerifyPosition(dir))) {
				--x;
				globalPos.x -= MadSand.TILESIZE;
			}
			if ((dir == Direction.RIGHT) && (!VerifyPosition(dir))) {
				++x;
				globalPos.x += MadSand.TILESIZE;
			}
			if (x == World.MAPSIZE - 1 || y == World.MAPSIZE - 1 || x == World.BORDER || y == World.BORDER) {
				MadSand.print("Press [GRAY]N[WHITE] to move to the next sector.");
			}
			stepping = true;
		}
	}

	public void tileDmg() {
		int tid = MadSand.world.getTileId(x, y);
		int dmg = TileProp.damage.getOrDefault(tid, 0);
		if (dmg > 0) {
			MadSand.print("You took " + dmg + " damage from " + (TileProp.name.get(tid)));
			damage(dmg);
		}
	}

	public void turn(Direction dir) {
		stats.look = dir;
		if (!stepping) {
			if (dir == Direction.UP) {
				Resource.playerSprite = new Sprite(Resource.utex);
			}
			if (dir == Direction.DOWN) {
				Resource.playerSprite = new Sprite(Resource.dtex);
			}
			if (dir == Direction.LEFT) {
				Resource.playerSprite = new Sprite(Resource.ltex);
			}
			if (dir == Direction.RIGHT) {
				Resource.playerSprite = new Sprite(Resource.rtex);
			}
		}
	}

	void walk(Direction dir) {
		if (stepping)
			return;
		stats.look = dir;
		turn(stats.look);
		if (MadSand.world.getCurLoc().getObject(x, y, stats.look).id != 0 || VerifyPosition(stats.look))
			return;
		doAction(Stats.AP_WALK);
		move(stats.look);
		objectInFront();
	}

	public void objectInFront() {
		int obj = MadSand.world.getObjID(x, y, stats.look);
		if ((obj != 666) && (obj != 0)) {
			MadSand.print("You see: " + ObjectProp.name.get(obj));
		}
	}
}