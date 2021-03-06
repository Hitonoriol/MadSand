package hitonoriol.madsand.map.object;

import hitonoriol.madsand.Resources;
import hitonoriol.madsand.TimeDependent;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.gui.dialogs.ItemFactoryUI;
import hitonoriol.madsand.map.ItemProducer;
import hitonoriol.madsand.properties.ItemProp;

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
		super();
	}

	public ItemProducer getItemProducer() {
		if (itemProducer == null)
			initItemProducer();

		return itemProducer;
	}
	
	public void setItemProducer(ItemProducer producer) {
		this.itemProducer = producer;
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
		String info = "Produces " + ItemProp.getItemName(getItemProducer().getProductId());

		if (!itemProducer.isEndless())
			info += Resources.LINEBREAK + "Consumes " + ItemProp.getItemName(itemProducer.getConsumedMaterialId());

		return info;
	}
	
	@Override
	public void update() {
		itemProducer.produce();
	}
}
