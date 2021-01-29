package hitonoriol.madsand.entities.npc;

import hitonoriol.madsand.Mouse;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.map.ProductionStation;
import hitonoriol.madsand.properties.NpcContainer;

public class FarmAnimal extends AbstractNpc {
	public ProductionStation animalProduct;

	public FarmAnimal(NpcContainer protoNpc) {
		super(protoNpc);
		animalProduct = new ProductionStation(-id);
	}

	public FarmAnimal() {
		super();
	}

	@Override
	public String getInfoString() {		
		Mouse.getProdStationInfo(animalProduct);
		return super.getInfoString();
	}
	
	@Override
	public void interact(Player player) {
		player.interact(this);
	}
}
