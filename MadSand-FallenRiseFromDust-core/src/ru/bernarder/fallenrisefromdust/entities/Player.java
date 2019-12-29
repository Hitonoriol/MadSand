package ru.bernarder.fallenrisefromdust.entities;

import java.util.ArrayList;
import java.util.HashSet;

import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.annotation.JsonIgnore;

import ru.bernarder.fallenrisefromdust.BuildScript;
import ru.bernarder.fallenrisefromdust.GameSaver;
import ru.bernarder.fallenrisefromdust.Gui;
import ru.bernarder.fallenrisefromdust.MadSand;
import ru.bernarder.fallenrisefromdust.Quest;
import ru.bernarder.fallenrisefromdust.Resources;
import ru.bernarder.fallenrisefromdust.Utils;
import ru.bernarder.fallenrisefromdust.containers.Pair;
import ru.bernarder.fallenrisefromdust.dialog.GameDialog;
import ru.bernarder.fallenrisefromdust.entities.inventory.Item;
import ru.bernarder.fallenrisefromdust.enums.*;
import ru.bernarder.fallenrisefromdust.map.Map;
import ru.bernarder.fallenrisefromdust.map.MapObject;
import ru.bernarder.fallenrisefromdust.properties.ItemProp;
import ru.bernarder.fallenrisefromdust.properties.ObjectProp;
import ru.bernarder.fallenrisefromdust.properties.QuestList;
import ru.bernarder.fallenrisefromdust.properties.TileProp;
import ru.bernarder.fallenrisefromdust.world.World;

public class Player extends Entity {
	private Pair coords = new Pair();

	public HashSet<Integer> unlockedItems = new HashSet<Integer>(); // set of items player obtained at least once
	public ArrayList<Integer> craftRecipes = new ArrayList<Integer>(); // list of items which recipes are available to
																		// the player

	public HashSet<Integer> completedQuests = new HashSet<Integer>(); // sets of completed quests and the ones in
																		// progress. all the quests are already loaded
																		// in QuestList.quests, so we only need to store
																		// the ids
	public HashSet<Integer> questsInProgress = new HashSet<Integer>();

	public HashSet<Integer> knownNpcs = new HashSet<Integer>();

	public boolean newlyCreated = true;

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

	@Override
	void initInventory() {
		super.initInventory();
		inventory.initUI();
		inventory.refreshUITitle();
	}

	@JsonIgnore
	public boolean isNewlyCreated() {
		if (newlyCreated) {
			newlyCreated = false;
			return true;
		}
		return false;
	}

	public boolean knowsNpc(int id) {
		return knownNpcs.contains(id);
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
	public boolean equip(Item item) {
		boolean ret = super.equip(item);
		if (ret) {
			Gui.refreshEquipDisplay();
			MadSand.print("You equip your " + item.name);
		}
		return ret;
	}

	@Override
	public boolean attack(Direction dir) {
		boolean dead;
		turn(dir);
		Npc npc = MadSand.world.getCurLoc().getNpc(coords.set(x, y).addDirection(dir));
		if (npc == Map.nullNpc)
			return false;
		else {
			int atk = stats.calcAttack();
			if (atk == 0)
				MadSand.print("You miss " + npc.stats.name);
			else {
				MadSand.print("You deal " + atk + " damage to " + npc.stats.name);
				if (npc.friendly)
					npc.friendly = false;
				npc.damage(atk);
			}
			dead = npc.stats.dead;
			if (dead && knownNpcs.add(npc.id))
				MadSand.print("You now know more about " + npc.stats + "s");
			doAction(stats.AP_ATTACK);
			return dead;
		}
	}

	public boolean attack() {
		return attack(stats.look);
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

	@Override
	public boolean addItem(Item item) {
		if (super.addItem(item)) {
			MadSand.print("You get " + Item.queryToName(item.id + "/" + item.quantity));
			Utils.out("Got item id: " + item.id + "; quantity: " + item.quantity);
			Utils.out("For the first time: " + unlockedItems.add(item.id));
			return true;
		} else
			return false;
	}

	public boolean craftItem(int id) {
		if (inventory.delItem(ItemProp.recipe.get(id))) {
			increaseSkill(Skill.Crafting);
			inventory.putItem(id, stats.skills.getItemReward(Skill.Crafting));
			if (isMain)
				Gui.drawOkDialog("Crafted " + ItemProp.name.get(id), Gui.craft);
			doAction(stats.AP_MINOR);
			return true;
		}
		if (isMain)
			Gui.drawOkDialog("Not enough resources to craft " + ItemProp.name.get(id), Gui.craft);
		return false;
	}

	public boolean isQuestInProgress(int id) {
		return questsInProgress.contains(id);
	}

	public boolean isQuestCompleted(int id) {
		return completedQuests.contains(id);
	}

	private void startQuest(Quest quest) {
		inventory.putItem(quest.giveItems);
		questsInProgress.add(quest.id);
		GameDialog.generateDialogChain(quest.startMsg, Gui.overlay).show();
	}

	private void completeQuest(Quest quest) {
		MadSand.print("You completed a quest!");
		inventory.delItem(quest.reqItems);
		inventory.delItem(quest.removeOnCompletion);
		inventory.putItem(quest.rewardItems);
		stats.skills.increaseSkill(Skill.Level, quest.exp);
		MadSand.print("You get " + quest.exp + " EXP!");
		if (!quest.repeatable)
			completedQuests.add(quest.id);
		questsInProgress.remove(quest.id);
		GameDialog.generateDialogChain(quest.endMsg, Gui.overlay).show();
	}

	public void processQuest(int id) {
		Quest quest = QuestList.quests.get(id);
		if (isQuestInProgress(id)) {
			if (inventory.itemsExist(quest.reqItems))
				completeQuest(quest);
			else
				GameDialog.generateDialogChain(quest.reqMsg, Gui.overlay).show();
		} else
			startQuest(quest);
	}

	public int getAvailableQuest(ArrayList<Integer> quests) { // search NPC's quest list for {not yet started / not
																// finished / repeatable} quests
		for (int qid : quests) {
			if (isQuestInProgress(qid) || !isQuestCompleted(qid))
				return qid;
		}
		return QuestList.NO_QUESTS_STATUS;
	}

	public void interact(Npc npc) {
		String name = npc.stats.name;
		Gui.closeGameContextMenu();
		Utils.out("Interacting with NPC " + name + " type: " + npc.type.toString());
		switch (npc.type) {
		case Regular:
			MadSand.print("Doesn't seem like " + name + " wants to talk.");
			return;
		case QuestMaster:
			int qid = getAvailableQuest(npc.questList);
			Utils.out("Got quest id: " + qid);
			npc.pause();
			if (qid == QuestList.NO_QUESTS_STATUS)
				MadSand.print(name + " has no more tasks for you.");
			else
				processQuest(qid);
			break;

		default:
			break;

		}
		doAction(stats.AP_MINOR);
	}

	public void interact(final Direction direction) {
		coords.set(x, y).addDirection(direction);

		Map loc = MadSand.world.getCurLoc();
		Npc npc = loc.getNpc(coords.x, coords.y);
		if (npc != Map.nullNpc) {
			interact(npc);
			return;
		}
		int id = loc.getObject(coords.x, coords.y).id;

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
			addItem(item, stats.skills.getItemReward(skill));
			increaseSkill(skill);
		}
		if (!destroyed)
			MadSand.print("You hit a " + obj.name + " [ " + obj.harverstHp + " / " + mhp + " ]");
		if (item == -1 && destroyed)
			MadSand.print("You damaged " + obj.name);
	}

	public void useItem(Item item) {
		if (inventory.getSameCell(item) == -1)
			return;
		stats.hand = item;
		Gui.setHandDisplay(item.id);
		useItem();
	}

	public void useItem() {
		int id = stats.hand.id;
		if (equip(stats.hand))
			return;
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

	public Direction lookAtMouse(int x, int y) {
		Direction dir;

		if (x > this.x)
			dir = Direction.RIGHT;
		else if (x < this.x)
			dir = Direction.LEFT;
		else if (y > this.y)
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
		if (isMain && (MadSand.world.curlayer == World.LAYER_OVERWORLD)
				&& (x == World.MAPSIZE - 1 || y == World.MAPSIZE - 1 || x == World.BORDER || y == World.BORDER)) {
			MadSand.print("Press [GRAY]N[WHITE] to move to the next sector.");
		}
		return true;
	}

	@Override
	public boolean rest() {
		boolean ret = super.rest();
		if (ret)
			MadSand.world.ticks(1);
		return ret;
	}

	@Override
	public int doAction(int ap) {
		int ticks = super.doAction(ap);
		MadSand.world.ticks(ticks); // committing our action and then letting everything catch up to time we've
									// spent
		return ticks;
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
	public boolean walk(Direction dir) {
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

	public void hideInventory() {
		Utils.invBtnSetVisible(false);
		Gdx.input.setInputProcessor(Gui.overlay);
		Gui.gameUnfocused = false;
		MadSand.state = GameState.GAME;
		Gui.mousemenu.setVisible(true);
		inventory.inventoryUI.hide();
		Gui.inventoryActive = false;
		inventory.clearContextMenus();
	}

	public void showInventory() {
		inventory.inventoryUI.toggleVisible();
		Gui.gamecontext.setVisible(false);
		Gui.gameUnfocused = false;
		Gui.mousemenu.setVisible(false);
		Utils.invBtnSetVisible(true);
		Gdx.input.setInputProcessor(Gui.overlay);
		MadSand.state = GameState.INVENTORY;
		Gui.inventoryActive = true;
	}

	@Override
	public void setEquipment(ArrayList<Integer> eq) { // For deserializer only
		super.setEquipment(eq);
		Gui.refreshEquipDisplay();
		Gui.setHandDisplay(stats.hand.id);
	}
}