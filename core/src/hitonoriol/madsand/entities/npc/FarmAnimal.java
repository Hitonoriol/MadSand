package hitonoriol.madsand.entities.npc;

import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.gui.textgenerator.CellInfoGenerator;
import hitonoriol.madsand.map.ItemProducer;
import hitonoriol.madsand.properties.NpcContainer;

public class FarmAnimal extends AbstractNpc {
	public ItemProducer animalProduct;

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
		return sb.toString() + super.getInfoString();
	}

	@Override
	public void interact(Player player) {
		player.interact(this);
	}
}
