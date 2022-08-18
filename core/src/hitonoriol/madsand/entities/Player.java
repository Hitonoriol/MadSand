package hitonoriol.madsand.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.Line;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.dialog.DialogChainGenerator;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.entities.ability.Ability;
import hitonoriol.madsand.entities.ability.ActiveAbility;
import hitonoriol.madsand.entities.equipment.EquipSlot;
import hitonoriol.madsand.entities.inventory.CraftWorker;
import hitonoriol.madsand.entities.inventory.Inventory;
import hitonoriol.madsand.entities.inventory.ItemUI;
import hitonoriol.madsand.entities.inventory.item.Consumable;
import hitonoriol.madsand.entities.inventory.item.CropSeeds;
import hitonoriol.madsand.entities.inventory.item.FishingBait;
import hitonoriol.madsand.entities.inventory.item.GrabBag;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.inventory.item.PlaceableItem;
import hitonoriol.madsand.entities.inventory.item.Projectile;
import hitonoriol.madsand.entities.inventory.item.Tool;
import hitonoriol.madsand.entities.inventory.item.Weapon;
import hitonoriol.madsand.entities.inventory.item.category.ItemCategories;
import hitonoriol.madsand.entities.inventory.item.category.ItemCategory;
import hitonoriol.madsand.entities.inventory.trade.TradeInventoryUI;
import hitonoriol.madsand.entities.npc.AbstractNpc;
import hitonoriol.madsand.entities.npc.FarmAnimal;
import hitonoriol.madsand.entities.npc.Npc;
import hitonoriol.madsand.entities.npc.QuestMaster;
import hitonoriol.madsand.entities.npc.Trader;
import hitonoriol.madsand.entities.quest.Quest;
import hitonoriol.madsand.entities.quest.QuestWorker;
import hitonoriol.madsand.entities.skill.Skill;
import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.gui.animation.SimpleAnimation;
import hitonoriol.madsand.gui.dialogs.FishingUI;
import hitonoriol.madsand.gui.dialogs.GrabBagDialog;
import hitonoriol.madsand.gui.dialogs.ItemFactoryUI;
import hitonoriol.madsand.gui.dialogs.TraderDialog;
import hitonoriol.madsand.gui.dialogs.WaitDialog;
import hitonoriol.madsand.gui.widgets.overlay.ResourceProgressBar;
import hitonoriol.madsand.input.Keyboard;
import hitonoriol.madsand.lua.Lua;
import hitonoriol.madsand.map.FishingSpot;
import hitonoriol.madsand.map.Loot;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.MapEntity;
import hitonoriol.madsand.map.Tile;
import hitonoriol.madsand.map.object.MapObject;
import hitonoriol.madsand.map.object.ResourceObject;
import hitonoriol.madsand.pathfinding.Path;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.properties.ObjectProp;
import hitonoriol.madsand.properties.TileProp;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.TimeUtils;
import hitonoriol.madsand.util.Utils;
import hitonoriol.madsand.world.Location;
import hitonoriol.madsand.world.WorkerType;
import hitonoriol.madsand.world.World;

public class Player extends Entity {
	@JsonIgnore
	public PlayerStats stats; // Reference to the same Stats object as super.stats
	private float runSpeedCoef = 3.5f;
	private final static float DEF_LUMINOSITY = 1.5f;
	private final static float MIN_ANIM_SPEED = 2f;

	private Set<Integer> unlockedItems = new HashSet<>(); // set of items player obtained at least once
	private List<Integer> craftRecipes = new ArrayList<>(); // list of items which recipes are available to the player
	private List<Integer> buildRecipes = new ArrayList<>();
	private QuestWorker quests = new QuestWorker();
	private Set<String> luaActions = new HashSet<>(); // Set for one-time lua actions
	private HashMap<Integer, Integer> killCount = new HashMap<>();
	private Reputation reputation = new Reputation();
	private List<Ability> abilities = new ArrayList<>();
	private LinkedHashMap<Integer, Integer> abilityKeyBinds = new LinkedHashMap<>();
	private int settlementsEstablished = 0;
	private Set<Pair> completedDungeons = new HashSet<>();
	@JsonIgnore
	private List<Pair> visibleArea = new ArrayList<>();

	private Runnable scheduledAction, afterMovement;

	@JsonProperty
	private boolean newlyCreated = true;

	static {
		loadPlayerAnimation();
	}

	@JsonIgnore
	public Player(String name) {
		super(name);
		stats = stats();
		super.setSprites(standingSprites);
		initInventory();
		setFov();
		quests.setPlayer(this);
		setLuminosity(DEF_LUMINOSITY);
		Optional.ofNullable(Gui.overlay).ifPresent(overlay -> overlay.equipmentSidebar.init());
	}

	public Player() {
		this("");
	}

	@Override
	public void postLoadInit() {
		inventory.initUI();
		initStatActions();
		quests.setPlayer(this);
		turn(stats.look);
		stats.equipment.refreshUI();
		abilityKeyBinds.forEach((key, abilityId) -> bindAbility(key, abilityId));
		inventory.refreshContents();
		refreshAvailableRecipes();
		setFov();
	}

	public void finishCreation() {
		newlyCreated = false;
	}

	public boolean uninitialized() {
		return newlyCreated;
	}

	private static final int ANIM_WIDTH = 35, ANIM_HEIGHT = 74;
	private static final float animDuration = 0.2f;
	private static Sprite[] standingSprites;
	private static SimpleAnimation[] walkAnim;

	private static void loadPlayerAnimation() {
		walkAnim = new SimpleAnimation[Direction.BASE_DIRECTIONS];
		standingSprites = new Sprite[Direction.BASE_DIRECTIONS];
		TextureRegion[][] animSheet = Resources.getTexture("player/anim").split(ANIM_WIDTH, ANIM_HEIGHT);

		Direction.forEachBase(direction -> {
			int dirIdx = direction.baseOrdinal();
			walkAnim[dirIdx] = new SimpleAnimation(animDuration, animSheet[dirIdx]);
			standingSprites[dirIdx] = new Sprite(animSheet[dirIdx][0]);
		});
	}

	@Override
	public boolean add(Map map, Pair coords) {
		return true;
	}

	public int getEstablishedSettlements() {
		return settlementsEstablished;
	}

	public void establishSettlement() {
		++settlementsEstablished;
	}

	public QuestWorker getQuestWorker() {
		return quests;
	}

	public List<Ability> getAbilities() {
		return abilities;
	}

	public HashMap<Integer, Integer> getTotalKillCount() {
		return killCount;
	}

	public Reputation getReputation() {
		return reputation;
	}

	public List<Integer> getBuildRecipes() {
		return buildRecipes;
	}

	public List<Integer> getCraftRecipes() {
		return craftRecipes;
	}

	public PlayerStats stats() {
		return (PlayerStats) super.stats();
	}

	public Ability getAbility(int id) {
		int idx = abilities.indexOf(Ability.get(id));
		if (idx == -1)
			return null;

		return abilities.get(idx);
	}

	public boolean addAbility(int id) {
		Ability ability = Ability.get(id);
		boolean firstTime = !abilities.contains(ability);

		Utils.out("Adding ability %d, first time: %b", id, firstTime);

		if (firstTime) {
			abilities.add(ability);
			MadSand.notice("You learn " + ability.name);
		} else {
			ability = abilities.get(abilities.indexOf(ability));
			if (ability.levelUp())
				MadSand.notice("Your %s is now Level %d!", ability.name, ability.lvl);
			else
				MadSand.notice("Your %s (Level %d) gets a bit better [%d/%d]",
						ability.name, ability.lvl, ability.exp, ability.getLevelUpRequirement());
		}

		return firstTime;
	}

	public boolean removeAbility(int id) {
		return abilities.remove(Ability.get(id));
	}

	public int getAbilityKey(int id) {
		return abilityKeyBinds.entrySet().stream()
				.filter(bind -> bind.getValue() == id)
				.findFirst()
				.map(bind -> bind.getKey())
				.orElse(-1);
	}

	public void bindAbility(int key, int abilityId) {
		int oldKey;
		if ((oldKey = getAbilityKey(abilityId)) != -1 && oldKey != key)
			unbindAbility(oldKey);

		if (abilityKeyBinds.getOrDefault(key, -1) != abilityId)
			abilityKeyBinds.put(key, abilityId);

		Gui.overlay.getHotbar().addEntry(MadSand.player().getAbility(abilityId).as(ActiveAbility.class).get());
		Keyboard.getKeyBindManager().bind(key, () -> MadSand.player().getAbility(abilityId).apply());
	}

	public void unbindAbility(int key) {
		abilityKeyBinds.remove(key);
		Keyboard.getKeyBindManager().unbind(key);
		Gui.overlay.getHotbar().refresh();
	}

	public void refreshEquipment() {
		Gui.overlay.equipmentSidebar.refresh();
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
			scheduledAction.run();
		scheduledAction = null;
	}

	public double addExp(double amount) {
		double coef = 1d + stats().baseStats.getEffectiveness(Stat.Intelligence) * 0.02;
		double actualExp = amount * coef;
		stats().skills.increaseSkill(Skill.Level, actualExp);
		Gui.refreshOverlay();
		return actualExp;
	}

	@Override
	public int getEffectiveFov() {
		return (int) (getFov() * 0.66);
	}

	@Override
	public void setFov(int val) {
		if (!visibleArea.isEmpty() && val == getFov())
			return;

		Utils.dbg("Player fov=%d", val);
		super.setFov(val);
		setVisibleArea(val);
		if (MadSand.world() != null)
			MadSand.world().updateLight();
	}

	public void setFov() {
		setFov((int) (Gdx.graphics.getWidth() / MadSand.TILESIZE / 1.65));
	}

	public void setVisibleArea(int radius) {
		final int center = radius, diameter = radius * 2;
		visibleArea.clear();
		for (int y = 0; y < diameter; ++y) {
			for (int x = 0; x < diameter; ++x) {
				if (Line.calcDistance(center, center, x, y) <= radius)
					visibleArea.add(new Pair(center - x, center - y));
			}
		}
	}

	@Override
	public void forEachInFov(BiConsumer<Integer, Integer> action) {
		for (Pair coords : visibleArea)
			action.accept(x + coords.x, y + coords.y);
	}

	@JsonIgnore
	public List<Pair> getVisibleArea() {
		return visibleArea;
	}

	@Override
	public Inventory initInventory() {
		super.initInventory();
		inventory.initUI();
		inventory.refreshUITitle();
		return inventory;
	}

	public void increaseSkill(Skill skill, double by) {
		stats().skills.increaseSkill(skill, by);

		if (skill.isResourceSkill())
			stats().skills.increaseSkill(Skill.Level, by * Skill.RES_EXP_COEF);
	}

	public void increaseSkill(Skill skill) {
		increaseSkill(skill, 1);
	}

	public void checkHands(int id) {
		int itemIdx = inventory.getIndex(id);
		if (itemIdx == -1) {
			if (stats.hand().id() == id)
				stats.equipment.unEquip(EquipSlot.MainHand);
			else if (stats.offHand().id() == id)
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
		if (item == Item.nullItem)
			return false;

		MadSand.print("You unequip " + item.name);

		boolean unEquipped = stats.equipment.unEquip(item);
		if (!unEquipped)
			MadSand.warn("You can't unequip " + item.name + " - it's cursed!");

		return unEquipped;
	}

	/* Melee Attack method for Ability scripts */
	private void performAttack(Pair coords, int dmg) {
		Map map = MadSand.world().getCurLoc();
		if (super.distanceTo(coords) > 1)
			return;

		if (map.isFreeTile(coords))
			return;

		attack(map.getMapEntity(coords), new Damage(dmg));
	}

	public void attack(Pair coords, int dmg) {
		doAction(stats.meleeAttackCost, () -> performAttack(coords, dmg));
	}

	public boolean canPerformRangedAttack() {
		Optional<Projectile> projectile = stats.getEquippedProjectile();
		Optional<Weapon> rangedWeapon = stats.getEquippedWeapon();
		if (!projectile.isPresent())
			return false;

		return projectile.get().thrownByHand
				|| (rangedWeapon.isPresent() && rangedWeapon.get().type == Weapon.Type.RangedWeapon);
	}

	private void performRangedAttack(Pair targetPos) {
		stats.getEquippedProjectile().ifPresent(projectile -> {
			super.rangedAttack(targetPos, projectile);
			damageHeldEquipment();
			delItem(projectile, 1);
		});
	}

	public void rangedAttack(Pair target) {
		if (!canPerformRangedAttack())
			return;

		doAction(stats.rangedAttackCost, () -> performRangedAttack(target));
	}

	private void performMeleeAttack(Direction dir) {
		turn(dir);
		Map map = MadSand.world().getCurLoc();
		AbstractNpc npc = map.getNpc(coords.set(x, y).addDirection(dir));

		if (npc.isEmpty())
			return;

		Damage damage = new Damage(this).melee(npc.getDefense());
		attack(npc, damage);

		if (!damage.missed())
			stats.skills.increaseSkill(Skill.Melee);
	}

	@Override
	public void meleeAttack(Direction dir) {
		if (isMoving())
			return;

		if (MadSand.world().getCurLoc().npcExists(coords.set(x, y).addDirection(dir)))
			doAction(stats.meleeAttackCost, () -> performMeleeAttack(dir));
		else
			turn(dir);
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

	public boolean addToKillCount(int id, int kills) {
		boolean first = killCount.containsKey(id);
		killCount.put(id, getKillCount(id) + kills);
		return !first;
	}

	public boolean addToKillCount(int id) {
		return addToKillCount(id, 1);
	}

	void satiate(int amt) {
		stats.food += amt;
		stats.check();
	}

	void increaseStamina(int to) {
		if (stats.stamina + to < stats.maxstamina)
			stats.stamina += to;
		else
			stats.stamina = stats.maxstamina;
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

	void damageHeldEquipment(MapEntity damagedEntity) {
		stats.getHeldEquipment().ifPresent(heldTool -> {
			if (heldTool.damage()) {
				MadSand.warn("Your " + heldTool.name + " broke");

				unEquip(heldTool);

				inventory.delItem(heldTool);
				freeHands(true);
			} else
				Gui.overlay.equipmentSidebar.refreshSlot(EquipSlot.MainHand);
		});
	}

	public void damageHeldEquipment() {
		damageHeldEquipment(Map.nullObject);
	}

	private boolean unlockRecipe(List<Integer> recipeList, int recipe) {
		if (recipeList.contains(recipe))
			return false;

		if (recipeList == craftRecipes)
			MadSand.notice("You figure out how to craft " + ItemProp.getItemName(recipe) + "!");
		else if (recipeList == buildRecipes)
			MadSand.notice("You now know how to build " + ObjectProp.getName(recipe) + "!");
		recipeList.add(recipe);

		return true;
	}

	private boolean unlockRandomRecipe(java.util.Map<Integer, List<Integer>> reqMap, List<Integer> recipes) {
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

	private String getRecipeProgress(List<Integer> recipeList) {
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
		showItemUnlockNotification(Arrays.asList(recipe));
	}

	public void unlockBuildRecipe(int recipe) {
		unlockRecipe(buildRecipes, recipe);
	}

	private void refreshRecipes(java.util.Map<Integer, List<Integer>> reqMap, List<Integer> recipeList) {
		boolean itemRecipe = recipeList == craftRecipes;
		List<Integer> newlyUnlockedItems = new ArrayList<>();
		reqMap.forEach((itemId, craftReqs) -> {
			if (itemRecipe && !ItemProp.getItem(itemId).isRecipeUnlockable())
				return;

			Set<Integer> itemCraftReqs = new HashSet<>(craftReqs);
			if (itemCraftReqs.contains(-1))
				return;

			Set<Integer> curUnlockedItems = new HashSet<>(unlockedItems);
			curUnlockedItems.retainAll(itemCraftReqs);

			if (curUnlockedItems.equals(itemCraftReqs) && unlockRecipe(recipeList, itemId))
				newlyUnlockedItems.add(itemId);
		});

		if (newlyUnlockedItems.isEmpty())
			return;

		if (itemRecipe)
			showItemUnlockNotification(newlyUnlockedItems);
	}

	private void showItemUnlockNotification(List<Integer> unlockedItems) {
		Table newItems = ItemUI.createItemList(unlockedItems.stream()
				.map(id -> ItemProp.getItem(id)).collect(Collectors.toList()));
		Gui.drawOkDialog("You now know how to craft following items:")
				.addContents(newItems);
	}

	public void refreshAvailableRecipes() {
		refreshRecipes(ItemProp.craftReq, craftRecipes);
		refreshRecipes(ItemProp.buildReq, buildRecipes);
	}

	@Override
	protected void dropOverflowingItem(Item item) {
		super.dropOverflowingItem(item);
		MadSand.warn("You can't carry any more items.");
	}

	public boolean knowsItem(int id) {
		return unlockedItems.contains(id);
	}

	@Override
	public boolean addItem(Item item) {
		if (!super.addItem(item))
			return false;

		if (!item.name.isEmpty() && item.quantity > 0) {
			String notifStr = String.format("You get %d %s", item.quantity, item.name);
			Item stack = inventory.getItem(item);
			if (stack.quantity != item.quantity)
				notifStr += String.format(" (%d in inventory)", stack.quantity);
			MadSand.notice(notifStr);
		}

		stats().equipment.refreshUI();
		if (unlockedItems.add(item.id()))
			refreshAvailableRecipes();

		return true;
	}

	@Override
	public boolean dropItem(Item item, int quantity) {
		if (item.quantity == quantity && stats().equipment.itemEquipped(item))
			stats().equipment.unEquip(item);

		return super.dropItem(item, quantity);
	}

	@Override
	public void delItem(Item item, int quantity) {
		super.delItem(item, quantity);
		if (stats.equipment.itemEquipped(item)) {
			checkHands(item.id());
			stats.equipment.refreshUI();
		}
	}

	public void delItem(Item item) {
		delItem(item, item.quantity);
	}

	private boolean performCraftItem(CraftWorker craftWorker, int quantity) {
		Item craftedItem;
		if ((craftedItem = craftWorker.craftItem(quantity)) != Item.nullItem) {
			increaseSkill(Skill.Crafting, quantity);

			//Gui.drawOkDialog("Crafted " + craftedItem.quantity + " " + craftedItem.name + " successfully!");
			MadSand.print("You craft " + craftedItem.quantity + " " + craftedItem.name);

			int bonus = stats.luckRoll() ? Utils.rand(stats.skills.getItemReward(Skill.Crafting)) : 0;
			if (bonus > 0) {
				MadSand.notice("You manage to craft " + bonus + " extra " + craftedItem.name);
				addItem(Item.create(craftedItem.id(), bonus));
			}
			return true;
		}
		return false;
	}

	public void craftItem(CraftWorker craftWorker, int quantity) {
		doAction(stats.minorCost, () -> performCraftItem(craftWorker, quantity));
	}

	public void interact() {
		interact(stats.look);
	}

	public void interact(QuestMaster questMaster) {
		questMaster.pause();
		if (!quests.processQuests(questMaster.questList, questMaster))
			MadSand.print(questMaster.stats.name + " has no more quests for you.");
	}

	public void interact(Trader trader) {
		new TraderDialog(this, trader).show();
	}

	public void interact(Npc npc) {
		talkToNpc(npc);
	}

	public void interact(FarmAnimal animal) {
		new ItemFactoryUI(animal.getItemProducer()).show();
	}

	public void interact(AbstractNpc npc) {
		npc.interact(this);
		Gui.overlay.getContextMenu().close();
		Gui.overlay.hideActionBtn();
	}

	public void tradeWith(Entity npc) {
		new TradeInventoryUI(npc.inventory, inventory).show();
	}

	private void talkToNpc(AbstractNpc npc) {
		int currency = Globals.values().currencyId;
		Location location = MadSand.world().getLocation();
		if (!npc.stats.faction.isHuman()) {
			MadSand.print("Doesn't seem like " + npc.stats.name + " can talk");
			return;
		}
		String dialogTitle = npc.stats.name;
		if (location.isSettlement()) {
			WorkerType occupation = location.settlement.getOccupation(npc.uid());
			dialogTitle += ((occupation != null) ? " (" + occupation.name() + ")" : "");
		}
		GameDialog npcDialog = new DialogChainGenerator(Utils.randElement(Globals.values().idleNpcText))
				.setAllTitles(dialogTitle).generate(Gui.overlay);

		npcDialog.getProceedButton().setText("Goodbye");

		if (npc.canGiveQuests) {
			TextButton questButton = new TextButton("Do you need any help?", Gui.skin);
			npcDialog.addButton(questButton);
			Gui.setAction(questButton, () -> {
				npcDialog.remove();
				quests.startProceduralQuest(npc.uid());
			});
		}

		if (location.isPlayerOwnedSettlement()) {
			int hireCost = location.settlement.getHireCost();
			TextButton recruitButton = new TextButton("Will you work for me? " + Quest.OBJECTIVE_COLOR + "[[" + hireCost
					+ " " + ItemProp.getItemName(currency) + "s]" + Resources.COLOR_END, Gui.skin);
			if (!location.settlement.isOccupied(npc.uid()))
				npcDialog.addButton(recruitButton);

			Gui.setAction(recruitButton, () -> {
				npcDialog.remove();
				WorkerType worker = location.settlement.recruitWorker(npc.uid());
				if (!canAfford(hireCost)) {
					Gui.drawOkDialog("You don't have enough money to recruit this worker!");
					return;
				}
				inventory.delItem(currency, hireCost);
				new DialogChainGenerator(
						"Sure. I'll be the best " + worker.name() + " of the " + location.name + " settlement!")
								.setAllTitles(npc.stats.name).generate(Gui.overlay).show();
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

		Map loc = MadSand.world().getCurLoc();
		MapObject obj = loc.getObject(coords.x, coords.y);
		AbstractNpc npc = loc.getNpc(coords.x, coords.y);
		Tile tileInFront = loc.getTile(coords.x, coords.y);

		if (npc != Map.nullNpc) {
			interact(npc);
			return;
		}

		if (stats.isToolEquipped(Tool.Type.FishingRod) && tileInFront.hasFishingSpot())
			fish(tileInFront.fishingSpot);

		Utils.dbg("Interacting with %s", obj);
		obj.interact(this);
		MadSand.world().updateLight();
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
		MapEntity entity = mapEntityInFront();

		if (!entity.as(AbstractNpc.class).map(npc -> npc.isNeutral() || entity.isEmpty()).orElse(true))
			return;

		doAction(stats.minorCost, () -> performInteraction(direction));
	}

	public void startResourceGathering(MapObject object) {
		if (object.harvestHp > 0)
			new ResourceProgressBar(object).start();
		else
			gatherResources(object);
	}

	public int gatherResources(MapObject obj, Supplier<Integer> dmgSupplier) {
		if (obj.equals(Map.nullObject))
			return -1;

		doAction(stats.minorCost);
		damageHeldEquipment(obj);
		changeStamina(-stats.calcStaminaCost());

		return obj.acceptHit(this, dmgSupplier);
	}

	public int gatherResources(MapObject obj) {
		return gatherResources(obj, () -> obj.simulateHit(this));
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
			if (useScriptedTile()) {
				TimeUtils.scheduleTask(() -> Gui.overlay.refreshActionButton(), 0.25f);
				return;
			}

			item.use(this);
			checkHands(item.id());
		});
	}

	public void useItem(GrabBag item) {
		ArrayList<Item> items = item.contents().rollItems();
		for (Item rolledItem : items)
			addItem(rolledItem);

		inventory.delItem(item, 1);
		new GrabBagDialog(item.name, items).show();

		if (items.isEmpty())
			MadSand.warn("You opened " + item.name + ", but it was empty");
	}

	public void useItem(PlaceableItem item) {
		Map map = MadSand.world().getCurLoc();
		if (item.getType() == PlaceableItem.Type.Object) {
			MapObject object = map.addObject(x, y, stats.look, item.getAltObject());
			if (item.isDirectional())
				object.setDirection(stats.look);
		} else
			map.addTile(x, y, item.getAltObject());

		inventory.delItem(item, 1);
	}

	public void useItem(CropSeeds item) {
		Pair coords = new Pair(x, y);
		if (MadSand.world().getCurLoc().putCrop(coords.x, coords.y, item.id())) {
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
		String tileAction = TileProp.getOnInteract(MadSand.world().getTileId(x, y));
		if (!tileAction.equals(Resources.emptyField)) {
			Lua.execute(tileAction);
			return true;
		}

		return false;
	}

	public void useItem(Tool item) {
		changeStamina(-item.weight);
		damageHeldEquipment();

		if (item.type == Tool.Type.Shovel) {
			int ptile = MadSand.world().getTileId(x, y);
			int altItem = MapObject.rollTileResource(ptile, item.type);

			if (altItem != -1) {
				MadSand.world().getCurLoc().delTile(x, y);
				Item gotItem = Item.create(altItem);

				addItem(gotItem);

				MadSand.notice("You dig " + gotItem.name + " from the ground");
				increaseSkill(Skill.Digging);
			}
		}
	}

	public void freeHands(EquipSlot slot, boolean silent) {
		Item item = stats.equipment.getItem(slot);
		if (!stats.equipment.unEquip(item))
			return;

		if (!silent && item.id() != Item.NULL_ITEM)
			MadSand.print("You put " + item.name + " back to your inventory");

		inventory.getUI().refreshItem(item);
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
		World world = MadSand.world();
		world.skipToNextHour();
		Gui.overlay.refreshActionButton();
		world.setLayer(Location.LAYER_OVERWORLD);
		int wx = world.wx();
		int wy = world.wy();
		Map map = world.getCurLoc();
		stats.food = stats.maxFood;
		stats.actionPts = stats.actionPtsMax;
		stats.hp = stats.mhp;
		stats.stamina = stats.maxstamina;
		stats.dead = false;
		freeHands();
		Gui.drawOkDialog("Awakening",
				"You wake up without any memories about the previous " + Utils.timeString(getSurvivedTime()) + ".")
				.setMaxWidth(500)
				.newLine(2)
				.appendText(
						"Seems like you got into some trouble, but were miraculously saved by an unknown creature... Or force.")
				.newLine(2)
				.appendText(
						"Your wounds are almost completely healed, but all your possessions have disappeared from your backpack...")
				.newLine(2)
				.appendText("Perhaps they're still at the place where you passed out...");
		stats.spawnTime = MadSand.world().currentTick();

		if (stats.hasRespawnPoint) {
			if (stats.respawnWX == wx && stats.respawnWY == wy)
				teleport(stats.respawnX, stats.respawnY);
			else
				world.switchLocation(stats.respawnWX, stats.respawnWY, Location.LAYER_OVERWORLD);

		} else
			teleport(map.getRandomPoint());
		Gui.refreshOverlay();
		updCoords();
	}

	public Direction lookAtMouse(int x, int y, boolean diagonal) {
		if (isMoving())
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

	public MapEntity mapEntityInFront() {
		return MadSand.world().getCurLoc().getMapEntity(coords.set(x, y).addDirection(stats.look));
	}

	public MapObject objectLookingAt() {
		return MadSand.world().getCurLoc().getObject(x, y, stats.look);
	}

	public Direction lookAtMouse(int x, int y) {
		return lookAtMouse(x, y, false);
	}

	@Override
	public void turn(Direction dir) {
		super.turn(dir);
		Gui.overlay.refreshActionButton();
	}

	public boolean canTravel() {
		World world = MadSand.world();
		Map map = world.getCurLoc();
		Direction direction = stats.look;
		boolean canTravel;

		canTravel = (x == map.getWidth() - 1 && direction == Direction.RIGHT);
		canTravel |= (y == map.getHeight() - 1 && direction == Direction.UP);
		canTravel |= (x < 1 && direction == Direction.LEFT);
		canTravel |= (y < 1 && direction == Direction.DOWN);

		Pair newWorldPos = world.getCurWPos().copy().addDirection(direction);
		canTravel &= world.getWorldMap().validCoords(newWorldPos);

		return canTravel && world.curLayer() == Location.LAYER_OVERWORLD;
	}

	@Override
	protected float getMovementSpeed() {
		return Math.max(MIN_ANIM_SPEED, super.getMovementSpeed());
	}

	@Override
	public boolean move(Direction dir) {
		if (!super.move(dir))
			return false;

		if ((MadSand.world().curLayer() == Location.LAYER_OVERWORLD) && canTravel())
			MadSand.print("Press [GRAY]N[WHITE] to travel to the next sector.");

		return true;
	}

	@Override
	public void teleport(int x, int y) {
		super.teleport(x, y);
		Gdx.graphics.requestRendering();
		MadSand.getRenderer().setWorldCamPosition(x, y);
		MadSand.world().updateLight();
	}

	@Override
	public void heal(int by) {
		boolean atMaxHp = stats.hp == stats.mhp;
		super.heal(by);

		if (by > 0 && !atMaxHp)
			MadSand.notice("Your wounds are healing [+" + by + " HP]");
	}

	private void rest(int ticks, boolean verbose) {
		double ap = stats.actionPts;
		stats.actionPts = stats.actionPtsMax;
		MadSand.world().timeTick(ticks);
		MadSand.world().timeSubtick(getActionLength(ap + (ticks - 1) * getSpeed()));

		if (verbose)
			MadSand.print("You rest a bit");
	}

	public void rest() {
		rest(1, true);
	}

	public void skipTicks(int ticks) {
		if (ticks > 1)
			MadSand.world().startTimeSkip();
		rest(ticks, false);
	}

	private void skipTime(int ticks, BooleanSupplier skipCondition) { // Skips <ticks> ticks while skipCondition is true
		MadSand.world().startTimeSkip();
		int step = Utils.largestDivisor(ticks);
		for (int i = 0; i < ticks; i += step) {
			if (!skipCondition.getAsBoolean())
				break;

			skipTicks(step);
		}
	}

	private int skipTime(BooleanSupplier skipCondition) {
		final int SKIP_STEP = 5;
		int ticksSkipped = 0;
		while (skipCondition.getAsBoolean()) {
			skipTicks(SKIP_STEP);
			ticksSkipped += SKIP_STEP;
		}
		return ticksSkipped;
	}

	public boolean canRest() {
		if (!stats.isSatiated()) {
			MadSand.warn("You are too hungry to rest...");
			return false;
		}

		if (isTargeted()) {
			MadSand.warn("It's no time to rest. A hostile creature is nearby!");
			return false;
		}

		return true;
	}

	public void restFully() {
		if (!canRest())
			return;

		int ticksToRest = (int) ((stats.maxstamina - stats.stamina) / stats.getStaminaRegenRate());
		if (ticksToRest == 0 && stats.healthFull()) {
			MadSand.notice("Your stamina and health are already full");
			return;
		}

		if (ticksToRest > 0)
			skipTime(ticksToRest, () -> canRest());
		else
			ticksToRest = skipTime(() -> canRest() && !stats.healthFull());

		MadSand.notice("Rested for " + Utils.timeString(MadSand.world().toWorldTimeSeconds(ticksToRest)));
	}

	public static float TIMESKIP_COEF = 18f, REALTIME_SKIP_COEF = 5.5f;

	public void skipTime() {
		int timeSkipItemId = Globals.values().timeSkipItem;
		Item timeSkipItem = inventory.getItem(timeSkipItemId);
		if (timeSkipItem.equals(Item.nullItem)) {
			Gui.drawOkDialog(
					"You need at least 1 " + ItemProp.getItemName(timeSkipItemId) + " to be able to skip time.");
			return;
		}

		int maxTimeSkip = (int) (timeSkipItem.quantity * TIMESKIP_COEF);
		new WaitDialog(maxTimeSkip).show();
	}

	public int doAction(double ap, Runnable action) {
		if (scheduledAction != null)
			return -1;

		int ticks = super.doAction(ap);
		scheduledAction = action;
		MadSand.world().timeTick(ticks); // committing our action and then letting world catch up to time we've spent
		MadSand.world().timeSubtick(getActionLength(ap)); // letting NPCs catch up
		Gui.overlay.refresh();
		Lua.onAction.run();
		return ticks;
	}

	@Override
	public int doAction(double ap) {
		int ticks = doAction(ap, () -> {});
		Gui.overlay.refreshActionButton();
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

		MadSand.world().updateLight();
		objectInFront();
		lootMsg();
	}

	@Override
	public boolean walk(Direction dir) {
		if (canWalk(dir))
			doAction(stats.walkCost, () -> performWalk(dir));
		return true;
	}

	@Override
	public void stopMovement() {
		super.stopMovement();
		if (!hasQueuedMovement()) {
			if (afterMovement != null) {
				afterMovement.run();
				afterMovement = null;
			}
		}
	}

	public void doAfterMovement(Runnable action) {
		if (!isMoving()) {
			action.run();
			return;
		}

		afterMovement = action;
	}

	public void run(Path path) {
		speedUp(runSpeedCoef);
		super.move(path);
	}

	@Override
	protected void pollMovementQueue() {
		if (!hasQueuedMovement())
			return;

		walk(movementQueue.poll());
	}

	@Override
	public void move(Path path) {
		Keyboard.ignoreInput();
		super.move(path);
	}

	@Override
	public void act(float time) {
		commitAction();
	}

	public void attackHostile() {
		AbstractNpc npc = MadSand.world().getCurLoc().getNpc(lookingAt());

		if (npc.equals(Map.nullNpc))
			return;

		if (!npc.isNeutral())
			meleeAttack();
	}

	@Override
	public void damage(int to) {
		super.damage(to);
		Gui.refreshOverlay();
	}

	@Override
	public void acceptDamage(Damage damage) {
		Entity dealer = damage.getDealer();
		if (damage.missed()) {
			MadSand.print(dealer.getName() + " misses!");
			increaseSkill(Skill.Evasion);
		} else
			MadSand.warn(dealer.getName() + " deals " + damage.getValueString() + " damage to you");
		super.acceptDamage(damage);
	}

	public void lootMsg() {
		if (standingOnLoot()) {
			Loot loot = MadSand.world().getCurLoc().getLoot(x, y);
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
		items.add(Item.create(Globals.values().currencyId, creationCost));

		// Not-so-random material of tier #<settlementsEstablished>
		Item requiredResource = Item.create(
				ItemCategories.get().getItemList(ItemCategory.Materials, settlementsEstablished)
						.getRandomId(new Random(settlementsEstablished)));
		requiredResource.quantity = SETTLEMENT_RES_COST * (settlementsEstablished + 1);
		items.add(requiredResource);

		return items;
	}

	public void objectInFront() {
		MapEntity entity = mapEntityInFront();

		if (!entity.isEmpty())
			MadSand.print("You see: " + entity.getName());
	}

	public boolean completeDungeon() {
		return completedDungeons.add(MadSand.world().getCurWPos().copy());
	}

	@JsonIgnore
	public TextureRegion getSprite() {
		if (!isMoving())
			return super.getSprite();

		return walkAnim[stats.look.baseOrdinal()].getCurrentKeyFrame(true);
	}

	public long getSurvivedTime() {
		return MadSand.world().toWorldTimeSeconds(MadSand.world().currentTick() - stats.spawnTime);
	}

	public void registerLuaAction(String name) {
		luaActions.add(name);
	}

	public boolean luaActionDone(String name) {
		return luaActions.contains(name);
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

	public int dungeonsCompleted() {
		return completedDungeons.size();
	}
}