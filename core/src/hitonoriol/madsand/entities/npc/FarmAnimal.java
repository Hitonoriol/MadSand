package hitonoriol.madsand.entities.npc;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.TimeDependent;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.gui.textgenerator.CellInfoGenerator;
import hitonoriol.madsand.map.ItemProducer;
import hitonoriol.madsand.properties.NpcContainer;
import hitonoriol.madsand.util.Utils;
import me.xdrop.jrand.JRand;
import me.xdrop.jrand.generators.basics.NaturalGenerator;

public class FarmAnimal extends AbstractNpc implements TimeDependent {
	private ItemProducer animalProduct;
	private long lastFed = MadSand.world().currentActionTick();
	private static final long STARVE_TIME = MadSand.world().timeToActionTicks(600);
	private static NaturalGenerator initialFoodGen = JRand.natural().range(0, 30);

	public FarmAnimal(NpcContainer protoNpc) {
		super(protoNpc);
		animalProduct = new ItemProducer(-id);
		animalProduct.addRawMaterial(initialFoodGen.gen());
	}

	public FarmAnimal() {}

	@Override
	public void interact(Player player) {
		player.interact(this);
	}

	public ItemProducer getItemProducer() {
		return animalProduct;
	}

	@Override
	public int doAction(double ap) {
		if (isStarving())
			damage(1);
		return super.doAction(ap);
	}

	public boolean isStarving() {
		return MadSand.world().currentActionTick() - lastFed >= STARVE_TIME;
	}

	@Override
	public void update() {
		animalProduct.produce();
		if (animalProduct.hasRawMaterial())
			lastFed = MadSand.world().currentActionTick();
	}

	@Override
	public String getInfoString() {
		StringBuilder sb = new StringBuilder();
		CellInfoGenerator.getItemProducerInfo(sb, animalProduct);
		Utils.newLine(sb);

		if (isStarving())
			Utils.newLine(sb.append("* Starving"));

		return sb.append(super.getInfoString()).toString();
	}
}
