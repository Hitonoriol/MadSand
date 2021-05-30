package hitonoriol.madsand.gui.textgenerator;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.Stat;
import hitonoriol.madsand.entities.npc.AbstractNpc;
import hitonoriol.madsand.map.Crop;
import hitonoriol.madsand.map.ItemProducer;
import hitonoriol.madsand.map.Loot;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.Tile;
import hitonoriol.madsand.map.object.ItemFactory;
import hitonoriol.madsand.map.object.MapObject;
import hitonoriol.madsand.pathfinding.Node;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.TileProp;
import hitonoriol.madsand.util.Utils;
import hitonoriol.madsand.world.World;

public class CellInfoGenerator extends TooltipTextGenerator {
	private int x, y;

	private Map loc;
	private Player player;
	private Tile tile;
	private MapObject object;
	private AbstractNpc npc;
	private Loot loot;
	private Crop crop;
	private ItemProducer station;
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

	@Override
	public void update(int x, int y) {
		this.x = x;
		this.y = y;
		player = World.player;
		loc = MadSand.world.getCurLoc();
		npc = loc.getNpc(x, y);
		tile = loc.getTile(x, y);
		object = loc.getObject(x, y);
		loot = loc.getLoot(x, y);
		crop = loc.getCrop(x, y);
		node = loc.getNode(x, y);

		station = null;
		object.as(ItemFactory.class)
				.ifPresent(itemFactory -> station = itemFactory.getItemProducer());
	}

	public boolean isCellOccupied() {
		return npc != Map.nullNpc ||
				object != Map.nullObject ||
				tile.hasFishingSpot() ||
				player.at(x, y);
	}

	public AbstractNpc getNpc() {
		return npc;
	}

	public boolean hasNpc() {
		return !npc.equals(Map.nullNpc);
	}

	public Loot getLoot() {
		return loot;
	}

	public MapObject getObject() {
		return object;
	}

	@Override
	public String getText() {
		clearBuilder();
		if (Globals.debugMode)
			getDebugInfo();

		if (player.at(x, y))
			getPlayerInfo();

		if (!tile.visible) {
			addLine("You can't see anything there");
			return super.getText();
		}

		addLine("Tile: " + TileProp.getName(tile.id));

		if (!loot.equals(Map.nullLoot))
			addLine("On the ground: %s", loot.getInfo());

		if (!object.equals(Map.nullObject))
			addLine("Object: " + object.name + " (" + Utils.round(object.getHpPercent()) + "%)");

		if (station != null)
			getItemProducerInfo(builder, station);

		if (!crop.equals(Map.nullCrop))
			getCropInfo();

		if (!npc.equals(Map.nullNpc))
			getNpcInfo();

		if (player.canPerformRangedAttack() && npc != Map.nullNpc)
			getRangedAttackInfo();

		return super.getText();
	}

	private void getRangedAttackInfo() {
		int dst = player.distanceTo(npc);
		addLine("Distance to target: " + dst + " meters")
				.addLine("Chance to miss: " + Utils.round(player.stats.calcRangedAttackMissChance(dst)) + "%");
	}

	private void getNpcInfo() {
		addLine(npc.stats.name + " (Level " + npc.getLvl() + ")")
				.addLine(
						(npc.isNeutral()
								? "Neutral"
								: "Hostile"));

		addLine(npc.getInfoString());

		if (npc.state == AbstractNpc.State.Hostile)
			addLine(npc.spottedMsg());

	}

	private void getPlayerInfo() {
		addLine("You look at yourself")
				.addLine(player.getInfoString());
	}

	public static void getItemProducerInfo(StringBuilder builder, ItemProducer producer) {
		builder.append("Status: ");
		String productName = producer.getProductName();
		String rawMaterialName = producer.getConsumableName();

		if (producer.canProduce()) {
			builder.append("producing " + productName)
					.append(" (" + Utils.round(producer.productStorage) + "/" + producer.maxProductStorage + ")")
					.append(NEWLINE);
			if (!producer.isEndless())
				builder.append(rawMaterialName + " left: " + Utils.round(producer.consumableMaterialStorage));
		} else
			builder.append("Idle");

		Utils.newLine(builder);

		if (!producer.hasRawMaterial())
			builder.append("* Add more " + rawMaterialName + " to start " + productName + " production");
		else if (!producer.hasFreeStorage())
			builder.append(productName + " storage is full!");
	}

	private void getCropInfo() {
		long time = (long) (crop.getHarvestTime() * MadSand.world.getRealtimeTickRate());
		addLine((time <= 0)
				? ("* Ready to harvest!")
				: ("[#58FFB1]* Will fully grow in " + Utils.timeString(time) + "[]"));
	}

	private void getDebugInfo() {
		addLine("[#C3C3C3]" + lineDelimiter)
				.addLine("Objects on map: " + loc.getObjectCount())
				.addLine("NPCs on map: " + loc.getNpcCount());
		if (!object.equals(Map.nullObject))
			addLine("Object HP: " + object.hp + " | Object HarvestHp: " + object.harvestHp);
		if (!npc.equals(Map.nullNpc)) {
			addLine("Npc hp: " + npc.stats.hp)
					.addLine("speed: " + npc.getSpeed() + "(" + npc.stats.get(Stat.Dexterity) + ")" + " | tickCharge: "
							+ npc.tickCharge);
		}

		getPathNodeInfo();
		addLine(lineDelimiter + "[]");
	}

	private void getPathNodeInfo() {
		if (node == null)
			return;

		addLine("Node: " + node);
	}
}
