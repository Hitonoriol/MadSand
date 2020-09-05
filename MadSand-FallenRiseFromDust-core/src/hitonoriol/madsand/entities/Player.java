package hitonoriol.madsand.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import hitonoriol.madsand.BuildScript;
import hitonoriol.madsand.GameSaver;
import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Quest;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.enums.*;
import hitonoriol.madsand.map.Loot;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.MapObject;
import hitonoriol.madsand.map.Tile;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.properties.ObjectProp;
import hitonoriol.madsand.properties.QuestList;
import hitonoriol.madsand.properties.TileProp;
import hitonoriol.madsand.world.World;

public class Player extends Entity {
	private Pair coords = new Pair();

	public HashSet<Integer> unlockedItems = new HashSet<Integer>(); // set of items player obtained at least once
	public ArrayList<Integer> craftRecipes = new ArrayList<Integer>(); // list of items which recipes are available to the player

	public HashSet<Integer> completedQuests = new HashSet<Integer>(); // sets of completed quests and the ones in progress
	public HashSet<Integer> questsInProgress = new HashSet<Integer>();

	public HashSet<Integer> knownNpcs = new HashSet<Integer>();

	@JsonProperty("newlyCreated")
	public boolean newlyCreated = true;

	public boolean isMain = true;

	@JsonIgnore
	public Player(String name) {
		super(name);
		super.setSprites(Resources.playerUpSpr, Resources.playerDownSpr, Resources.playerLeftSpr,
				Resources.playerRightSpr);
		initInventory();
		setFov(fov);
	}

	public Player() {
		this("");
	}

	@Override
	public void setFov(int val) {
		super.setFov(val);
		MadSand.setRenderRadius(val);
		if (MadSand.world != null)
			MadSand.world.updateLight();
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

	public void refreshAvailableRecipes() {
		HashSet<Integer> reqs;

		for (Entry<Integer, Vector<Integer>> entry : ItemProp.craftReq.entrySet()) {
			reqs = new HashSet<Integer>(entry.getValue());
			HashSet<Integer> all = new HashSet<Integer>(unlockedItems);
			int id = entry.getKey();

			if (reqs.contains(-1))
				continue;

			all.retainAll(reqs);

			if (all.equals(reqs) && !craftRecipes.contains(id)) {
				MadSand.notice("You figure out how to craft " + ItemProp.name.get(id) + "!");
				Utils.out("New recipe id: " + id + " unlocked! Adding to the list...");
				craftRecipes.add(id);
			}
		}
	}

	void damageHeldTool() {
		damageHeldTool(Skill.None);
	}

	@Override
	public boolean addItem(Item item) {
		if (super.addItem(item)) {
			if (item.name != "")
				MadSand.notice("You get " + item.quantity + " " + item.name);
			if (unlockedItems.add(item.id))
				refreshAvailableRecipes();
			return true;
		} else {
			MadSand.notice("You can't carry any more items.");
			return false;
		}
	}

	public boolean craftItem(int id) {
		if (inventory.delItem(ItemProp.recipe.get(id))) {
			increaseSkill(Skill.Crafting);
			int quantity = ItemProp.craftQuantity.get(id);
			int bonus = stats.skills.getItemReward(Skill.Crafting) - 1;
			Item item = new Item(id, quantity + bonus);
			if (!inventory.putItem(item)) {
				MadSand.world.getCurLoc().putLoot(x, y, item);
				MadSand.notice("You can't carry any more items");
			}
			Gui.drawOkDialog("Crafted " + quantity + " " + ItemProp.name.get(id) + " successfully!", Gui.craft);
			MadSand.notice("You craft " + quantity + " " + ItemProp.name.get(id));
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
		Utils.out("Trying to start quest id" + quest.id);
		if (isQuestCompleted(quest.id))
			return;
		if (inventory.putItem(quest.giveItems) != -1)
			MadSand.print("You get " + Item.queryToName(quest.giveItems));
		questsInProgress.add(quest.id);
		GameDialog.generateDialogChain(quest.startMsg, Gui.overlay).show();
	}

	private void completeQuest(Quest quest) {
		MadSand.notice("You completed a quest!");
		inventory.delItem(quest.reqItems);
		inventory.delItem(quest.removeOnCompletion);
		inventory.putItem(quest.rewardItems);
		stats.skills.increaseSkill(Skill.Level, quest.exp);
		MadSand.notice("You get " + quest.exp + " EXP!");
		if (!quest.repeatable)
			completedQuests.add(quest.id);
		questsInProgress.remove(quest.id);
		GameDialog.generateDialogChain(quest.endMsg, Gui.overlay).show();
	}

	public void processQuest(int id) {
		Utils.out("Processing quest " + id);
		Quest quest = QuestList.quests.get(id);
		if (isQuestInProgress(id) && !isQuestCompleted(id)) {
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
			Utils.out("quest " + qid + " inProgress: " + isQuestInProgress(qid) + " isCompleted: "
					+ isQuestCompleted(qid));
			if (isQuestInProgress(qid) || !isQuestCompleted(qid)) {
				return qid;
			}
		}
		return QuestList.NO_QUESTS_STATUS;
	}

	public void interact() {
		interact(stats.look);
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
		Gui.processActionMenu();
	}

	public void interact(final Direction direction) {
		coords.set(x, y).addDirection(direction);

		Map loc = MadSand.world.getCurLoc();
		MapObject obj = MadSand.world.getCurLoc().getObject(coords.x, coords.y);
		Npc npc = loc.getNpc(coords.x, coords.y);

		if (!npc.equals(Map.nullNpc)) {
			interact(npc);
			return;
		}

		int id = obj.id;

		if (id == 0)
			return;

		String action = ObjectProp.getOnInteract(id);
		doAction();

		if (!action.equals(Resources.emptyField)) {
			BuildScript.execute(action);
			return;
		}

		if (!loc.editable) {
			MadSand.notice("You try to interact with " + obj.name + "..." + Gui.LINEBREAK
					+ "But suddenly, you feel that it's protected by some mysterious force");
			return;
		}

		int item = MapObject.getAltItem(id, ItemProp.type.get(stats.hand.id).get());
		int mhp = ObjectProp.getObject(obj.id).harvestHp;
		Skill skill = obj.skill;
		int curLvl = stats.skills.getLvl(skill);

		damageHeldTool(skill);

		if (curLvl < obj.lvl) {
			MadSand.notice("You are not experienced enough." + Gui.LINEBREAK + skill + " level required: " + obj.lvl
					+ Gui.LINEBREAK + "Your " + skill + ": " + curLvl);
			return;
		}

		if (!stats.luckRoll()) {
			MadSand.print("You fail to interact with " + obj.name);
			return;
		}

		boolean destroyed = obj.takeDamage(stats.skills.getLvl(skill) + stats.hand.getSkillDamage(skill));

		if (item != -1 && destroyed) { // Succesfull interaction with item that drops something
			int rewardCount = stats.skills.getItemReward(skill);
			int rolls = stats.skills.getLvl(skill) + 1; // The higher the level of the skill, the more rolls of drop we
														// make
			if (!stats.luckRoll())
				rolls = 1;

			for (int i = 0; i < rolls; ++i) {
				Item objLoot = new Item(item, rewardCount);
				boolean gotItem = addItem(objLoot);
				if (!gotItem)
					MadSand.world.getCurLoc().putLoot(x, y, objLoot);
				item = MapObject.getAltItem(id, ItemProp.type.get(stats.hand.id).get());
			}

			increaseSkill(skill);
		}

		if (!destroyed)
			MadSand.print("You hit a " + obj.name + " [ " + obj.harvestHp + " / " + mhp + " ]");

		if (item == -1 && destroyed)
			MadSand.print("You damaged " + obj.name);

		MadSand.world.updateLight();
		Gui.processActionMenu();
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
			Item gotItem = new Item(item);
			if (!inventory.putItem(gotItem)) {
				MadSand.world.getCurLoc().putLoot(x, y, gotItem);
				MadSand.notice("You can't carry any more items");
			}
			MadSand.notice("You dig " + gotItem.name + " from the ground");
			increaseSkill(Skill.Digging);
		}
		String action = ItemProp.useAction.get(id);
		doAction();
		if (action != "-1") {
			BuildScript.execute(action);
			return;
		}
		String tileAction = TileProp.getOnInteract(ptile);
		if (tileAction != "-1") {
			BuildScript.execute(tileAction);
			return;
		}
		if (Item.getType(id) == ItemType.Consumable) {
			increaseSkill(Skill.Survival);
			MadSand.print("You eat " + ItemProp.name.get(id));
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
				MadSand.print("You plant " + new Item(id).name);
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
					MadSand.world.generate();
				}
			}
		}
		updCoords();
	}

	public Direction lookAtMouse(int x, int y, boolean diagonal) {
		Direction dir;

		if (x > this.x)
			dir = Direction.RIGHT;
		else if (x < this.x)
			dir = Direction.LEFT;
		else if (y > this.y)
			dir = Direction.UP;
		else
			dir = Direction.DOWN;

		if (diagonal) {
			if (dir == Direction.RIGHT && y > this.y)
				dir = Direction.UP_RIGHT;

			if (dir == Direction.RIGHT && y < this.y)
				dir = Direction.DOWN_RIGHT;

			if (dir == Direction.LEFT && y > this.y)
				dir = Direction.UP_LEFT;

			if (dir == Direction.LEFT && y < this.y)
				dir = Direction.DOWN_LEFT;
		}

		if (stats.look != dir)
			turn(dir);
		return dir;
	}

	public Direction lookAtMouse(int x, int y) {
		return lookAtMouse(x, y, false);
	}

	@Override
	public void turn(Direction dir) {
		super.turn(dir);
		Gui.processActionMenu();
	}

	@Override
	public boolean move(Direction dir) {
		if (!super.move(dir))
			return false;
		if (isMain && (MadSand.world.curlayer == World.LAYER_OVERWORLD)
				&& (x == World.MAPSIZE - 1 || y == World.MAPSIZE - 1 || x == World.BORDER || y == World.BORDER)) {
			MadSand.print("Press [GRAY]N[WHITE] to move to the next sector.");
		}
		Gui.processActionMenu();
		return true;
	}

	@Override
	public boolean rest() {
		boolean ret = super.rest();
		MadSand.world.ticks(1);
		MadSand.print("You rest for 1 turn");
		if (ret)
			MadSand.print("You feel well-rested");
		return ret;
	}

	@Override
	public int doAction(int ap) {
		int ticks = super.doAction(ap);
		MadSand.world.ticks(ticks); // committing our action and then letting everything catch up to time we've
									// spent
		Gui.refreshOverlay();
		return ticks;
	}

	@Override
	public int tileDmg() {
		int tid = super.tileDmg();
		final Tile tile = TileProp.getTileProp(tid);
		if (tile.damage > 0 && isMain)
			MadSand.print("You took " + tile.damage + " damage from " + (tile.name));
		return tid;
	}

	@Override
	public boolean walk(Direction dir) {
		if (super.walk(dir)) {
			MadSand.world.updateLight();
			objectInFront();
			lootMsg();
			Gui.processActionMenu();
			return true;
		} else
			return false;
	}

	public void lootMsg() {
		if (standingOnLoot()) {
			Loot loot = MadSand.world.getCurLoc().getLoot(x, y);
			MadSand.print("You see (" + loot.getInfo() + ") lying on the floor");
		}
	}

	public void objectInFront() {
		int obj = MadSand.world.getObjID(x, y, stats.look);
		if ((obj != MapObject.COLLISION_MASK_ID) && (obj != MapObject.NULL_OBJECT_ID)) {
			MadSand.print("You see: " + ObjectProp.getName(obj));
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