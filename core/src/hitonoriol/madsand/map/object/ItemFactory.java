package hitonoriol.madsand.map.object;

import hitonoriol.madsand.Resources;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.gui.dialogs.ProductionStationUI;
import hitonoriol.madsand.map.ItemProducer;
import hitonoriol.madsand.properties.ItemProp;

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

	@Override
	public String getBuildInfo() {
		String info = "Produces " + ItemProp.getItemName(itemProducer.producedMaterial);

		if (!itemProducer.isEndless())
			info += Resources.LINEBREAK + "Consumes " + ItemProp.getItemName(itemProducer.consumedMaterial);

		return info;
	}
}
