package hitonoriol.madsand.map;

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
		createPlaceable(Resources.load(json, PlaceableItem.class));
	}

	void createPlaceable(PlaceableItem item);

	default void createPlaceable(PlaceableItem item, Runnable initTask) {
		Resources.deferInit(() -> {
			java.util.Map<Integer, Item> items = ItemProp.items;
			item.setAltObject(id());
			item.setId(Item.getLastId() + 1);
			items.put(item.id(), item);
			createPlaceableTexture("inv/" + item.id());
			initTask.run();
			item.initRecipe();
			Utils.dbg("Created dynamic Placeable: {%s}", item);
		});
	}

	void createPlaceableTexture(String name);
}