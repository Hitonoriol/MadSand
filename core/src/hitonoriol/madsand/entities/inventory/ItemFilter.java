package hitonoriol.madsand.entities.inventory;

import java.util.function.Predicate;

import hitonoriol.madsand.entities.inventory.item.AbstractEquipment;
import hitonoriol.madsand.entities.inventory.item.Armor;
import hitonoriol.madsand.entities.inventory.item.Consumable;
import hitonoriol.madsand.entities.inventory.item.CropSeeds;
import hitonoriol.madsand.entities.inventory.item.FishingBait;
import hitonoriol.madsand.entities.inventory.item.GrabBag;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.inventory.item.PlaceableItem;
import hitonoriol.madsand.entities.inventory.item.Projectile;
import hitonoriol.madsand.entities.inventory.item.ScriptedConsumable;
import hitonoriol.madsand.entities.inventory.item.Tool;
import hitonoriol.madsand.entities.inventory.item.Weapon;

public class ItemFilter implements Predicate<Item> {
	private static final ItemFilter filters[] = {
		create("All", item -> true),
		create(
			"Consumables",
			item -> item.is(Consumable.class) || item.is(ScriptedConsumable.class) || item.is(GrabBag.class)
		),
		create("Equipment", item -> item.is(AbstractEquipment.class)),
		create("Armor", item -> item.is(Armor.class)),
		create("Weapons", item -> item.is(Weapon.class)),
		create("Ammunition", item -> item.is(Projectile.class)),
		create("Tools", item -> item.is(Tool.class)),
		create("Seeds", item -> item.is(CropSeeds.class)),
		create("Fishing bait", item -> item.is(FishingBait.class)),
		create("Placeables", item -> item.is(PlaceableItem.class))
	};

	private String name;
	private Predicate<Item> predicate;

	private ItemFilter(String name, Predicate<Item> predicate) {
		this.name = name;
		this.predicate = predicate;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean test(Item item) {
		return predicate.test(item);
	}

	public static ItemFilter[] getFilters() {
		return filters;
	}

	public static ItemFilter create(String name, Predicate<Item> predicate) {
		return new ItemFilter(name, predicate);
	}
}
