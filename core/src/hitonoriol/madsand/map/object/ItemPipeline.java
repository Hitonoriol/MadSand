package hitonoriol.madsand.map.object;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableFloat;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.TimeDependent;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.map.ItemProducer;
import hitonoriol.madsand.map.Loot;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.util.Functional;
import hitonoriol.madsand.util.Utils;

public class ItemPipeline extends MapObject implements TimeDependent {
	private float transportLimit; // kgs per tick
	private boolean sentItems = false, receivedItems = false;

	public ItemPipeline(ItemPipeline protoObject) {
		super(protoObject);
		transportLimit = protoObject.transportLimit;
	}

	public ItemPipeline() {}

	@Override
	public MapObject copy() {
		return new ItemPipeline(this);
	}

	@Override
	public void interact(Player player) {
		super.setDirection(directionFacing.counterClockwise());
	}

	private List<Item> getItems(Loot loot) {
		loot.mergeItemStacks();
		MutableFloat weight = new MutableFloat(0);
		List<Item> items = new ArrayList<>();
		Functional.takeWhile(loot.getContents().stream(), item -> weight.getValue() < transportLimit)
				.map(item -> item.split(transportLimit - weight.getValue()))
				.forEach(item -> {
					weight.add(item.getTotalWeight());
					items.add(item);
				});
		items.forEach(item -> loot.remove(item));
		return items;
	}

	private void moveItems(Loot from, Pair to) {
		getItems(from).forEach(item -> MadSand.world().exec(map -> map.putLoot(to, item)));
		MadSand.world().exec(map -> map.getObject(to).as(ItemPipeline.class)
				.ifPresent(pipeline -> pipeline.receivedItems = true));
	}

	/*
	 *	Move items from current tile in set direction - to the next tile or as a material for ItemFactory 
	 *	Or, if there's an ItemFactory in an opposite direction one tile away,
	 *	Pull factory's product to the current tile 
	 */
	@Override
	public void update() {
		if (receivedItems) {
			receivedItems = false;
			if (!sentItems)
				return;
		}
		Pair position = getPosition();
		MadSand.world().exec(map -> {
			map.getObject(position.copy().addDirection(directionFacing.opposite()))
					.as(ItemFactory.class)
					.ifPresentOrElse(
							factory -> {
								ItemProducer producer = factory.getItemProducer();
								Item product = Item.create(producer.producedMaterial);
								Utils.dbg("Extracting ItemFactory items at %s", position);
								map.putLoot(position, producer.getProduct((int) (transportLimit / product.weight)));
							},

							() -> {
								Loot loot = map.getLoot(position);
								if (loot == Map.nullLoot || loot.isEmpty())
									return;

								if (sentItems) {
									sentItems = false;
									return;
								}

								Utils.dbg("Movin items from %s", position);
								sentItems = true;
								moveItems(loot, position.addDirection(directionFacing));
							});
		});
	}

	public float getTransportLimit() {
		return transportLimit;
	}
}
