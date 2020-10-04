package hitonoriol.madsand.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import hitonoriol.madsand.GameSaver;
import hitonoriol.madsand.Gui;
import hitonoriol.madsand.LuaUtils;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.inventory.Inventory;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.entities.inventory.trade.TradeInventoryUI;
import hitonoriol.madsand.entities.quest.QuestWorker;
import hitonoriol.madsand.enums.*;
import hitonoriol.madsand.gui.dialogs.ProductionStationUI;
import hitonoriol.madsand.map.Loot;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.MapObject;
import hitonoriol.madsand.map.Tile;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.properties.NpcProp;
import hitonoriol.madsand.properties.ObjectProp;
import hitonoriol.madsand.properties.QuestList;
import hitonoriol.madsand.properties.TileProp;
import hitonoriol.madsand.world.World;

public class Player extends Entity {
	private Pair coords = new Pair();

	public HashSet<Integer> unlockedItems = new HashSet<Integer>(); // set of items player obtained at least once
	public ArrayList<Integer> craftRecipes = new ArrayList<Integer>(); // list of items which recipes are available to the player
	public QuestWorker quests = new QuestWorker();
	public HashSet<Integer> knownNpcs = new HashSet<Integer>();
	
	public HashSet<String> luaActions = new HashSet<>(); //Set for one-time lua actions

	@JsonProperty("newlyCreated")
	public boolean newlyCreated = true;

	@JsonIgnore
	public Player(String name) {
		super(name);
		super.setSprites(Resources.playerUpSpr, Resources.playerDownSpr, Resources.playerLeftSpr,
				Resources.playerRightSpr);
		stats.equipment.setIsPlayer(true);
		initInventory();
		setFov(fov);
		quests.setPlayer(this);
		Gui.overlay.equipmentSidebar.init();
	}

	public Player() {
		this("");
	}

	public void addExp(int amount) {
		stats.skills.increaseSkill(Skill.Level, amount);
	}

	@Override
	public void setFov(int val) {
		super.setFov(val);
		MadSand.setRenderRadius(val);
		if (MadSand.world != null)
			MadSand.world.updateLight();
	}

	@Override
	public Inventory initInventory() {
		super.initInventory();
		inventory.initUI();
		inventory.refreshUITitle();
		return inventory;
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
			stats.setHand(Item.nullItem);
			Gui.overlay.setHandDisplay(Item.nullItem);
			return;
		} else
			Gui.overlay.equipmentSidebar.refreshSlot(EquipSlot.MainHand);
	}

	@Override
	public boolean equip(Item item) {
		boolean ret = super.equip(item);

		if (ret)
			MadSand.print("You equip your " + item.name);

		return ret;
	}

	@Override
	public boolean attack(Direction dir) {
		boolean dead;
		turn(dir);
		Npc npc = MadSand.world.getCurLoc().getNpc(coords.set(x, y).addDirection(dir));

		if (npc == Map.nullNpc)
			return false;

		int atk = stats.calcAttack(npc.getDefense());

		if (atk == 0)
			MadSand.print("You miss " + npc.stats.name);
		else {
			MadSand.print("You deal " + atk + " damage to " + npc.stats.name);

			if (npc.friendly)
				npc.friendly = false;

			npc.damage(atk);
		}

		dead = npc.stats.dead;

		if (dead) {

			MadSand.print("You kill " + npc.stats.name);
			addExp(npc.rewardExp);

			if (knownNpcs.add(npc.id)) // If killed for the first time
				MadSand.print("You now know more about " + npc.stats.name + "s");

		}

		doAction(stats.AP_ATTACK);
		return dead;

	}

	public boolean attack() {
		return attack(stats.look);
	}

	@Override
	void die() {
		super.die();
		Gui.deathStage.setDeadText("You died\nYou survived " + getSurvivedTime() + " ticks");
		Gui.darkness.setVisible(true);
		Gdx.input.setInputProcessor(Gui.deathStage);
		MadSand.state = GameState.DEAD;
	}

	void damageHeldTool(Skill objectSkill) {
		if (inventory.damageTool(stats.hand(), objectSkill)) {
			MadSand.notice("Your " + stats.hand().name + " broke");
			inventory.delItem(stats.hand());
			freeHands(true);
		} else
			Gui.overlay.equipmentSidebar.refreshSlot(EquipSlot.MainHand);
	}

	public void refreshAvailableRecipes() {
		HashSet<Integer> reqs, all;

		for (Entry<Integer, ArrayList<Integer>> entry : ItemProp.craftReq.entrySet()) {
			reqs = new HashSet<Integer>(entry.getValue());
			all = new HashSet<Integer>(unlockedItems);
			int id = entry.getKey();

			if (reqs.contains(-1))
				continue;

			all.retainAll(reqs);

			if (all.equals(reqs) && !craftRecipes.contains(id)) {
				MadSand.notice("You figure out how to craft " + ItemProp.getItemName(id) + "!");
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

			if (item.name != "" && item.quantity > 0)
				MadSand.notice("You get " + item.quantity + " " + item.name);

			if (unlockedItems.add(item.id))
				refreshAvailableRecipes();

			return true;
		} else {
			MadSand.world.getCurLoc().putLoot(x, y, item);
			MadSand.notice("You can't carry any more items.");
			return false;
		}
	}

	public boolean craftItem(int id) {
		Item itemToCraft = ItemProp.items.get(id);
		int craftQuantity = ItemProp.getCraftQuantity(id);

		if (inventory.delItem(itemToCraft.recipe)) {
			increaseSkill(Skill.Crafting);

			int bonus = stats.skills.getItemReward(Skill.Crafting) - 1;
			int quantity = craftQuantity + bonus;

			Item item = new Item(id, quantity + bonus);

			addItem(item);

			Gui.drawOkDialog("Crafted " + quantity + " " + itemToCraft.name + " successfully!", Gui.craftMenu);
			MadSand.notice("You craft " + quantity + " " + itemToCraft.name);
			doAction(stats.AP_MINOR);
			return true;
		}

		Gui.drawOkDialog("Not enough resources to craft " + itemToCraft.name, Gui.craftMenu);

		return false;
	}

	public void interact() {
		Gui.overlay.hideActionBtn();
		interact(stats.look);
	}

	private void interact(Npc npc) {
		String name = npc.stats.name;
		ArrayList<Integer> questList = NpcProp.npcs.get(npc.id).questList;

		Gui.overlay.closeGameContextMenu();

		Utils.out("Interacting with NPC " + name + " type: " + npc.type.toString());

		switch (npc.type) {

		case Trader:
			tradeWithNpc(npc);
			return;

		case FarmAnimal:
			new ProductionStationUI(npc.animalProductWorker).show();
			return;

		case Regular:
			MadSand.print("Doesn't seem like " + name + " wants to talk.");
			return;

		case QuestMaster:
			int qid = quests.getAvailableQuest(questList);
			Utils.out("Got quest id: " + qid);
			npc.pause();

			if (qid == QuestList.NO_QUESTS_STATUS)
				MadSand.print(name + " has no more tasks for you.");
			else
				quests.processQuest(qid);
			return;

		default:
			break;

		}

		doAction(stats.AP_MINOR);
		Gui.overlay.processActionMenu();
	}

	public void tradeWithNpc(Direction direction) {
		coords.set(x, y).addDirection(direction);
		Npc npc = MadSand.world.getCurLoc().getNpc(coords);
		tradeWithNpc(npc);
	}

	public void tradeWithNpc(Npc npc) {

		Utils.out("Trade with npc: " + npc.stats.name);

		if (!npc.canTrade && !npc.type.equals(NpcType.Trader))
			return;

		new TradeInventoryUI(npc.inventory, inventory).show();
	}

	private void interact(Direction direction) {
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

		if (obj.isProductionStation) {
			new ProductionStationUI(loc.getProductionStation(coords)).show();
			return;
		}

		if (!action.equals(Resources.emptyField)) {
			LuaUtils.execute(action);
			return;
		}

		if (!loc.editable) {
			MadSand.notice("You try to interact with " + obj.name + "..." + Resources.LINEBREAK
					+ "But suddenly, you feel that it's protected by some mysterious force");
			return;
		}

		int item = MapObject.getAltItem(id, ItemProp.getType(stats.hand().id).get());
		int mhp = ObjectProp.getObject(obj.id).harvestHp;
		Skill skill = obj.skill;
		int curLvl = stats.skills.getLvl(skill);

		damageHeldTool(skill);

		if (curLvl < obj.lvl) {
			MadSand.notice(
					"You are not experienced enough." + Resources.LINEBREAK + skill + " level required: " + obj.lvl
							+ Resources.LINEBREAK + "Your " + skill + ": " + curLvl);
			return;
		}

		if (!stats.luckRoll()) {
			MadSand.print("You fail to interact with " + obj.name);
			return;
		}

		boolean destroyed = obj.takeDamage(stats.skills.getBaseSkillDamage(skill) + stats.hand().getSkillDamage(skill));

		if (item != -1 && destroyed) { // Succesfull interaction with item that drops something
			Item objLoot;
			int rewardCount;
			int rolls = stats.skills.getItemDropRolls(skill); // The higher the level of the skill, the more rolls of drop we make
			if (!stats.luckRoll() || !stats.skills.skillRoll(skill))
				rolls = 1;

			for (int i = 0; i < rolls; ++i) {
				rewardCount = stats.skills.getItemReward(skill);
				Utils.out("Rolling " + i + "/" + rolls + ": item id" + item + ", quantity: " + rewardCount);
				objLoot = new Item(item, rewardCount);
				addItem(objLoot);
				item = MapObject.getAltItem(id, ItemProp.getType(stats.hand().id).get());
			}

			increaseSkill(skill);
		}

		if (!destroyed)
			MadSand.print("You hit a " + obj.name + " [ " + obj.harvestHp + " / " + mhp + " ]");

		if (item == -1 && destroyed)
			MadSand.print("You damaged " + obj.name);

		MadSand.world.updateLight();
		Gui.overlay.processActionMenu();
	}

	public void useItem() {
		useItem(stats.hand());
	}

	public boolean useItem(Item item) {

		boolean itemUsed = false;
		checkHands(item.id);

		if (itemUsed = equip(item));
		
		else if (itemUsed = useGrabBag(item));
		
		else if (itemUsed = useTileInteractItem(item));
		
		else if (itemUsed = useScriptedItem(item));
		
		else if (itemUsed = useScriptedTile());
		
		else if (itemUsed = useConsumableItem(item));
		
		else if (itemUsed = plantCrop(item));
		
		else if (itemUsed = usePlaceableObject(item));

		else if (itemUsed = usePlaceableTile(item));

		checkHands(item.id);

		if (itemUsed) {
			damageHeldTool();
			doAction();
		}

		return itemUsed;

	}

	private boolean useGrabBag(Item item) {
		if (!item.type.equals(ItemType.GrabBag))
			return false;

		ArrayList<Item> items = item.contents.roll();
		for (Item rolledItem : items)
			addItem(rolledItem);
		
		inventory.delItem(item, 1);

		return true;
	}

	private boolean usePlaceableTile(Item item) {
		if (Item.getType(item.id).equals(ItemType.PlaceableTile)) {
			inventory.delItem(item, 1);
			MadSand.world.getCurLoc().addTile(x, y, stats.look, Item.getAltObject(item.id));
			return true;
		}

		return false;
	}

	private boolean usePlaceableObject(Item item) {
		if (Item.getType(item.id).equals(ItemType.PlaceableObject)) {
			inventory.delItem(item, 1);
			MadSand.world.getCurLoc().addObject(x, y, stats.look, Item.getAltObject(item.id));
			return true;
		}

		return false;
	}

	private boolean plantCrop(Item item) {
		if (Item.getType(item.id) == ItemType.Crop) {
			Pair coords = new Pair(x, y);
			if (MadSand.world.getCurLoc().putCrop(coords.x, coords.y, item.id)) {
				increaseSkill(Skill.Farming);
				MadSand.print("You plant " + item.name);
				inventory.delItem(item, 1);
			}
			return true;
		}

		return false;
	}

	private boolean useConsumableItem(Item item) {
		if (Item.getType(item.id).equals(ItemType.Consumable)) {
			increaseSkill(Skill.Survival);
			MadSand.print("You eat " + item.name);
			heal(item.healAmount);
			satiate(item.satiationAmount);
			inventory.delItem(item, 1);
			Gui.refreshOverlay();
			return true;
		}

		return false;
	}

	private boolean useScriptedTile() {
		String tileAction = TileProp.getOnInteract(MadSand.world.getTileId(x, y));
		if (!tileAction.equals(Resources.emptyField)) {
			LuaUtils.execute(tileAction);
			return true;
		}

		return false;
	}

	private boolean useScriptedItem(Item item) {
		String action = ItemProp.getOnUseAction(item.id);

		if (!action.equals(Resources.emptyField)) {
			LuaUtils.execute(action);
			if (item.type.equals(ItemType.Consumable))
				this.inventory.delItem(item, 1);
			checkHands(item.id);
			return true;
		}

		return false;
	}

	private boolean useTileInteractItem(Item item) {
		int ptile = MadSand.world.getTileId(x, y);
		int altItem = MapObject.getTileAltItem(ptile, item.type.get());

		if (altItem != -1) {
			MadSand.world.getCurLoc().delTile(x, y);
			Item gotItem = new Item(altItem);

			addItem(gotItem);

			MadSand.notice("You dig " + gotItem.name + " from the ground");
			increaseSkill(Skill.Digging);
			return true;
		}

		return false;
	}

	public void freeHands(boolean silent) {
		if (!silent && stats.hand().id != Item.NULL_ITEM)
			MadSand.print("You put your " + stats.hand().name + " back to your inventory");
		super.freeHands();
		Gui.overlay.setHandDisplay(stats.hand());
	}

	@Override
	public void freeHands() {
		this.freeHands(false);
	}

	public void respawn() {
		MadSand.world.curlayer = World.LAYER_OVERWORLD;
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
		if (isStepping())
			return stats.look;

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

	public Pair lookingAt() {
		return new Pair(x, y).addDirection(stats.look);
	}

	public Direction lookAtMouse(int x, int y) {
		return lookAtMouse(x, y, false);
	}

	@Override
	public void turn(Direction dir) {
		super.turn(dir);
		Gui.overlay.processActionMenu();
	}

	public boolean canTravel() {
		Map map = MadSand.world.getCurLoc();
		return (x == map.getWidth() - 1 || y == map.getHeight() - 1 || x == 0 || y == 0);
	}

	@Override
	public boolean move(Direction dir) {
		if (!super.move(dir))
			return false;

		Gui.overlay.processActionMenu();

		if ((MadSand.world.curlayer == World.LAYER_OVERWORLD) && canTravel())
			MadSand.print("Press [GRAY]N[WHITE] to travel to the next sector.");

		return true;
	}

	@Override
	public boolean rest() {
		boolean ret = super.rest();
		MadSand.world.ticks(1);
		MadSand.print("You rest for 1 turn");
		if (ret)
			MadSand.print("You feel well-rested");
		Gui.refreshOverlay();
		return ret;
	}

	@Override
	public int doAction(int ap) {
		int ticks = super.doAction(ap);
		MadSand.world.ticks(ticks); // committing our action and then letting everything catch up to time we've spent
		Gui.overlay.refreshOverlay();
		LuaUtils.execute(LuaUtils.onAction);
		return ticks;
	}

	@Override
	public int tileDmg() {
		int tid = super.tileDmg();
		final Tile tile = TileProp.getTileProp(tid);
		if (tile.damage > 0)
			MadSand.print("You took " + tile.damage + " damage from " + (tile.name));
		return tid;
	}

	@Override
	public boolean walk(Direction dir) {
		if (super.walk(dir)) {
			MadSand.world.updateLight();

			objectInFront();
			lootMsg();
			Gui.overlay.processActionMenu();
			return true;
		}

		return false;

	}

	public void attackHostile() {
		Npc npc = MadSand.world.getCurLoc().getNpc(lookingAt());

		if (npc.equals(Map.nullNpc))
			return;

		if (!npc.friendly) {
			attack();
			return;
		}
	}

	public void lootMsg() {
		if (standingOnLoot()) {
			Loot loot = MadSand.world.getCurLoc().getLoot(x, y);
			MadSand.print("You see [" + loot.getInfo() + "] lying on the floor");
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

	public void registerLuaAction(String name) {
		luaActions.add(name);
	}

	public boolean luaActionDone(String name) {
		return luaActions.contains(name);
	}

	public void hideInventory() {
		Gdx.input.setInputProcessor(Gui.overlay);
		Gui.gameResumeFocus();
		MadSand.state = GameState.GAME;
		inventory.inventoryUI.hide();
		Gui.inventoryActive = false;
		inventory.clearContextMenus();
		Gui.overlay.showTooltip();
	}

	public void showInventory() {
		inventory.inventoryUI.toggleVisible();
		Gui.overlay.gameContextMenu.setVisible(false);
		Gui.gameUnfocus();
		Gdx.input.setInputProcessor(Gui.overlay);
		MadSand.state = GameState.INVENTORY;
		Gui.inventoryActive = true;
		Gui.overlay.hideTooltip();
	}
}