package hitonoriol.madsand.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.Random;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.Keyboard;
import hitonoriol.madsand.LuaUtils;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.dialog.DialogChainGenerator;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.inventory.CraftWorker;
import hitonoriol.madsand.entities.inventory.Inventory;
import hitonoriol.madsand.entities.inventory.item.Consumable;
import hitonoriol.madsand.entities.inventory.item.CropSeeds;
import hitonoriol.madsand.entities.inventory.item.FishingBait;
import hitonoriol.madsand.entities.inventory.item.GrabBag;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.inventory.item.Placeable;
import hitonoriol.madsand.entities.inventory.item.Projectile;
import hitonoriol.madsand.entities.inventory.item.Tool;
import hitonoriol.madsand.entities.inventory.item.Weapon;
import hitonoriol.madsand.entities.inventory.trade.TradeInventoryUI;
import hitonoriol.madsand.entities.npc.FarmAnimal;
import hitonoriol.madsand.entities.npc.AbstractNpc;
import hitonoriol.madsand.entities.npc.QuestMaster;
import hitonoriol.madsand.entities.npc.Npc;
import hitonoriol.madsand.entities.npc.Trader;
import hitonoriol.madsand.entities.quest.Quest;
import hitonoriol.madsand.entities.quest.QuestWorker;
import hitonoriol.madsand.enums.*;
import hitonoriol.madsand.gui.dialogs.FishingUI;
import hitonoriol.madsand.gui.dialogs.GrabBagDialog;
import hitonoriol.madsand.gui.dialogs.ProductionStationUI;
import hitonoriol.madsand.gui.dialogs.TraderDialog;
import hitonoriol.madsand.gui.dialogs.WaitDialog;
import hitonoriol.madsand.gui.widgets.ResourceProgressBar;
import hitonoriol.madsand.map.FishingSpot;
import hitonoriol.madsand.map.Loot;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.MapEntity;
import hitonoriol.madsand.map.Tile;
import hitonoriol.madsand.map.object.MapObject;
import hitonoriol.madsand.map.object.ResourceObject;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.properties.NpcProp;
import hitonoriol.madsand.properties.ObjectProp;
import hitonoriol.madsand.properties.TileProp;
import hitonoriol.madsand.world.Location;
import hitonoriol.madsand.world.WorkerType;

public class Player extends Entity {
	@JsonIgnore
	public PlayerStats stats; // Reference to the same Stats object as super.stats
	float elapsedTime;// For player animation

	public int targetedByNpcs = 0;
	public HashSet<Integer> unlockedItems = new HashSet<>(); // set of items player obtained at least once
	public ArrayList<Integer> craftRecipes = new ArrayList<>(); // list of items which recipes are available to the player
	public ArrayList<Integer> buildRecipes = new ArrayList<>();
	public QuestWorker quests = new QuestWorker();
	public HashSet<String> luaActions = new HashSet<>(); // Set for one-time lua actions
	public HashMap<Integer, Integer> killCount = new HashMap<>();
	public Reputation reputation = new Reputation();
	public ArrayList<Integer> abilities = new ArrayList<>();
	public int settlementsEstablished = 0;

	private TimedAction scheduledAction;

	@JsonProperty("newlyCreated")
	public boolean newlyCreated = true;

	@JsonIgnore
	public Player(String name) {
		super(name);
		stats = stats();
		super.setSprites(Resources.playerUpSpr, Resources.playerDownSpr, Resources.playerLeftSpr,
				Resources.playerRightSpr);
		initInventory();
		setFov(fov);
		quests.setPlayer(this);
		Gui.overlay.equipmentSidebar.init();
	}

	public Player() {
		this("");
	}

	public PlayerStats stats() {
		return (PlayerStats) super.stats();
	}

	public void refreshEquipment() {
		Gui.overlay.equipmentSidebar.refresh();
	}

	@JsonIgnore
	public void setName(String name) {
		super.setName(name);
	}

	public void unTarget() {
		--targetedByNpcs;
	}

	public void target() {
		++targetedByNpcs;
	}

	@JsonIgnore
	public boolean isTargeted() {
		return targetedByNpcs > 0;
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

	public void increaseSkill(Skill skill, int by) {
		if (by < 1)
			by = 1;
		stats.skills.increaseSkill(skill, by);

		if (!skill.isFightingSkill())
			stats.skills.increaseSkill(Skill.Level);
	}

	public void increaseSkill(Skill skill) {
		increaseSkill(skill, 1);
	}

	public void checkHands(int id) {
		int itemIdx = inventory.getSameCell(id);
		if (itemIdx == -1) {
			if (stats.hand().id == id)
				stats.equipment.unEquip(EquipSlot.MainHand);
			else if (stats.offHand().id == id)
				stats.equipment.unEquip(EquipSlot.Offhand);
			return;
		} else {
			Gui.overlay.equipmentSidebar.refreshSlot(EquipSlot.MainHand);
			Gui.overlay.equipmentSidebar.refreshSlot(EquipSlot.Offhand);
		}
	}

	public void equip(Item item) {
		item.equip(this);
		MadSand.print("You equip " + item.name);
	}

	public boolean unEquip(Item item) {
		MadSand.print("You unequip " + item.name);
		return stats.equipment.unEquip(item);
	}

	protected void attack(MapObject object, int dmg) {
		if (dmg == 0)
			MadSand.print("You hit " + object.name + " but deal no damage");
		else
			MadSand.print("You deal " + dmg + " damage to " + object.name);
		super.attack(object, dmg);
	}

	private void attack(AbstractNpc npc, int dmg) {
		Map map = MadSand.world.getCurLoc();

		npc.provoke();
		if (dmg == 0)
			MadSand.print("You miss " + npc.stats.name);
		else {
			MadSand.print("You deal " + dmg + " damage to " + npc.stats.name);
			damageHeldTool();
		}

		if (npc.stats.dead) {
			MadSand.notice("You kill " + npc.stats.name + "! [+" + npc.rewardExp + " exp]");
			addExp(npc.rewardExp);
			reputation.change(npc.stats.faction, Reputation.KILL_PENALTY);

			if (addToKillCount(npc.id)) // If killed for the first time
				MadSand.print("You now know more about " + npc.stats.name + "s");

			if (!map.editable && map.getHostileNpcCount() == 0 && MadSand.world.isUnderGround()) {
				MadSand.print("The curse of the dungeon has been lifted!" + Resources.LINEBREAK
						+ "You can now break objects on this floor of the dungeon.");
				map.editable = true;
			}
		}
	}

	@Override
	protected void attack(MapEntity target, int dmg) {
		super.attack(target, dmg);

		if (target instanceof AbstractNpc)
			attack((AbstractNpc) target, dmg);
	}

	public boolean canPerformRangedAttack() {
		Optional<Projectile> projectile = stats.getEquippedProjectile();
		Optional<Weapon> rangedWeapon = stats.getEquippedWeapon();
		if (!projectile.isPresent())
			return false;

		return projectile.get().thrownByHand
				|| (rangedWeapon.isPresent() && rangedWeapon.get().type == Weapon.Type.RangedWeapon);
	}

	private void performRangedAttack(AbstractNpc npc) {
		stats.getEquippedProjectile().ifPresent(projectile -> {
			super.rangedAttack(npc, projectile);
			damageHeldTool();
			delItem(projectile, 1);
		});
	}

	public void rangedAttack(AbstractNpc npc) {
		if (!canPerformRangedAttack())
			return;

		doAction(stats.rangedAttackCost, () -> performRangedAttack(npc));
	}

	private void performMeleeAttack(Direction dir) {
		turn(dir);
		Map map = MadSand.world.getCurLoc();
		AbstractNpc npc = map.getNpc(coords.set(x, y).addDirection(dir));

		if (npc == Map.nullNpc)
			return;

		int atk = stats.calcMeleeAttack(npc.getDefense());
		attack((MapEntity) npc, atk);

		if (atk > 0)
			stats.skills.increaseSkill(Skill.Melee);
	}

	@Override
	public void meleeAttack(Direction dir) {
		doAction(stats.meleeAttackCost, () -> performMeleeAttack(dir));
	}

	public int getKillCount(int id) {
		return killCount.getOrDefault(id, 0);
	}

	@JsonIgnore
	public int getKillCount() {
		int totalKills = 0;
		for (int id : killCount.keySet())
			totalKills += getKillCount(id);
		return totalKills;
	}

	public boolean addToKillCount(int id) {
		boolean first = killCount.containsKey(id);
		killCount.put(id, getKillCount(id) + 1);
		return !first;
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

	public void changeStamina(float by) {
		stats.stamina += by;
		stats.check();
		Gui.overlay.refresh();
	}

	public boolean knowsNpc(int id) {
		return killCount.containsKey(id);
	}

	public void meleeAttack() {
		meleeAttack(stats.look);
	}

	@Override
	protected void die() {
		super.die();
		stats.equipment.unEquipAll();
		refreshEquipment();
		MadSand.switchScreen(MadSand.deathScreen);
		MadSand.warn("You died");
	}

	void damageHeldTool(Skill objectSkill) {
		stats.getEquippedTool().ifPresent(heldTool -> {
			if (inventory.damageTool(heldTool, objectSkill)) {
				MadSand.notice("Your " + heldTool.name + " broke");

				unEquip(heldTool);

				inventory.delItem(heldTool);
				freeHands(true);
			} else
				Gui.overlay.equipmentSidebar.refreshSlot(EquipSlot.MainHand);
		});
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
		return unlocked + "/" + totalRecipes + " (" + Utils.round(100 * ((float) unlocked / (float) totalRecipes))
				+ "%)";

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

	public void damageHeldTool() {
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

	@Override
	public void delItem(Item item, int quantity) {
		super.delItem(item, quantity);
		if (stats.equipment.itemEquipped(item)) {
			checkHands(item.id);
			stats.equipment.refreshUI();
		}
	}

	public boolean addItem(List<Item> items) {
		for (Item item : items)
			addItem(item);
		return true;
	}

	private boolean performCraftItem(CraftWorker craftWorker, int quantity) {
		Item craftedItem;
		if ((craftedItem = craftWorker.craftItem(quantity)) != Item.nullItem) {
			increaseSkill(Skill.Crafting, quantity);

			Gui.drawOkDialog("Crafted " + craftedItem.quantity + " " + craftedItem.name + " successfully!");
			MadSand.print("You craft " + craftedItem.quantity + " " + craftedItem.name);

			int bonus = stats.luckRoll() ? Utils.rand(stats.skills.getItemReward(Skill.Crafting)) : 0;
			if (bonus > 0) {
				MadSand.notice("You manage to craft " + bonus + " extra " + craftedItem.name);
				addItem(Item.create(craftedItem.id, bonus));
			}
			return true;
		}

		return false;
	}

	public void craftItem(CraftWorker craftWorker, int quantity) {
		doAction(stats.minorCost, () -> performCraftItem(craftWorker, quantity));
	}

	public void interact() {
		Gui.overlay.hideActionBtn();
		interact(stats.look);
	}

	public void interact(QuestMaster questMaster) {
		questMaster.pause();
		if (!quests.processQuests(questMaster.questList, questMaster))
			MadSand.print(questMaster.stats.name + " has no more quests for you.");
	}

	public void interact(Trader trader) {
		if (trader.canGiveQuests)
			new TraderDialog(this, trader).show();
		else
			new TradeInventoryUI(trader.inventory, inventory).show();
	}

	public void interact(Npc npc) {
		talkToNpc(npc);
	}

	public void interact(FarmAnimal animal) {
		new ProductionStationUI(animal.animalProduct).show();
	}

	public void interact(AbstractNpc npc) {
		npc.interact(this);
		Gui.overlay.closeGameContextMenu();
		Gui.overlay.hideActionBtn();
	}

	private void talkToNpc(AbstractNpc npc) {
		int currency = Globals.getInt(Globals.CURRENCY);
		Location location = MadSand.world.getLocation();
		if (!npc.stats.faction.isHuman()) {
			MadSand.print("Doesn't seem like " + npc.stats.name + " can talk");
			return;
		}
		String dialogTitle = npc.stats.name;
		if (location.isSettlement()) {
			WorkerType occupation = location.settlement.getOccupation(npc.uid);
			dialogTitle += ((occupation != null) ? " (" + occupation.name() + ")" : "");
		}
		GameDialog npcDialog = new DialogChainGenerator(Utils.randElement(Globals.instance().idleNpcText))
				.setAllTitles(dialogTitle).generate(Gui.overlay);

		npcDialog.getProceedButton().setText("Goodbye");

		if (npc.canGiveQuests) {
			TextButton questButton = new TextButton("Do you need any help?", Gui.skin);
			npcDialog.addButton(questButton);
			questButton.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					npcDialog.remove();
					quests.startProceduralQuest(npc.uid);
				}
			});
		}

		if (location.isPlayerOwnedSettlement()) {
			int hireCost = location.settlement.getHireCost();
			TextButton recruitButton = new TextButton("Will you work for me? " + Quest.OBJECTIVE_COLOR + "[[" + hireCost
					+ " " + ItemProp.getItemName(currency) + "s]" + Resources.COLOR_END, Gui.skin);
			if (!location.settlement.isOccupied(npc.uid))
				npcDialog.addButton(recruitButton);

			recruitButton.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					npcDialog.remove();
					WorkerType worker = location.settlement.recruitWorker(npc.uid);
					if (!canAfford(hireCost)) {
						Gui.drawOkDialog("You don't have enough money to recruit this worker!");
						return;
					}
					inventory.delItem(currency, hireCost);
					new DialogChainGenerator(
							"Sure. I'll be the best " + worker.name() + " of the " + location.name + " settlement!")
									.setAllTitles(npc.stats.name).generate(Gui.overlay).show();
				}
			});
		}

		npcDialog.show();
	}

	private void fish(FishingSpot spot) {
		if (!stats.offHand().is(FishingBait.class)) {
			MadSand.notice("You don't have any bait equipped!");
			return;
		}

		new FishingUI(spot).show();
	}

	private void performInteraction(Direction direction) {
		coords.set(x, y).addDirection(direction);

		Map loc = MadSand.world.getCurLoc();
		MapObject obj = loc.getObject(coords.x, coords.y);
		AbstractNpc npc = loc.getNpc(coords.x, coords.y);
		Tile tileInFront = loc.getTile(coords.x, coords.y);

		if (npc != Map.nullNpc) {
			interact(npc);
			return;
		}

		if (stats.isToolEquipped(Tool.Type.FishingRod) && tileInFront.hasFishingSpot())
			fish(tileInFront.fishingSpot);

		obj.interact(this);
		MadSand.world.updateLight();
	}

	public void interact(ResourceObject resourceObj) {
		if (stats.stamina <= 0) {
			MadSand.print("You are too tired to gather resources. Try resting a little.");
			return;
		}

		if (getObjectResource(resourceObj) != -1)
			startResourceGathering(resourceObj);
		else
			MadSand.print("You can't do anything with " + resourceObj.name + " using your current tool");
	}

	private void interact(Direction direction) {
		doAction(stats.minorCost, () -> performInteraction(direction));
	}

	public void startResourceGathering(MapObject object) {
		if (object.harvestHp > 0)
			new ResourceProgressBar(object).start();
		else
			gatherResources(object);
	}

	public int gatherResources(MapObject obj) {
		if (obj.equals(Map.nullObject))
			return -1;

		doAction(stats.minorCost);
		damageHeldTool(obj.getInteractionSkill());
		changeStamina(-stats.calcStaminaCost());

		return obj.acceptHit(this);
	}

	private int getObjectResource(MapObject object) {
		return object.as(ResourceObject.class)
				.map(resourceObj -> resourceObj.rollDrop(stats.getEquippedToolType()))
				.orElse(-1);
	}

	public void useItem() {
		useItem(stats.hand());
	}

	public void useItem(Item item) {
		doAction(stats.minorCost, () -> {
			if (useScriptedTile())
				return;

			item.use(this);
			checkHands(item.id);
		});
	}

	public void useItem(GrabBag item) {
		ArrayList<Item> items = item.contents.rollItems();
		for (Item rolledItem : items)
			addItem(rolledItem);

		inventory.delItem(item, 1);
		new GrabBagDialog(item.name, items).show();

		if (items.isEmpty())
			MadSand.warn("You opened " + item.name + ", but it was empty");
	}

	public void useItem(Placeable item) {
		Map map = MadSand.world.getCurLoc();
		if (item.type == Placeable.Type.Object)
			map.addObject(x, y, stats.look, item.altObject);
		else
			map.addTile(x, y, stats.look, item.altObject);

		inventory.delItem(item, 1);
	}

	public void useItem(CropSeeds item) {
		Pair coords = new Pair(x, y);
		if (MadSand.world.getCurLoc().putCrop(coords.x, coords.y, item.id)) {
			increaseSkill(Skill.Farming);
			MadSand.print("You plant " + item.name);
			inventory.delItem(item, 1);
		}
	}

	public void useItem(Consumable item) {
		increaseSkill(Skill.Survival);
		MadSand.print("You eat " + item.name);
		stats.foodTicks += item.getNutritionalValue();
		heal(item.healAmount);
		satiate(item.satiationAmount);
		changeStamina(item.staminaAmount);
		inventory.delItem(item, 1);
		Gui.refreshOverlay();
	}

	private boolean useScriptedTile() {
		String tileAction = TileProp.getOnInteract(MadSand.world.getTileId(x, y));
		if (!tileAction.equals(Resources.emptyField)) {
			LuaUtils.execute(tileAction);
			return true;
		}

		return false;
	}

	public void useItem(Tool item) {
		changeStamina(-item.weight);
		damageHeldTool();

		if (item.type == Tool.Type.Shovel) {
			int ptile = MadSand.world.getTileId(x, y);
			int altItem = MapObject.rollTileResource(ptile, item.type);

			if (altItem != -1) {
				MadSand.world.getCurLoc().delTile(x, y);
				Item gotItem = Item.create(altItem);

				addItem(gotItem);

				MadSand.notice("You dig " + gotItem.name + " from the ground");
				increaseSkill(Skill.Digging);
			}
		}
	}

	public void freeHands(EquipSlot slot, boolean silent) {
		Item item = stats.equipment.getItem(slot);
		if (!silent && item.id != Item.NULL_ITEM)
			MadSand.print("You put " + item.name + " back to your inventory");
		stats.equipment.unEquip(slot);
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

	private void rest(int ticks, boolean verbose) {
		double ap = stats.actionPts;
		stats.actionPts = stats.actionPtsMax;
		MadSand.world.timeTick(ticks);
		MadSand.world.timeSubtick(getActionLength(ap + (ticks - 1) * getSpeed()));
		Gui.refreshOverlay();

		if (verbose)
			MadSand.print("You rest a bit");
	}

	public void rest() {
		rest(1, true);
	}

	public void skipTicks(int ticks) {
		if (ticks > 1)
			MadSand.world.startTimeSkip();
		rest(ticks, false);
	}

	private void skipTime(int ticks, BooleanSupplier skipCondition) { // Skips <ticks> ticks while skipCondition is true
		MadSand.world.startTimeSkip();
		int step = Utils.largestDivisor(ticks);
		for (int i = 0; i < ticks; i += step) {
			if (!skipCondition.getAsBoolean())
				break;

			skipTicks(step);
		}
	}

	public void restFully() {
		int ticksToRest = (int) ((stats.maxstamina - stats.stamina) / stats.getStaminaRegenRate());

		if (ticksToRest == 0) {
			MadSand.notice("Your stamina is already full");
			return;
		}

		skipTime(ticksToRest, () -> {
			if (isTargeted()) {
				MadSand.warn("It's no time to rest. A hostile creature is nearby!");
				return false;
			}

			if (!stats.isSatiated()) {
				MadSand.warn("You are too hungry to rest...");
				return false;
			}

			return true;
		});

		MadSand.notice("Rested for " + Utils.timeString(MadSand.world.toWorldTimeSeconds(ticksToRest)));
	}

	public static float TIMESKIP_COEF = 18f, REALTIME_SKIP_COEF = 5.5f;

	public void skipTime() {
		int timeSkipItemId = Globals.getInt(Globals.TIMESKIP_ITEM);
		Item timeSkipItem = inventory.getItem(timeSkipItemId);
		if (timeSkipItem.equals(Item.nullItem)) {
			Gui.drawOkDialog(
					"You need at least 1 " + ItemProp.getItemName(timeSkipItemId) + " to be able to skip time.");
			return;
		}

		int maxTimeSkip = (int) (timeSkipItem.quantity * TIMESKIP_COEF);
		new WaitDialog(maxTimeSkip).show();
	}

	public int doAction(double ap, TimedAction action) {
		if (scheduledAction != null)
			return -1;

		int ticks = super.doAction(ap);
		scheduledAction = action;
		MadSand.world.timeTick(ticks); // committing our action and then letting world catch up to time we've spent
		MadSand.world.timeSubtick(getActionLength(ap)); // letting NPCs catch up
		Gui.overlay.refresh();
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
			doAction(stats.walkCost, () -> performWalk(dir));
		return true;
	}

	@Override
	public void act(float time) {
		commitAction();
	}

	public void attackHostile() {
		AbstractNpc npc = MadSand.world.getCurLoc().getNpc(lookingAt());

		if (npc.equals(Map.nullNpc))
			return;

		if (!npc.friendly) {
			meleeAttack();
			return;
		}
	}

	public void damage(int to) {
		super.damage(to);
		Gui.refreshOverlay();
	}

	public void lootMsg() {
		if (standingOnLoot()) {
			Loot loot = MadSand.world.getCurLoc().getLoot(x, y);
			MadSand.print("You see [" + loot.getInfo() + "] lying on the floor");
		}
	}

	@JsonIgnore
	public int getLvl() {
		return stats.skills.getLvl();
	}

	static int SETTLEMENT_COST = 500;
	static int SETTLEMENT_RES_COST = 20;

	@JsonIgnore
	public ArrayList<Item> getSettlementCreationReq() {
		ArrayList<Item> items = new ArrayList<>();
		// Require coins
		int creationCost = (settlementsEstablished + 1) * SETTLEMENT_COST;
		items.add(Item.create(Globals.getInt(Globals.CURRENCY), creationCost));

		// Not-so-random material of tier #<settlementsEstablished>
		Item requiredResource = Item.create(
				NpcProp.tradeLists.getTradeItemList(TradeCategory.Materials, settlementsEstablished)
						.getRandomId(new Random(settlementsEstablished)));
		requiredResource.quantity = SETTLEMENT_RES_COST * (settlementsEstablished + 1);
		items.add(requiredResource);

		return items;
	}

	public void objectInFront() {
		int obj = MadSand.world.getObjID(x, y, stats.look);
		if ((obj != MapObject.COLLISION_MASK_ID) && (obj != MapObject.NULL_OBJECT_ID))
			MadSand.print("You see: " + ObjectProp.getName(obj));
	}

	@JsonIgnore
	public TextureRegion getSprite() {
		if (!isStepping())
			return super.getSprite();

		Animation<TextureRegion> anim = null;
		elapsedTime += Gdx.graphics.getDeltaTime();

		if (stats.look == Direction.RIGHT)
			anim = Resources.ranim;
		else if (stats.look == Direction.LEFT)
			anim = Resources.lanim;
		else if (stats.look == Direction.UP)
			anim = Resources.uanim;
		else
			anim = Resources.danim;

		return anim.getKeyFrame(elapsedTime, true);
	}

	public long getSurvivedTime() {
		return MadSand.world.toWorldTimeSeconds(MadSand.world.globalTick - stats.spawnTime);
	}

	public void registerLuaAction(String name) {
		luaActions.add(name);
	}

	public boolean luaActionDone(String name) {
		return luaActions.contains(name);
	}

	public void hideInventory() {
		inventory.inventoryUI.hide();
	}

	public void showInventory() {
		inventory.inventoryUI.show();
	}

	@JsonGetter("equipment")
	public ArrayList<Integer> getEquipment() {
		return stats.equipment.getIndexList(inventory);
	}

	@JsonSetter("equipment")
	public void setEquipment(ArrayList<Integer> list) {
		stats.setOwner(this);
		for (int itemIdx : list)
			equip(inventory.getItemByIndex(itemIdx));
	}

	@JsonSetter("stats")
	public void setStats(PlayerStats stats) {
		super.stats = this.stats = stats;
	}

	@JsonGetter("stats")
	public PlayerStats getStats() {
		return stats;
	}

	private interface TimedAction {
		void act();
	}
}