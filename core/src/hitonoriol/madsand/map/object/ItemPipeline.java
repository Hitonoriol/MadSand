package hitonoriol.madsand.map.object;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableFloat;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.TimeDependent;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.map.Loot;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.util.Functional;

public class ItemPipeline extends MapObject implements TimeDependent {
	private float transportLimit; // kgs per tick
	private long transportPeriod; // realtime ticks per update() call
	private boolean sentItems = false, receivedItems = false;

	public ItemPipeline(ItemPipeline protoObject) {
		super(protoObject);
		transportLimit = protoObject.transportLimit;
		transportPeriod = protoObject.transportPeriod;
	}

	public ItemPipeline() {}

	@Override
	public MapObject copy() {
		return new ItemPipeline(this);
	}

	@Override
	public void interact(Player player) {
		super.interact(player, () -> setDirection(directionFacing.rotateClockwise()));
	}

	private List<Item> getItems(Loot loot) {
		loot.mergeItemStacks();
		var weight = new MutableFloat(0);
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
		getItems(from).forEach(item -> MadSand.world().exec(map -> {
			var destObject = map.getObject(to);
			destObject.as(ItemFactory.class)
				.ifPresent(factory -> {
					var producer = factory.getItemProducer();
					if (item.equals(producer.getConsumedMaterialId())) {
						producer.addRawMaterial(item.quantity);
						item.clear();
					}
				});
			destObject.as(ItemPipeline.class)
				.ifPresent(pipeline -> pipeline.receivedItems = true);
			map.putLoot(to, item);
		}));
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
		var position = getPosition();
		MadSand.world().exec(map -> {
			Functional.ifPresentOrElse(
				map.getObject(position.copy().addDirection(directionFacing.opposite()))
					.as(ItemFactory.class),

				factory -> {
					var producer = factory.getItemProducer();
					if (!producer.hasProduct())
						return;
					var product = Item.create(producer.getProductId());
					map.putLoot(position, producer.getProduct((int) (transportLimit / product.weight)));
				},

				() -> {
					var loot = map.getLoot(position);
					if (loot == Map.nullLoot || loot.isEmpty())
						return;

					if (sentItems) {
						sentItems = false;
						return;
					}
					sentItems = true;
					moveItems(loot, position.addDirection(directionFacing));
				}
			);
		});
	}

	public float getTransportLimit() {
		return transportLimit;
	}

	@Override
	public long getUpdateRate() {
		return transportPeriod;
	}
}
