package hitonoriol.madsand.map.object;

import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.gui.dialogs.ProductionStationUI;
import hitonoriol.madsand.map.ItemProducer;

public class ItemFactory extends BuiltObject {

	public ItemProducer itemProducer;

	public ItemFactory(ItemFactory protoObject) {
		super(protoObject);
		itemProducer = new ItemProducer(id);
	}

	@Override
	public ItemFactory copy() {
		return new ItemFactory(this);
	}

	public ItemFactory() {
		super();
	}

	@Override
	public void interact(Player player) {
		super.interact(player, () -> new ProductionStationUI(itemProducer).show());
	}
}
