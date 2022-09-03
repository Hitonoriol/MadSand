package hitonoriol.madsand.entities.inventory;

import static java.util.Comparator.comparingDouble;
import static java.util.Comparator.comparingInt;

import java.util.Comparator;

import hitonoriol.madsand.entities.inventory.item.Item;

public class ItemSort implements Comparator<Item> {
	private static final ItemSort sortings[] = {
			create("Default", comparingInt(Item::id)),
			create("Name", Comparator.comparing(Item::name)),
			create("Price", comparingInt(Item::getPrice)),
			create("Weight", comparingDouble(Item::getWeight)),
			create("Quantity", comparingInt(Item::getQuantity)),
			create("Stack weight", comparingDouble(Item::getTotalWeight)),
			create("Stack price", comparingDouble(Item::getTotalPrice))
	};

	private String name;
	private Comparator<Item> comparator;

	private ItemSort(String name, Comparator<Item> comparator) {
		this.name = name;
		this.comparator = comparator;
	}
	
	public Comparator<Item> applyOrder(Order order) {
		return order == Order.Ascending ? this : reversed();
	}
	
	@Override
	public int compare(Item o1, Item o2) {
		return comparator.compare(o1, o2);
	}

	@Override
	public String toString() {
		return name;
	}

	public static ItemSort[] getSortings() {
		return sortings;
	}
	
	public static ItemSort create(String name, Comparator<Item> comparator) {
		return new ItemSort(name, comparator);
	}
	
	public enum Order {
		Ascending, Descending
	}
}
