package hitonoriol.madsand.map.object;

import hitonoriol.madsand.TimeDependent;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.gamecontent.Items;
import hitonoriol.madsand.gui.dialogs.ItemFactoryUI;
import hitonoriol.madsand.map.ItemProducer;
import hitonoriol.madsand.resources.Resources;

public class ItemFactory extends BuiltObject implements TimeDependent {
	private ItemProducer itemProducer;

	public ItemFactory(ItemFactory protoObject) {
		super(protoObject);
		initItemProducer();
	}

	@Override
	public ItemFactory copy() {
		return new ItemFactory(this);
	}

	public ItemFactory() {
	}

	public ItemProducer getItemProducer() {
		if (itemProducer == null)
			initItemProducer();

		return itemProducer;
	}

	public void setItemProducer(ItemProducer producer) {
		itemProducer = producer;
	}

	private void initItemProducer() {
		itemProducer = new ItemProducer(id);
	}

	@Override
	public void interact(Player player) {
		super.interact(player, () -> new ItemFactoryUI(itemProducer).show());
	}

	@Override
	public String getBuildInfo() {
		var info = "Produces " + Items.all().getName(getItemProducer().getProductId());

		if (!itemProducer.isEndless())
			info += Resources.LINEBREAK + "Consumes " + Items.all().getName(itemProducer.getConsumedMaterialId());

		return info;
	}

	@Override
	public void update() {
		itemProducer.produce();
	}
}
