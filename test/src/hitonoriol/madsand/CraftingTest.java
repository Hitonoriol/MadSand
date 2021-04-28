package hitonoriol.madsand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import hitonoriol.madsand.entities.Entity;
import org.junit.jupiter.api.BeforeEach;


import hitonoriol.madsand.entities.inventory.CraftWorker;
import hitonoriol.madsand.entities.inventory.Inventory;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.util.Utils;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

public class CraftingTest {
    Entity player = MadSandTestWrapper.player();
    Inventory inventory = player.inventory;

    @BeforeEach
    void setUp() {
        inventory.clear();
    }

    @RepeatedTest(5)
    public void test() {
        Utils.out("Crafting test");
        Item itemToCraft = getRandomCraftableItem();
        Utils.out("Trying to craft %s", itemToCraft);
        assertTrue(itemToCraft.isCraftable(), itemToCraft + " is not craftable");

        List<Item> craftRecipe = Item.parseItemString(itemToCraft.recipe);
        player.addItem(craftRecipe);
        inventory.dump();
        assertTrue(inventory.containsAll(craftRecipe), "Failed to add all ingredients to inventory");

        CraftWorker craftWorker = new CraftWorker(player, itemToCraft);
        assertTrue(craftWorker.canBeCrafted(), "CraftWorker: " + itemToCraft + " can't be crafted");

        craftWorker.craftItem(1);
        assertTrue(player.hasItem(itemToCraft.id), "Failed to add crafted item to inventory");
        assertTrue(inventory.containsNone(craftRecipe), "Failed to remove ingredients from inventory");
        Utils.out();
    }

    private Item getRandomCraftableItem() {
        return Item.create(Utils.randElement(ItemProp.craftReq.keySet()));
    }
}
