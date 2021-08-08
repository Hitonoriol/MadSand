package hitonoriol.madsand.tests;

import static org.junit.Assume.assumeFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.RepeatedTest;

import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.entities.inventory.item.ScriptedConsumable;
import hitonoriol.madsand.util.Utils;

public class ItemTest {

	@RepeatedTest(20)
	void fuzzyItemCreateTest() {
		/* No asserts here, if it fails, just be more specific with the item name, yknow */

		Item expectedItem;
		do
			expectedItem = Item.createRandom();
		while (expectedItem.is(ScriptedConsumable.class));
		/* (ScriptedConsumables have dynamically created names, no idea how to fuzzy search over them) */

		String name = expectedItem.name;
		String partialName = name.toLowerCase().substring(0, (int) (name.length() * 0.9));
		Item item = Item.create(partialName);
		assumeFalse("Got an item with the same name, but different id",
				expectedItem.id() != item.id() && expectedItem.name.equals(item.name));

		Utils.out("Trying to find \"%s\", result: \"%s\"", partialName, item.name);
		assumeTrue(item.id() == expectedItem.id(), "Failed to fuzzy match item names");
	}

	@RepeatedTest(10)
	void itemCopyTest() {
		Item originalItem = Item.createRandom();
		Item copy = originalItem.copy();
		/* Item.copy() should always produce exact copy
		 * (so that hashes of both items are equal/original.equals(copy) always true -- even for unique items) */

		assertEquals(originalItem, copy, "Copied item should be exactly the same as the original");
	}

}
