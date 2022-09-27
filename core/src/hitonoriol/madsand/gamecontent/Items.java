package hitonoriol.madsand.gamecontent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.badlogic.gdx.files.FileHandle;

import hitonoriol.madsand.entities.inventory.item.CropSeeds;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.inventory.item.PlaceableItem;
import hitonoriol.madsand.map.CropGrowthStageContainer;
import hitonoriol.madsand.resources.Content;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.resources.GameAssetManager;
import hitonoriol.madsand.resources.GameAssetManager.Dependencies;
import hitonoriol.madsand.resources.loaders.ContentLoader;

public class Items extends ContentStorage<Item> {
	private static final Items instance = new Items();
	
	private Queue<Runnable> initQueue = new ArrayDeque<>();
	private Map<Integer, List<Integer>> craftStationRecipes = new HashMap<>();
	private Map<Integer, List<Integer>> craftRequirements = new HashMap<>();
	private Map<Integer, List<Integer>> buildRequirements = new HashMap<>();
	
	protected Items() {
		super(Item.nullItem);
	}
	
	public Map<Integer, List<Integer>> craftStationRecipes() {
		return craftStationRecipes;
	}
	
	public Map<Integer, List<Integer>> craftRequirements() {
		return craftRequirements;
	}
	
	public Map<Integer, List<Integer>> buildRequirements() {
		return buildRequirements;
	}

	public void addCraftStationRecipe(int id, int item) {
		List<Integer> items;
		if (!craftStationRecipes.containsKey(id))
			items = new ArrayList<>();
		else
			items = craftStationRecipes.remove(id);

		items.add(item);
		craftStationRecipes.put(id, items);

	}

	public String getCraftRecipe(int id) {
		return get(id).recipe;
	}

	public int getCraftQuantity(int id) {
		int quantity = get(id).craftQuantity;

		if (quantity < 1)
			quantity = 1;

		return quantity;
	}

	public int getCost(int id) {
		return get(id).cost;
	}

	public int getAltObject(int id) {
		return ((PlaceableItem) get(id)).getAltObject();
	}

	public String getOnUseAction(int id) {
		String action = get(id).useAction;

		if (action == null)
			return Resources.emptyField;

		return action;
	}

	public CropGrowthStageContainer getCropStages(int id) {
		return ((CropSeeds) get(id)).cropContainer.cropStages;
	}

	public int getCropSoil(int id) {
		return ((CropSeeds) get(id)).cropContainer.soil;
	}

	public static Items all() {
		return instance;
	}

	/* Defer an item-related task until all items have been fully loaded */
	public static void deferInit(Runnable initTask) {
		instance.initQueue.add(initTask);
	}

	private void finalizeInit() {
		initQueue.forEach(action -> action.run());
	}
	
	@Override
	public void registerLoader(GameAssetManager manager) {
		manager.setLoader(Items.class, new ContentLoader<>(manager, instance, Item.class) {
			@Override
			protected void load(GameAssetManager manager, Items storage) {
				finalizeInit();
				storage.get().forEach((id, item) -> {
					item.initRecipe();
					item.initCategory();
				});
			}
			
			@Override
			public Dependencies getDependencies(String fileName, FileHandle file, Parameters<Items> parameter) {
				return Content.asDependencies(Content.globals);
			}
		});
	}
}