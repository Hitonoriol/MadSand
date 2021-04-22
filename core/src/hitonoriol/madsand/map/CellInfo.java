package hitonoriol.madsand.map;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.Stat;
import hitonoriol.madsand.entities.npc.AbstractNpc;
import hitonoriol.madsand.map.object.ItemFactory;
import hitonoriol.madsand.map.object.MapObject;
import hitonoriol.madsand.pathfinding.Node;
import hitonoriol.madsand.properties.TileProp;
import hitonoriol.madsand.util.Utils;
import hitonoriol.madsand.world.World;

public class CellInfo {
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

	private StringBuilder infoBuilder = new StringBuilder();

	public static String lineDelimiter = "**********";
	static final String NEWLINE = Resources.LINEBREAK;

	public CellInfo(int x, int y) {
		set(x, y);
	}

	public CellInfo() {
	}

	public void set(int x, int y) {
		this.x = x;
		this.y = y;
		refresh();
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}

	private void refresh() {
		player = World.player;
		loc = MadSand.world.getCurLoc();
		npc = loc.getNpc(x, y);
		tile = loc.getTile(x, y);
		object = loc.getObject(x, y);
		loot = loc.getLoot(x, y);
		crop = loc.getCrop(x, y);
		node = loc.nodeMap.get(x, y);

		station = null;
		object.as(ItemFactory.class)
				.ifPresent(itemFactory -> station = itemFactory.itemProducer);
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

	public String getInfo() {
		infoBuilder.setLength(0);
		infoBuilder.append("Looking at (" + x + ", " + y + ")").append(NEWLINE);

		if (Utils.debugMode)
			getDebugInfo();

		if (player.at(x, y))
			getPlayerInfo();

		if (!tile.visible) {
			infoBuilder.append("You can't see anything there").append(NEWLINE);
			return infoBuilder.toString();
		}

		infoBuilder.append("Tile: " + TileProp.getName(tile.id)).append(NEWLINE);

		if (!loot.equals(Map.nullLoot)) {
			infoBuilder.append("On the ground: ");
			infoBuilder.append(loot.getInfo()).append(NEWLINE);
		}

		if (!object.equals(Map.nullObject))
			infoBuilder.append("Object: " + object.name + " (" + Utils.round(object.getHpPercent()) + "%)")
					.append(NEWLINE);

		if (station != null)
			getProdStationInfo(infoBuilder, station);

		if (!crop.equals(Map.nullCrop))
			getCropInfo();

		if (!npc.equals(Map.nullNpc))
			getNpcInfo();

		if (player.canPerformRangedAttack() && npc != Map.nullNpc)
			getRangedAttackInfo();

		return infoBuilder.toString();
	}

	private void getRangedAttackInfo() {
		int dst = player.distanceTo(npc);
		infoBuilder.append("Distance to target: " + dst + " meters")
				.append(NEWLINE)
				.append("Chance to miss: " + Utils.round(player.stats.calcRangedAttackMissChance(dst)) + "%")
				.append(NEWLINE);
	}

	private void getNpcInfo() {
		infoBuilder.append(npc.stats.name + " (Level " + npc.getLvl() + ")")
				.append(NEWLINE)
				.append(
						(npc.isNeutral()
								? "Neutral"
								: "Hostile"))
				.append(NEWLINE);

		infoBuilder.append(npc.getInfoString());

		if (npc.state == AbstractNpc.State.Hostile)
			infoBuilder.append(npc.spottedMsg()).append(NEWLINE);

	}

	private void getPlayerInfo() {
		infoBuilder.append("You look at yourself")
				.append(NEWLINE)
				.append(player.getInfoString());
	}

	public static void getProdStationInfo(StringBuilder builder, ItemProducer station) {
		builder.append("Status: ");
		String productName = station.getProductName();
		String rawMaterialName = station.getConsumableName();

		if (station.canProduce()) {
			builder.append("producing " + productName)
					.append(" (" + Utils.round(station.productStorage) + "/" + station.maxProductStorage + ")")
					.append(NEWLINE);
			if (!station.isEndless())
				builder.append(rawMaterialName + " left: " + Utils.round(station.consumableMaterialStorage));
		} else
			builder.append("Idle");

		Utils.newLine(builder);

		if (!station.hasRawMaterial())
			builder.append("* Add more " + rawMaterialName + " to start " + productName + " production");
		else if (!station.hasFreeStorage())
			builder.append(productName + " storage is full!");
	}

	private void getCropInfo() {
		long time = (long) (crop.getHarvestTime() * MadSand.world.realtimeTickRate);
		infoBuilder.append(
				(time <= 0)
						? ("* Ready to harvest!")
						: ("[#58FFB1]* Will fully grow in " + Utils.timeString(time) + "[]"));
	}

	private void getDebugInfo() {
		infoBuilder.append("[#C3C3C3]")
				.append(lineDelimiter)
				.append(NEWLINE)
				.append("Objects on map: " + loc.getObjectCount()).append(NEWLINE)
				.append("NPCs on map: " + loc.getNpcCount()).append(NEWLINE);
		if (!object.equals(Map.nullObject))
			infoBuilder.append("Object HP: " + object.hp + " | Object HarvestHp: " + object.harvestHp)
					.append(NEWLINE);
		if (!npc.equals(Map.nullNpc)) {
			infoBuilder.append("Npc hp: " + npc.stats.hp).append(NEWLINE)
					.append("speed: " + npc.getSpeed() + "(" + npc.stats.get(Stat.Dexterity) + ")" + " | tickCharge: "
							+ npc.tickCharge)
					.append(NEWLINE);
		}

		getPathNodeInfo();

		infoBuilder.append(lineDelimiter).append(NEWLINE).append("[]");
	}

	private void getPathNodeInfo() {
		if (node == null)
			return;

		infoBuilder.append("Node: " + node)
				.append(NEWLINE);
	}
}
