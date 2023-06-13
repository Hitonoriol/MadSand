package hitonoriol.madsand.entities.npc;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.TimeDependent;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.gamecontent.NpcDescriptor;
import hitonoriol.madsand.gui.textgenerator.CellInfoGenerator;
import hitonoriol.madsand.map.ItemProducer;
import hitonoriol.madsand.util.Strings;
import me.xdrop.jrand.JRand;
import me.xdrop.jrand.generators.basics.NaturalGenerator;

public class FarmAnimal extends AbstractNpc implements TimeDependent {
	private ItemProducer animalProduct;
	private long lastFed = MadSand.world().currentActionTick();
	private static final long STARVE_TIME = MadSand.world().timeToActionTicks(1800);
	private static NaturalGenerator initialFoodGen = JRand.natural().range(0, 30);

	public FarmAnimal(NpcDescriptor protoNpc) {
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
	protected void despawnProcess() {
		if (isStarving() && !stats.luckRoll())
			damage(1);
	}

	@Override
	protected void live(float time) {
		if (isStarving())
			super.live(time);
	}

	public boolean isStarving() {
		return MadSand.world().currentActionTick() - lastFed >= STARVE_TIME;
	}

	@Override
	public void update() {
		animalProduct.produce();
		if (animalProduct.hasRawMaterial()) {
			lastFed = MadSand.world().currentActionTick();
			if (canBeDespawned())
				addLifetime();
		}
	}

	@Override
	public String getInfoString() {
		var sb = new StringBuilder();
		CellInfoGenerator.getItemProducerInfo(sb, animalProduct);

		if (isStarving()) {
			Strings.newLine(sb);
			sb.append("* Starving");
		}

		return sb.append(super.getInfoString()).toString();
	}
}
