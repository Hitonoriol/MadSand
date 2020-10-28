package hitonoriol.madsand.entities.quest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.dialog.DialogChainGenerator;
import hitonoriol.madsand.entities.Npc;
import hitonoriol.madsand.entities.inventory.Item;
import hitonoriol.madsand.enums.TradeCategory;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.properties.NpcProp;
import hitonoriol.madsand.world.World;

public class ProceduralQuest extends Quest {

	private final int EXP_MIN = 10;

	private final int MAX_RESOURCE_REQS = 3;
	private final int MAX_ITEM_Q = 10;
	public Type type;

	public ProceduralQuest(int id, long npcUID) {
		super(id);
		super.npcUID = npcUID;
		generate();
	}

	public ProceduralQuest() {
		super();
	}

	public void generate() {
		type = Type.values()[Utils.rand(Type.values().length)];
		super.reqItems = "";
		super.startMsg = super.endMsg = "#" + getNpc().stats.name + "#";
		super.startMsg += getStartMsg();

		switch (type) {
		case Resource:
			randomResourceQuest();
			break;
		case Craft:
			if (World.player.craftRecipes.isEmpty()) {
				generate();
				return;
			}
			randomCraftQuest();
			break;
		default:
			break;
		}

		super.name = type.name() + " Quest";
		super.exp = Utils.rand(EXP_MIN, World.player.stats.skills.getExp() / 10);
		super.reqMsg = super.startMsg;
		createEndMsg();
	}

	private void createEndMsg() {
		super.endMsg += "Thank you, {PLAYER}!" + Resources.LINEBREAK + Resources.LINEBREAK;
		super.endMsg += DialogChainGenerator.LBRACKET + "+" + super.exp + " EXP" + DialogChainGenerator.RBRACKET
				+ Resources.LINEBREAK;
		if (!super.rewardItems.equals(Item.EMPTY_ITEM))
			super.endMsg += DialogChainGenerator.LBRACKET + "+" + Item.queryToName(super.rewardItems)
					+ DialogChainGenerator.RBRACKET;
	}

	private void randomItemQuest(List<Integer> items) {
		HashSet<Integer> usedItems = new HashSet<>();
		int rolls = Utils.rand(1, MAX_RESOURCE_REQS);
		int quantity;
		int item;

		for (int i = 0; i < rolls; ++i) {

			while (!usedItems.add(item = Utils.randElement(items)))
				if (usedItems.size() == items.size())
					break;

			quantity = Utils.rand(1, MAX_ITEM_Q);
			super.startMsg += quantity + " " + ItemProp.getItemName(item);

			if (i == rolls - 2)
				super.startMsg += " and ";
			else if (i < rolls - 1)
				super.startMsg += ", ";
			else
				super.startMsg += " ";

			super.reqItems += item + Item.ITEM_DELIM + quantity + Item.BLOCK_DELIM;
		}
	}

	private void randomResourceQuest() {
		List<Item> items = NpcProp.tradeLists.roll(TradeCategory.Materials, 0);
		List<Integer> reqs = new ArrayList<>();

		for (Item item : items)
			if (!reqs.contains(item.id))
				reqs.add(item.id);

		randomItemQuest(reqs);
	}

	private String getStartMsg() {
		return Utils.randElement(Globals.instance().proceduralQuestText.get(type));
	}

	private void randomCraftQuest() {
		randomItemQuest(World.player.craftRecipes);
	}

	@JsonIgnore
	public Npc getNpc() { // Returns nullNpc if quest npc does not exist or is not in the current location
		return MadSand.world.getCurLoc().getNpc(npcUID);
	}

	public enum Type {
		Craft, // Request random craftable item
		//Fetch, // Place random item somewhere in the world and ask player to bring it to NPC
		Resource,
		//Kill // kill quests
	}

}
