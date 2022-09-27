package hitonoriol.madsand.gamecontent;

import java.util.Map;

import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.map.ItemProducer;
import hitonoriol.madsand.map.object.MapObject;
import hitonoriol.madsand.resources.GameAssetManager;
import hitonoriol.madsand.resources.loaders.ContentLoader;
import hitonoriol.madsand.resources.loaders.ContentLoader.ContentMap;

public class Objects extends ContentStorage<MapObject> {
	private Map<Integer, ItemProducer> itemProducers;
	private Map<Integer, String> buildRecipes;

	private static final Objects instance = new Objects();

	private static final String ITEMFACTORY_FILE = "itemfactories.json";
	private static final String BUILDRECIPE_FILE = "buildrecipes.json";
	
	private static class ItemProducerMap extends ContentMap<ItemProducer> {}
	private static class RecipeMap extends ContentMap<String> {}

	public Objects() {
		super(hitonoriol.madsand.map.Map.nullObject);
	}

	public String getOnInteract(int id) {
		return get(id).onInteract;
	}

	public Map<Integer, ItemProducer> itemProducers() {
		return itemProducers;
	}

	public Map<Integer, String> buildRecipes() {
		return buildRecipes;
	}

	public void setItemProducers(Map<Integer, ItemProducer> itemProducers) {
		this.itemProducers = itemProducers;
	}

	public void setBuildRecipes(Map<Integer, String> buildRecipes) {
		this.buildRecipes = buildRecipes;
	}

	@Override
	public void registerLoader(GameAssetManager manager) {
		manager.contentMapLoader(ItemProducerMap.class, ItemProducer.class);
		manager.contentMapLoader(RecipeMap.class, String.class);
		manager.setLoader(Objects.class, new ContentLoader<>(manager, instance, MapObject.class) {
			@Override
			protected void load(GameAssetManager manager, Objects storage) {
				manager.loadAndThen(ITEMFACTORY_FILE, ItemProducerMap.class, Objects.all()::setItemProducers);
				manager.loadAndThen(BUILDRECIPE_FILE, RecipeMap.class, recipes -> {
					Objects.all().setBuildRecipes(recipes);
					recipes.forEach((id, recipe) -> Items.all().buildRequirements().put(id, Item.parseCraftRequirements(recipe)));
				});
			}
		});
	}

	public static Objects all() {
		return instance;
	}
}
