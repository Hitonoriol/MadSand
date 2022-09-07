package hitonoriol.madsand.map;

import static hitonoriol.madsand.resources.Resources.loader;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.node.ObjectNode;

import hitonoriol.madsand.Enumerable;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.inventory.item.PlaceableItem;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.Utils;

public interface Placeable extends Enumerable {

	@JsonSetter("placeable")
	default void loadPlaceable(ObjectNode json) {
		json.put("@type", "PlaceableItem");
		createPlaceable(loader().load(json, PlaceableItem.class));
	}

	void createPlaceable(PlaceableItem item);

	default void createPlaceable(PlaceableItem item, Runnable initTask) {
		Resources.deferInit(() -> {
			java.util.Map<Integer, Item> items = ItemProp.items;
			item.setAltObject(id());
			item.setId(Item.getLastId() + 1);
			items.put(item.id(), item);
			Resources.getAtlas().addRegion("inv/" + item.id(), createPlaceableTexture());
			initTask.run();
			item.initRecipe();
			Utils.dbg("Created dynamic Placeable: {%s}", item);
		});
	}

	TextureRegion createPlaceableTexture();
}