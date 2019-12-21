package ru.bernarder.fallenrisefromdust;

import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.annotation.JsonIgnore;

import ru.bernarder.fallenrisefromdust.enums.*;
import ru.bernarder.fallenrisefromdust.properties.ItemProp;
import ru.bernarder.fallenrisefromdust.properties.ObjectProp;
import ru.bernarder.fallenrisefromdust.properties.TileProp;

public class Player extends Entity {

	public boolean isMain = true;

	@JsonIgnore
	public Player(String name) {
		super(name);
		super.setSprites(Resources.playerUpSpr, Resources.playerDownSpr, Resources.playerLeftSpr,
				Resources.playerRightSpr);
		initInventory();
	}

	public Player() {
		this("");
	}

	void increaseSkill(Skill skill) {
		stats.skills.increaseSkill(skill);
		stats.skills.increaseSkill(Skill.Level);
	}

	public void checkHands(int id) {
		int itemIdx = inventory.getSameCell(id);
		if (itemIdx == -1) {
			stats.hand = Item.nullItem;
			if (isMain)
				Gui.setHandDisplay(0);
			return;
		}
	}

	@Override
	void die() {
		super.die();
		Gui.setDeadText("You died\nYou survived " + getSurvivedTime() + " ticks");
		Gui.darkness.setVisible(true);
		Gdx.input.setInputProcessor(Gui.dead);
		MadSand.state = GameState.DEAD;
	}

	void damageHeldTool(Skill objectSkill) {
		if (inventory.damageTool(stats.hand, objectSkill)) {
			if (isMain)
				MadSand.print("Your " + stats.hand.name + " broke");
			inventory.delItem(stats.hand);
			freeHands(true);
		}
	}

	void damageHeldTool() {
		damageHeldTool(Skill.None);
	}

	public boolean craftItem(int id) {
		if (inventory.delItem(ItemProp.recipe.get(id))) {
			increaseSkill(Skill.Crafting);
			inventory.putItem(id, stats.skills.getItemReward(Skill.Crafting));
			if (isMain)
				Gui.drawOkDialog("Crafted " + ItemProp.name.get(id), Gui.craft);
			doAction(Stats.AP_MINOR);
			return true;
		}
		if (isMain)
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
		damageHeldTool(skill);
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
		damageHeldTool();
		if (item != -1) {
			MadSand.world.getCurLoc().delTile(x, y);
			inventory.putItem(item);
			increaseSkill(Skill.Digging);
		}
		String action = ItemProp.useAction.get(id);
		doAction();
		if (action != "-1") {
			BuildScript.execute(action);
			return;
		}
		if ((ptile == 6) || (ptile == 16)) {
			MadSand.print("You entered the dungeon.");
			MadSand.world.curlayer += 1;
			MadSand.world.delObj(x, y);
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
		if ((id == 9) && (inventory.getSameCell(9, 1) != -1) // TODO script this or make campfire craftable
				&& (inventory.getSameCell(1, 5) != -1)) {
			MadSand.print("You placed a campfire");
			inventory.delItem(9);
			inventory.delItem(1);
			MadSand.world.getCurLoc().addObject(x, y, stats.look, 6);
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
				inventory.delItem(id);
			}

		}
		if (Item.getType(id) == ItemType.PlaceableObject) {
			inventory.delItem(id);
			MadSand.world.getCurLoc().addObject(x, y, stats.look, Item.getAltObject(id));
		}
		if (Item.getType(id) == ItemType.PlaceableTile) {
			inventory.delItem(id);
			MadSand.world.getCurLoc().addTile(x, y, stats.look, Item.getAltObject(id));
		}
		checkHands(id);
	}

	public void freeHands(boolean silent) {
		if (!silent && isMain && stats.hand.id != Item.NULL_ITEM)
			MadSand.print("You put your " + stats.hand.name + " back to your inventory");
		super.freeHands();
		if (isMain)
			Gui.setHandDisplay(stats.hand.id);
	}

	@Override
	public void freeHands() {
		this.freeHands(false);
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
		freeHands();
		stats.spawnTime = MadSand.world.globalTick;

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
					GameSaver.loadLocation();
				} else {
					MadSand.world.Generate();
				}
			}
		}
		updCoords();
	}

	Direction lookAtMouse() {
		Direction dir;

		if (MadSand.wmx > x)
			dir = Direction.RIGHT;
		else if (MadSand.wmx < x)
			dir = Direction.LEFT;
		else if (MadSand.wmy > y)
			dir = Direction.UP;
		else
			dir = Direction.DOWN;

		turn(dir);
		return dir;
	}

	@Override
	public boolean move(Direction dir) {
		if (!MadSand.dialogClosed)
			return false;
		if (!super.move(dir))
			return false;
		if (isMain && (x == World.MAPSIZE - 1 || y == World.MAPSIZE - 1 || x == World.BORDER || y == World.BORDER)) {
			MadSand.print("Press [GRAY]N[WHITE] to move to the next sector.");
		}
		return true;
	}

	@Override
	public int tileDmg() {
		int tid = super.tileDmg();
		int dmg = TileProp.damage.getOrDefault(tid, 0);
		if (dmg > 0 && isMain)
			MadSand.print("You took " + dmg + " damage from " + (TileProp.name.get(tid)));
		return tid;
	}

	@Override
	boolean walk(Direction dir) {
		if (super.walk(dir)) {
			objectInFront();
			return true;
		} else
			return false;
	}

	public void objectInFront() {
		int obj = MadSand.world.getObjID(x, y, stats.look);
		if ((obj != 666) && (obj != 0)) {
			MadSand.print("You see: " + ObjectProp.name.get(obj));
		}
	}

	long getSurvivedTime() {
		return MadSand.world.globalTick - stats.spawnTime;
	}

	void hideInventory() {
		Utils.invBtnSetVisible(false);
		Gdx.input.setInputProcessor(Gui.overlay);
		Gui.contextMenuActive = false;
		MadSand.state = GameState.GAME;
		Gui.mousemenu.setVisible(true);
		inventory.inventoryUI.hide();
		Gui.inventoryActive = false;
		inventory.clearContextMenus();
	}

	void showInventory() {
		inventory.inventoryUI.toggleVisible();
		Gui.gamecontext.setVisible(false);
		Gui.contextMenuActive = false;
		Gui.mousemenu.setVisible(false);
		Utils.invBtnSetVisible(true);
		Gdx.input.setInputProcessor(Gui.overlay);
		MadSand.state = GameState.INVENTORY;
		Gui.inventoryActive = true;
	}
}