package hitonoriol.madsand.entities.quest;

import java.util.ArrayList;

import hitonoriol.madsand.Utils;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.enums.TradeCategory;
import hitonoriol.madsand.properties.NpcProp;

public class ProceduralQuest extends Quest {

	private final int MAX_RESOURCE_REQS = 3;
	public Type type;

	public ProceduralQuest(int id) {
		super(id);
		generate();
	}

	public void generate() {
		type = Type.values()[Utils.rand(Type.values().length)];

		switch (type) {
		case Resource:
			randomResourceQuest();
			break;
		default:
			break;
		}
	}

	private void randomResourceQuest() {
		ArrayList<Item> reqs = NpcProp.tradeLists.roll(TradeCategory.Materials, 0);
		int i = 1;
		for (Item item : reqs) {
			if (i > MAX_RESOURCE_REQS)
				break;

			itemObjective.put(item.id, item.quantity);

			++i;
		}
	}

	public enum Type {
		//Craft, // Request random craftable item
		//Fetch, // Place random item somewhere in the world and ask player to bring it to NPC
		Resource,
		//Kill // kill quests
	}

}
