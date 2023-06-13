package hitonoriol.madsand.gui.textgenerator;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.Stat;
import hitonoriol.madsand.gamecontent.Globals;
import hitonoriol.madsand.map.ItemProducer;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.MapCell;
import hitonoriol.madsand.pathfinding.Node;
import hitonoriol.madsand.resources.Resources;
import hitonoriol.madsand.util.Strings;
import hitonoriol.madsand.util.Utils;

public class CellInfoGenerator extends TextGenerator {
	private int x, y;

	private Player player;
	private Map map;
	private final MapCell cell = new MapCell();
	private Node node;

	public static String lineDelimiter = "**********";
	static final String NEWLINE = Resources.LINEBREAK;

	public CellInfoGenerator(int x, int y) {
		update(x, y);
	}

	public CellInfoGenerator() {}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public boolean pointsAt(int x, int y) {
		return this.x == x && this.y == y;
	}

	@Override
	public void update(int x, int y) {
		this.x = x;
		this.y = y;
		player = MadSand.player();
		map = MadSand.world().getCurLoc();
		map.getMapCell(cell.setCoords(x, y));
		node = map.getPathfindingEngine().getNode(x, y);
	}

	public MapCell getCell() {
		return cell;
	}

	@Override
	public String getText() {
		clearBuilder();
		if (Globals.debugMode)
			getDebugInfo();

		if (player.at(x, y))
			getPlayerInfo();

		if (!cell.coordsValid() || !cell.getTile().visible()) {
			addLine("You can't see anything there");
			return super.getText();
		}

		addLine("Tile: " + cell.getTile().name);

		if (cell.hasLoot())
			addLine("On the ground: %s", cell.getLoot().getInfo());

		if (cell.hasObject()) {
			var object = cell.getObject();
			addLine("Object: " + object.name + " (" + Utils.round(object.getHpPercent()) + "%)");
		}

		if (cell.hasItemFactory())
			getItemProducerInfo(builder, cell.getItemFactoryProducer());

		if (cell.hasCrop())
			getCropInfo();

		if (cell.hasNpc())
			getNpcInfo();

		if (player.canPerformRangedAttack() && cell.hasNpc())
			getRangedAttackInfo();

		return super.getText();
	}

	private void getRangedAttackInfo() {
		int dst = player.distanceTo(cell.getNpc());
		addLine("Distance to target: " + dst + " meters")
			.addLine("Chance to miss: " + Utils.round(player.stats.calcRangedAttackMissChance(dst)) + "%");
	}

	private void getNpcInfo() {
		var npc = cell.getNpc();
		addLine(npc.stats.name + " (Level " + npc.getLvl() + ")")
			.addLine(
				(npc.isNeutral()
					? "Neutral"
					: "Hostile")
			);
		addLine(npc.getInfoString());
	}

	private void getPlayerInfo() {
		addLine("You look at yourself")
			.addLine(player.getInfoString());
	}

	public static void getItemProducerInfo(StringBuilder builder, ItemProducer producer) {
		builder.append("Status: ");
		var productName = producer.getProductName();
		var rawMaterialName = producer.getConsumableName();

		if (producer.canProduce()) {
			builder.append("producing " + productName)
				.append(
					" (" + Utils.round(producer.getProductStorage()) + "/" + producer.getMaxProductStorage()
						+ ")"
				)
				.append(NEWLINE);
			if (!producer.isEndless())
				builder.append(rawMaterialName + " left: " + Utils.round(producer.getConsumedMaterialStorage()));
		} else
			builder.append("Idle");

		Strings.newLine(builder);

		if (!producer.hasRawMaterial()) {
			builder.append("* Add more " + rawMaterialName + " to start " + productName + " production");
			Strings.newLine(builder);
		} else if (!producer.hasFreeStorage())
			builder.append(productName + " storage is full!");
	}

	private void getCropInfo() {
		var time = (long) (cell.getCrop().getHarvestTime() * MadSand.world().getRealtimeActionSeconds());
		addLine(
			(time <= 0)
				? ("* Ready to harvest!")
				: ("[#58FFB1]* Will fully grow in " + Utils.timeString(time) + "[]")
		);
	}

	Pair coords = new Pair();

	private void getDebugInfo() {
		var tile = cell.getTile();
		var object = cell.getObject();
		var npc = cell.getNpc();
		addLine("[#C3C3C3]" + lineDelimiter);

		addLine("Light level: " + tile.getLightLevel() + " (sky: " + MadSand.world().getSkyLight() + ")");
		var emitterInfo = map.getLightEngine().getEntityLight(coords.set(x, y));
		if (!emitterInfo.isEmpty())
			addLine(emitterInfo);

		if (cell.hasObject())
			addLine("Object HP: " + object.hp + " (" + object.harvestHp + ")")
				.addLine("Luminosity: " + object.getLuminosity());

		if (cell.hasNpc())
			addLine("UID: " + npc.uid())
				.addLine("HP: " + npc.stats.hp)
				.addLine("Speed: " + npc.getSpeed() + "(" + npc.stats.get(Stat.Dexterity) + ")")
				.addLine("Lifetime: " + Utils.round(npc.getLifetime()));

		if (node != null)
			addLine("Node: " + node);
		addLine(lineDelimiter + "[]");
	}

	@Override
	public String toString() {
		return String.format(
			"[%d, %d] Object: %s",
			x, y,
			cell.getObject()
		);
	}
}
