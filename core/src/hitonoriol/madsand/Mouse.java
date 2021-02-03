package hitonoriol.madsand;

import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.math.Vector3;

import hitonoriol.madsand.containers.Line;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.Stat;
import hitonoriol.madsand.entities.npc.AbstractNpc;
import hitonoriol.madsand.gui.widgets.GameTooltip;
import hitonoriol.madsand.map.Crop;
import hitonoriol.madsand.map.Loot;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.ItemProducer;
import hitonoriol.madsand.map.Tile;
import hitonoriol.madsand.map.object.ItemFactory;
import hitonoriol.madsand.map.object.MapObject;
import hitonoriol.madsand.properties.TileProp;
import hitonoriol.madsand.world.World;

public class Mouse {
	public static int x = 0, y = 0; // Coords of mouse cursor on the screen
	public static Pair prevCoords = new Pair();
	public static int wx = 0, wy = 0; // Coords of the cell of map that mouse is currently pointing at
	public static Set<Integer> heldButtons = new HashSet<>();

	public static Vector3 mouseWorldCoords = new Vector3(0.0F, 0.0F, 0.0F);

	public static String lineDelimiter = "**********";
	static final String NEWLINE = Resources.LINEBREAK;

	public static Map loc;
	public static Player player;
	public static Tile tile;
	public static MapObject object;
	public static AbstractNpc npc;
	public static Loot loot;
	public static Crop crop;
	public static ItemProducer station;

	public static GameTooltip tooltipContainer;

	private static boolean pointingAtObject = false;

	public static void updCoords() {
		x = Gdx.input.getX();
		y = Gdx.graphics.getHeight() - Gdx.input.getY();
		tooltipContainer.moveTo(x, y);

		wx = (int) Math.floor(Mouse.mouseWorldCoords.x / MadSand.TILESIZE);
		wy = (int) Math.floor(Mouse.mouseWorldCoords.y / MadSand.TILESIZE);

		if (prevCoords.equals(wx, wy))
			return;
		prevCoords.set(wx, wy);

		if (Gui.gameUnfocused)
			return;

		player = World.player;
		loc = MadSand.world.getCurLoc();
		npc = loc.getNpc(wx, wy);
		tile = loc.getTile(wx, wy);
		object = loc.getObject(wx, wy);
		loot = loc.getLoot(wx, wy);
		crop = loc.getCrop(wx, wy);

		station = null;
		object.as(ItemFactory.class)
				.ifPresent(itemFactory -> station = itemFactory.itemProducer);

		pointingAtObject = npc != Map.nullNpc ||
				object != Map.nullObject ||
				tile.hasFishingSpot() ||
				player.at(wx, wy);

		Gui.overlay.getTooltip().setText(getCurrentCellInfo());
	}

	private static StringBuilder infoBuilder = new StringBuilder();

	public static String getCurrentCellInfo() {
		infoBuilder.setLength(0);
		infoBuilder.append("Looking at (" + wx + ", " + wy + ")").append(NEWLINE);

		if (Utils.debugMode)
			getDebugInfo();

		if (player.at(wx, wy))
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
			getProdStationInfo(station);

		if (!crop.equals(Map.nullCrop))
			getCropInfo();

		if (!npc.equals(Map.nullNpc))
			getNpcInfo();

		if (player.canPerformRangedAttack() && npc != Map.nullNpc)
			getRangedAttackInfo();

		return infoBuilder.toString();
	}

	private static void getRangedAttackInfo() {
		int dst = player.distanceTo(npc);
		infoBuilder.append("Distance to target: " + dst + " meters")
				.append(NEWLINE)
				.append("Chance to miss: " + Utils.round(player.stats.calcRangedAttackMissChance(dst)) + "%")
				.append(NEWLINE);
	}

	private static void getNpcInfo() {
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

	private static void getPlayerInfo() {
		infoBuilder.append("You look at yourself")
				.append(NEWLINE)
				.append(player.getInfoString());
	}

	public static void getProdStationInfo(ItemProducer station) {
		infoBuilder.append("Status: ");
		String productName = station.getProductName();
		String rawMaterialName = station.getConsumableName();

		if (station.canProduce()) {
			infoBuilder.append("producing " + productName)
					.append(" (" + Utils.round(station.productStorage) + "/" + station.maxProductStorage + ")")
					.append(NEWLINE);
			if (!station.isEndless())
				infoBuilder.append(rawMaterialName + " left: " + Utils.round(station.consumableMaterialStorage));
		} else
			infoBuilder.append("Idle");

		Utils.newLine(infoBuilder);

		if (!station.hasRawMaterial())
			infoBuilder.append("* Add more " + rawMaterialName + " to start " + productName + " production");
		else if (!station.hasFreeStorage())
			infoBuilder.append(productName + " storage is full!");
	}

	private static void getCropInfo() {
		long time = (long) (crop.getHarvestTime() * MadSand.world.realtimeTickRate);
		infoBuilder.append(
				(time <= 0)
						? ("* Ready to harvest!")
						: ("[#58FFB1]* Will fully grow in " + Utils.timeString(time) + "[]"));
	}

	private static void getDebugInfo() {
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
		infoBuilder.append(lineDelimiter).append(NEWLINE).append("[]");
	}

	private static final int CLICK_CUR_TILE = 0, CLICK_ADJ_TILE = 1;

	public static int getClickDistance() {
		return (int) Line.calcDistance(World.player.x, World.player.y, wx, wy);
	}

	public static boolean isClickActionPossible() {
		int clickDst = getClickDistance();
		return pointingAtObject ||
				clickDst == CLICK_CUR_TILE;
	}

	public static void mouseClickAction() {
		int clickDst = getClickDistance();
		boolean adjacentTileClicked = (clickDst == CLICK_ADJ_TILE);
		boolean currentTileClicked = (clickDst == CLICK_CUR_TILE);

		if ((player.isStepping()) || Gui.isGameUnfocused() || !pointingAtObject) {
			Utils.out("CUM");
			return;
		}

		if (currentTileClicked) {
			if (loot != Map.nullLoot)
				player.pickUpLoot();
			else
				player.rest();
		}

		else if (adjacentTileClicked) {
			player.lookAtMouse(wx, wy, true);

			if (npc.state == AbstractNpc.State.Hostile)
				player.meleeAttack();
			else
				player.interact();
		}

		else if (clickDst > 1 && npc != Map.nullNpc)
			player.rangedAttack(npc);
	}

	public static void pollMouseMovement() {
		if (Keyboard.inputIgnored())
			return;

		if (heldButtons.contains(Buttons.LEFT)) {
			World.player.lookAtMouse(wx, wy);
			World.player.walk(World.player.stats.look);
		}
	}

}
