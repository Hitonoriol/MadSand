package hitonoriol.madsand.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.Keyboard;
import hitonoriol.madsand.LuaUtils;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.dialog.DialogChainGenerator;
import hitonoriol.madsand.entities.inventory.Inventory;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.entities.inventory.trade.TradeInventoryUI;
import hitonoriol.madsand.entities.quest.QuestWorker;
import hitonoriol.madsand.enums.*;
import hitonoriol.madsand.gui.dialogs.FishingUI;
import hitonoriol.madsand.gui.dialogs.ProductionStationUI;
import hitonoriol.madsand.gui.dialogs.TraderDialog;
import hitonoriol.madsand.gui.widgets.ResourceProgressBar;
import hitonoriol.madsand.map.FishingSpot;
import hitonoriol.madsand.map.Loot;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.MapObject;
import hitonoriol.madsand.map.Tile;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.properties.NpcProp;
import hitonoriol.madsand.properties.ObjectProp;
import hitonoriol.madsand.properties.TileProp;
import hitonoriol.madsand.world.Location;

public class Player extends Entity {

	public HashSet<Integer> unlockedItems = new HashSet<Integer>(); // set of items player obtained at least once
	public ArrayList<Integer> craftRecipes = new ArrayList<Integer>(); // list of items which recipes are available to the player
	public ArrayList<Integer> buildRecipes = new ArrayList<Integer>();
	public QuestWorker quests = new QuestWorker();
	public HashSet<String> luaActions = new HashSet<>(); //Set for one-time lua actions
	public HashMap<Integer, Integer> killCount = new HashMap<>();
	public Reputation reputation = new Reputation();

	private TimedAction scheduledAction;

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

	public void refreshEquipment() {
		Gui.overlay.equipmentSidebar.refresh();
	}

	public Player() {
		this("");
	}

	@JsonIgnore
	public void setName(String name) {
		super.setName(name);
	}

	public void joinFaction(Faction faction) {
		if (stats.faction != Faction.None)
			return;

		stats.faction = faction;
		if (reputation.get(faction) < 0)
			reputation.set(faction, 0);
		
		MadSand.print("You join " + faction.name());
	}

	public void leaveFaction(Faction faction) {
		float rep = reputation.get(faction);
		reputation.change(faction, rep - (rep * Reputation.LEAVE_PENALTY));
		stats.faction = Faction.None;
		
		MadSand.notice("You leave " + faction.name());
	}

	public void commitAction() {
		if (scheduledAction != null)
			scheduledAction.act();
		scheduledAction = null;
	}

	public void addExp(int amount) {
		stats.skills.increaseSkill(Skill.Level, amount);
		Gui.refreshOverlay();
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

	void increaseSkill(Skill skill, int by) {
		if (by < 1)
			by = 1;
		stats.skills.increaseSkill(skill, by);
		stats.skills.increaseSkill(Skill.Level);
	}

	void increaseSkill(Skill skill) {
		increaseSkill(skill, 1);
	}

	public void checkHands(int id) {
		int itemIdx = inventory.getSameCell(id);
		if (itemIdx == -1) {
			if (stats.hand().id == id)
				stats.setHand(Item.nullItem);
			else if (stats.offHand().id == id)
				stats.equipment.unEquip(EquipSlot.Offhand);
			return;
		} else
			Gui.overlay.equipmentSidebar.refreshSlot(EquipSlot.MainHand);
	}

	@Override
	public boolean equip(Item item) {
		boolean ret = super.equip(item);

		if (ret)
			MadSand.print("You equip " + item.name);
		else
			takeInHand(item);

		return ret;
	}

	public void takeInHand(Item item) {
		EquipSlot slot = item.type.handSlot();
		Item prevHand = stats.equipment.getItem(slot);
		stats.equipment.equip(slot, item);

		if (!prevHand.equals(Item.nullItem))
			inventory.refreshItem(prevHand);

		if (!prevHand.equals(item))
			MadSand.print("You take " + item.name + " in your hand");
		else
			freeHands(slot);

	}

	public boolean unEquip(Item item) {
		MadSand.print("You unequip " + item.name);
		return stats.equipment.unEquip(item);
	}

	private void performAttack(Direction dir) {
		boolean dead;
		turn(dir);
		Map map = MadSand.world.getCurLoc();
		Npc npc = map.getNpc(coords.set(x, y).addDirection(dir));

		if (npc == Map.nullNpc)
			return;

		int atk = stats.calcAttack(npc.getDefense());
		npc.provoke();

		if (atk == 0)
			MadSand.print("You miss " + npc.stats.name);
		else {
			MadSand.print("You deal " + atk + " damage to " + npc.stats.name);

			super.attackAnimation(npc);
			npc.damage(atk);
			stats.skills.increaseSkill(Skill.Melee);
		}

		dead = npc.stats.dead;

		if (dead) {

			MadSand.notice("You kill " + npc.stats.name + "! [+" + npc.rewardExp + " exp]");
			addExp(npc.rewardExp);
			reputation.change(npc.stats.faction, Reputation.KILL_PENALTY);

			if (addToKillCount(npc.id)) // If killed for the first time
				MadSand.print("You now know more about " + npc.stats.name + "s");

			if (map.getHostileNpcCount() == 0 && MadSand.world.isUnderGround()) {
				MadSand.print("The curse of the dungeon has been lifted!" + Resources.LINEBREAK +
						"You can now break objects on this floor of the dungeon.");
				map.editable = true;
			}

		}
	}

	@Override
	public void attack(Direction dir) {
		doAction(stats.attackCost, new TimedAction() {
			@Override
			public void act() {
				performAttack(dir);
			}
		});
	}

	public int getKillCount(int id) {
		return killCount.getOrDefault(id, 0);
	}

	public boolean addToKillCount(int id) {
		boolean first = killCount.containsKey(id);
		killCount.put(id, getKillCount(id) + 1);
		return !first;
	}

	public boolean knowsNpc(int id) {
		return killCount.containsKey(id);
	}

	public void attack() {
		attack(stats.look);
	}

	@Override
	void die() {
		super.die();
		stats.equipment.unEquipAll();
		refreshEquipment();
		Gui.deathStage.setDeadText("You died\nYou survived " + Utils.round(getSurvivedTime()) + " hours");
		Gui.darkness.setVisible(true);
		Gdx.input.setInputProcessor(Gui.deathStage);
		MadSand.state = GameState.DEAD;
		MadSand.warn("You died");
	}

	void damageHeldTool(Skill objectSkill) {
		if (inventory.damageTool(stats.hand(), objectSkill)) {
			MadSand.notice("Your " + stats.hand().name + " broke");
			inventory.delItem(stats.hand());
			freeHands(true);
		} else
			Gui.overlay.equipmentSidebar.refreshSlot(EquipSlot.MainHand);
	}

	private void unlockRecipe(ArrayList<Integer> recipeList, int recipe) {
		if (recipeList.contains(recipe))
			return;

		if (recipeList == craftRecipes)
			MadSand.notice("You figure out how to craft " + ItemProp.getItemName(recipe) + "!");
		else if (recipeList == buildRecipes)
			MadSand.notice("You now know how to build " + ObjectProp.getName(recipe) + "!");
		recipeList.add(recipe);
	}

	private boolean unlockRandomRecipe(HashMap<Integer, ArrayList<Integer>> reqMap, ArrayList<Integer> recipes) {
		Set<Integer> availableRecipes;

		if (recipes == craftRecipes)
			availableRecipes = new HashSet<>(craftRecipes);
		else
			availableRecipes = new HashSet<>(buildRecipes);

		List<Integer> notUnlocked = new ArrayList<>(reqMap.keySet());
		notUnlocked.removeAll(availableRecipes);

		if (notUnlocked.isEmpty())
			return false;

		unlockRecipe(recipes, Utils.randElement(notUnlocked));
		return true;
	}

	private String getRecipeProgress(ArrayList<Integer> recipeList) {
		int unlocked = recipeList.size();
		int totalRecipes = 0;
		if (recipeList == craftRecipes)
			totalRecipes = ItemProp.craftReq.size();
		else if (recipeList == buildRecipes)
			totalRecipes = ItemProp.buildReq.size();
		return unlocked + "/" + totalRecipes + " ("
				+ Utils.round(100 * ((float) unlocked / (float) totalRecipes)) + "%)";

	}

	public void unlockRandomBuildRecipe() {
		if (!unlockRandomRecipe(ItemProp.buildReq, buildRecipes))
			MadSand.notice("No recipes were unlocked. You already know everything!");
	}

	public String craftRecipeProgress() {
		return getRecipeProgress(craftRecipes);
	}

	public String buildRecipeProgress() {
		return getRecipeProgress(buildRecipes);
	}

	public void unlockCraftRecipe(int recipe) {
		unlockRecipe(craftRecipes, recipe);
	}

	public void unlockBuildRecipe(int recipe) {
		unlockRecipe(buildRecipes, recipe);
	}

	private void refreshRecipes(HashMap<Integer, ArrayList<Integer>> reqMap, ArrayList<Integer> recipes) {
		HashSet<Integer> reqs, all;

		for (Entry<Integer, ArrayList<Integer>> entry : reqMap.entrySet()) {
			reqs = new HashSet<Integer>(entry.getValue());
			all = new HashSet<Integer>(unlockedItems);
			int id = entry.getKey();

			if (reqs.contains(-1))
				continue;

			all.retainAll(reqs);

			if (all.equals(reqs))
				unlockRecipe(recipes, id);

		}
	}

	public void refreshAvailableRecipes() {
		refreshRecipes(ItemProp.craftReq, craftRecipes);
		refreshRecipes(ItemProp.buildReq, buildRecipes);
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

	public boolean addItem(ArrayList<Item> items) {
		for (Item item : items)
			addItem(item);
		return true;
	}

	private boolean performCraftItem(int id) {
		Item itemToCraft = ItemProp.items.get(id);
		int craftQuantity = ItemProp.getCraftQuantity(id);

		if (inventory.itemsExist(itemToCraft.recipe)) {
			increaseSkill(Skill.Crafting);
			inventory.delItem(itemToCraft.recipe);

			int bonus = stats.skills.getItemReward(Skill.Crafting) - 1;
			int quantity = craftQuantity + bonus;

			Item item = new Item(id, quantity + bonus);

			addItem(item);

			Gui.drawOkDialog("Crafted " + quantity + " " + itemToCraft.name + " successfully!", Gui.craftMenu);
			MadSand.notice("You craft " + quantity + " " + itemToCraft.name);
			return true;
		}

		Gui.drawOkDialog("Not enough resources to craft " + itemToCraft.name, Gui.craftMenu);

		return false;
	}

	public void craftItem(int id) {
		doAction(stats.minorCost, new TimedAction() {

			@Override
			public void act() {
				performCraftItem(id);
			}
		});
	}

	public void interact() {
		Gui.overlay.hideActionBtn();
		interact(stats.look);
	}

	private void interact(Npc npc) {
		String name = npc.stats.name;
		Faction faction = npc.stats.faction;
		ArrayList<Integer> questList = NpcProp.npcs.get(npc.id).questList;

		Gui.overlay.closeGameContextMenu();
		Gui.overlay.hideActionBtn();

		switch (npc.type) {

		case Trader:
			new TraderDialog(this, npc).show();
			break;

		case FarmAnimal:
			new ProductionStationUI(npc.animalProductWorker).show();
			break;

		case Regular:
			if (faction == Faction.Animals || faction == Faction.Monsters)
				MadSand.print("Doesn't seem like " + name + " wants to talk.");
			else {
				if (stats.luckRoll()) {
					quests.startProceduralQuest(npc.uid);
				} else {
					new DialogChainGenerator("#" + npc.stats.name + "#" +
							Utils.randElement(Globals.instance().idleNpcText))
									.generate(Gui.overlay).show();
				}
			}
			break;

		case QuestMaster:
			npc.pause();
			if (!quests.processQuests(questList, npc))
				MadSand.print(name + " has no more quests for you.");
			break;

		default:
			break;

		}
	}

	public void tradeWithNpc(Direction direction) {
		coords.set(x, y).addDirection(direction);
		Npc npc = MadSand.world.getCurLoc().getNpc(coords);
		tradeWithNpc(npc);
	}

	public void tradeWithNpc(Npc npc) {

		if (!npc.canTrade && !npc.type.equals(NpcType.Trader))
			return;

		new TradeInventoryUI(npc.inventory, inventory).show();
	}

	private void fish(FishingSpot spot) {
		if (stats.offHand().type != ItemType.FishingBait) {
			MadSand.notice("You don't have any bait equipped!");
			return;
		}

		new FishingUI(spot).show();
	}

	private void performInteraction(Direction direction) {
		coords.set(x, y).addDirection(direction);

		Map loc = MadSand.world.getCurLoc();
		MapObject obj = MadSand.world.getCurLoc().getObject(coords.x, coords.y);
		Npc npc = loc.getNpc(coords.x, coords.y);
		Tile tileInFront = loc.getTile(coords.x, coords.y);

		if (!npc.equals(Map.nullNpc)) {
			interact(npc);
			return;
		}

		if (stats.hand().type == ItemType.FishingRod && tileInFront.hasFishingSpot())
			fish(tileInFront.fishingSpot);

		if (obj.id == Map.nullObject.id)
			return;

		String action = ObjectProp.getOnInteract(obj.id);

		if (obj.isCraftingStation) {
			Gui.openCraftMenu(obj.id);
			return;
		}
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

		if (stats.stamina <= 0) {
			MadSand.print("You are too tired to gather resources. Try resting a little.");
			return;
		}

		if (getObjectResource(obj.id) != -1) {
			if (obj.harvestHp > 0)
				new ResourceProgressBar(obj).start();
			else
				gatherResources(obj);
		} else
			MadSand.print("You can't gather any resources from " + obj.name + " with your current tool");

		MadSand.world.updateLight();
	}

	private void interact(Direction direction) {
		doAction(stats.minorCost, new TimedAction() {
			@Override
			public void act() {
				performInteraction(direction);
			}
		});
	}

	private float BASE_RES_FAIL = 40; // Base resource gathering fail probability
	// returns amount of damage done to object's harvestHp or negative value on fail

	public int gatherResources(MapObject obj) {
		if (obj.id == Map.nullObject.id)
			return -1;

		int item = getObjectResource(obj.id);
		int mhp = ObjectProp.getObject(obj.id).harvestHp;
		Skill skill = obj.skill;
		int curLvl = stats.skills.getLvl(skill);

		if (curLvl < obj.lvl) {
			MadSand.notice(
					"You are not experienced enough." + Resources.LINEBREAK + skill + " level required: " + obj.lvl
							+ Resources.LINEBREAK + "Your " + skill + ": " + curLvl);
			return -2;
		}

		doAction(stats.minorCost);
		damageHeldTool(skill);
		changeStamina(-stats.calcStaminaCost());

		if (Utils.percentRoll(BASE_RES_FAIL) && !stats.skills.skillRoll(skill) && !stats.luckRoll()) {
			MadSand.print("You fail to interact with " + obj.name);
			return -1;
		}

		int damage = stats.skills.getBaseSkillDamage(skill) + stats.hand().getSkillDamage(skill);
		boolean damaged = obj.takeDamage(damage);

		if (item != -1 && damaged) { // Succesfull interaction with item that drops something
			Item objLoot;
			int rewardCount;
			int rolls = stats.skills.getItemDropRolls(skill); // The higher the level of the skill, the more rolls of drop we make
			if (!stats.luckRoll() || !stats.skills.skillRoll(skill))
				rolls = 1;

			for (int i = 0; i < rolls; ++i) {
				rewardCount = stats.skills.getItemReward(skill);
				objLoot = new Item(item, rewardCount);
				addItem(objLoot);
				item = MapObject.getAltItem(obj.id, stats.hand().type);
			}

			increaseSkill(skill, obj.lvl);
		}

		if (!damaged)
			MadSand.print("You hit " + obj.name + " [ " + obj.harvestHp + " / " + mhp + " ]");

		if (item == -1 && damaged && obj.id != Map.nullObject.id)
			MadSand.print("You damaged " + obj.name);

		return damage;
	}

	private int getObjectResource(int objectId) {
		return MapObject.getAltItem(objectId, stats.hand().type);
	}

	public void useItem() {
		useItem(stats.hand());
	}

	private void performUseItem(Item item) {
		boolean itemUsed = false;
		checkHands(item.id);

		if (stats.equipment.getItem(EquipSlot.slotByTypeAll(item.type)) != item)
			itemUsed = equip(item);

		itemUsed |= useGrabBag(item);
		itemUsed |= useTileInteractItem(item);
		itemUsed |= useScriptedItem(item);
		itemUsed |= useScriptedTile();
		itemUsed |= useConsumableItem(item);
		itemUsed |= plantCrop(item);
		itemUsed |= usePlaceableObject(item);
		itemUsed |= usePlaceableTile(item);

		checkHands(item.id);

		if (itemUsed) {
			damageHeldTool();

			if (!item.type.isConsumable())
				changeStamina(-item.weight);
		}
	}

	public void useItem(Item item) {
		doAction(stats.minorCost, new TimedAction() {
			@Override
			public void act() {
				performUseItem(item);
			}
		});

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
			changeStamina(item.staminaAmount);
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
		int altItem = MapObject.getTileAltItem(ptile, item.type);

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

	public void freeHands(EquipSlot slot, boolean silent) {
		Item item = stats.equipment.getItem(slot);
		if (!silent && item.id != Item.NULL_ITEM)
			MadSand.print("You put " + item.name + " back to your inventory");
		stats.equipment.equip(slot, Item.nullItem);
		inventory.refreshItem(item);
	}

	public void freeHands(EquipSlot slot) {
		freeHands(slot, false);
	}

	public void freeHands(boolean silent) {
		this.freeHands(EquipSlot.MainHand, silent);
	}

	public void freeHands() {
		this.freeHands(EquipSlot.MainHand, false);
	}

	public void respawn() {
		MadSand.world.setLayer(Location.LAYER_OVERWORLD);
		MadSand.world.getCurWPos();
		int wx = MadSand.world.wx();
		int wy = MadSand.world.wy();
		Map map = MadSand.world.getCurLoc();
		MadSand.state = GameState.GAME;
		stats.food = stats.maxFood;
		stats.actionPts = stats.actionPtsMax;
		stats.hp = stats.mhp;
		stats.stamina = stats.maxstamina;
		stats.dead = false;
		freeHands();
		stats.spawnTime = MadSand.world.globalTick;

		if (stats.hasRespawnPoint) {
			if (stats.respawnWX == wx && stats.respawnWY == wy) {
				x = stats.respawnX;
				y = stats.respawnY;
			} else {
				MadSand.world.switchLocation(stats.respawnWX, stats.respawnWY, Location.LAYER_OVERWORLD);
			}
		} else {
			x = Utils.rand(0, map.getWidth());
			y = Utils.rand(0, MadSand.world.getCurLoc().getHeight());
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

	public MapObject objectLookingAt() {
		return MadSand.world.getCurLoc().getObject(x, y, stats.look);
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
		Direction direction = stats.look;
		boolean canTravel = false;

		canTravel = (x == map.getWidth() - 1 && direction == Direction.RIGHT);
		canTravel |= (y == map.getHeight() - 1 && direction == Direction.UP);
		canTravel |= (x < 1 && direction == Direction.LEFT);
		canTravel |= (y < 1 && direction == Direction.DOWN);

		return canTravel;
	}

	@Override
	public boolean move(Direction dir) {
		if (!super.move(dir))
			return false;

		if ((MadSand.world.curLayer() == Location.LAYER_OVERWORLD) && canTravel())
			MadSand.print("Press [GRAY]N[WHITE] to travel to the next sector.");

		return true;
	}

	@Override
	public void rest() {
		double ap = stats.actionPts;
		super.rest();
		MadSand.world.timeTick(1);
		MadSand.world.timeSubtick(getActionLength(ap));
		MadSand.print("You rest a bit");
		Gui.refreshOverlay();
	}

	public int doAction(double ap, TimedAction action) {
		if (scheduledAction != null)
			return -1;

		int ticks = super.doAction(ap);
		scheduledAction = action;
		MadSand.world.timeTick(ticks); // committing our action and then letting world catch up to time we've spent
		MadSand.world.timeSubtick(getActionLength(ap)); // letting NPCs catch up
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

	private void performWalk(Direction dir) {
		if (!super.walk(dir))
			return;

		MadSand.world.updateLight();
		objectInFront();
		lootMsg();
	}

	@Override
	public boolean walk(Direction dir) {

		if (Keyboard.inputIgnored())
			return false;

		if (canWalk(dir))
			doAction(stats.walkCost, new TimedAction() {
				@Override
				public void act() {
					performWalk(dir);
				}
			});
		return true;
	}

	@Override
	public void act(float time) {
		commitAction();
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

	public void damage(int to) {
		super.damage(to);
		Gui.refreshOverlay();
	}

	public void changeStamina(float by) {
		super.changeStamina(by);
		Gui.overlay.refreshOverlay();
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

	float getSurvivedTime() {
		return (float) (MadSand.world.globalTick - stats.spawnTime) / (float) MadSand.world.ticksPerHour;
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

	private interface TimedAction {
		void act();
	}
}