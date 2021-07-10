package hitonoriol.madsand.entities.npc;

import hitonoriol.madsand.Resources;
import hitonoriol.madsand.TimeDependent;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.gui.textgenerator.CellInfoGenerator;
import hitonoriol.madsand.map.ItemProducer;
import hitonoriol.madsand.properties.NpcContainer;

public class FarmAnimal extends AbstractNpc implements TimeDependent {
	private ItemProducer animalProduct;

	public FarmAnimal(NpcContainer protoNpc) {
		super(protoNpc);
		animalProduct = new ItemProducer(-id);
	}

	public FarmAnimal() {
		super();
	}

	@Override
	public String getInfoString() {
		StringBuilder sb = new StringBuilder();
		CellInfoGenerator.getItemProducerInfo(sb, animalProduct);
		return sb.toString() + Resources.LINEBREAK + super.getInfoString();
	}

	@Override
	public void interact(Player player) {
		player.interact(this);
	}

	public ItemProducer getItemProducer() {
		return animalProduct;
	}

	@Override
	public void update() {
		animalProduct.produce();
	}
}
