package hitonoriol.madsand;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.math.Vector3;

import hitonoriol.madsand.containers.Line;
import hitonoriol.madsand.entities.Npc;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.enums.GameState;
import hitonoriol.madsand.enums.NpcType;
import hitonoriol.madsand.enums.Stat;
import hitonoriol.madsand.gui.widgets.GameTooltip;
import hitonoriol.madsand.map.Crop;
import hitonoriol.madsand.map.Loot;
import hitonoriol.madsand.map.Map;
import hitonoriol.madsand.map.MapObject;
import hitonoriol.madsand.map.ProductionStation;
import hitonoriol.madsand.map.Tile;
import hitonoriol.madsand.properties.TileProp;
import hitonoriol.madsand.world.World;

public class Mouse {
	public static int wx = 0; // Coords of the cell of map that mouse is currently pointing at
	public static int wy = 0;
	public static int clickDst = 0; // Distance from wx, wy to player
	public static int x = 0; // Coords of mouse cursor on the screen
	public static int y = 0;

	static Vector3 mouseinworld = new Vector3(0.0F, 0.0F, 0.0F);

	public static String lineDelimiter = "**********";

	public static Map loc;
	public static Tile tile;
	public static MapObject object;
	public static Npc npc;
	public static Loot loot;
	public static Crop crop;
	public static ProductionStation station;

	public static GameTooltip tooltipContainer;

	public static boolean pointingAtObject = false; // flag that shows if mouse is pointing at object or npc or not
	public static boolean justClicked = false;

	public static void updCoords() {
		x = Gdx.input.getX();
		y = Gdx.graphics.getHeight() - Gdx.input.getY();

		wx = (int) Math.floor(Mouse.mouseinworld.x / MadSand.TILESIZE);
		wy = (int) Math.floor(Mouse.mouseinworld.y / MadSand.TILESIZE);

		if (Gui.gameUnfocused || !MadSand.state.equals(GameState.GAME))
			return;

		loc = MadSand.world.getCurLoc();
		npc = loc.getNpc(wx, wy);
		tile = loc.getTile(wx, wy);
		object = loc.getObject(wx, wy);
		loot = loc.getLoot(wx, wy);
		crop = loc.getCrop(wx, wy);

		if (npc.type.equals(NpcType.FarmAnimal))
			station = npc.animalProductWorker;

		if (object.isProductionStation)
			station = loc.getProductionStation(wx, wy);
		else
			station = null;

		pointingAtObject = (npc != Map.nullNpc) || (object != Map.nullObject);

		tooltipContainer.moveTo(x, y);

		Gui.overlay.getTooltip().setText(getCurrentCellInfo());
	}

	public static String getCurrentCellInfo() {
		Player player = World.player;
		String info = "";
		info += ("Looking at (" + wx + ", " + wy + ")") + Resources.LINEBREAK;

		if (Utils.debugMode)
			info += getDebugInfo();

		if (wx == player.x && wy == player.y) {
			info += "You look at yourself" + Resources.LINEBREAK;
			info += player.getInfoString();
		}

		if (!tile.visible) {
			info += "You can't see anything there" + Resources.LINEBREAK;
			return info;
		}

		info += ("Tile: " + TileProp.getName(tile.id)) + Resources.LINEBREAK;

		if (!loot.equals(Map.nullLoot)) {
			info += "On the ground: ";
			info += loot.getInfo() + Resources.LINEBREAK;
		}

		if (!object.equals(Map.nullObject))
			info += ("Object: " + object.name) + Resources.LINEBREAK;

		if (station != null)
			info += getProdStationInfo(station);

		if (!crop.equals(Map.nullCrop))
			info += getCropInfo();

		if (!npc.equals(Map.nullNpc)) {
			info += ("You look at " + " " + npc.stats.name) + Resources.LINEBREAK;

			if (World.player.knowsNpc(npc.id))
				info += npc.getInfoString();

			if (!npc.friendly)
				info += npc.spottedMsg();

			if (npc.animalProductWorker != null)
				info += getProdStationInfo(npc.animalProductWorker);

		}

		return info;
	}

	private static String getProdStationInfo(ProductionStation station) {
		String info = "Status: ";
		String productName = station.getProductName();
		String rawMaterialName = station.getConsumableName();

		if (station.canProduce()) {
			info += "producing " + productName;
			info += " (" + Utils.round(station.productStorage) + "/" + station.maxProductStorage + ")";
			info += Resources.LINEBREAK;
			info += rawMaterialName + " left: " + Utils.round(station.consumableMaterialStorage);
		} else
			info += "Idle";

		info += Resources.LINEBREAK;

		if (!station.hasRawMaterial())
			info += "* Add more " + rawMaterialName + " to start " + productName + " production";
		else if (!station.hasFreeStorage())
			info += productName + " storage is full!";

		return info;
	}

	private static String getCropInfo() {
		long time = crop.getHarvestTime() * MadSand.world.realtimeTickRate;
		String info = (time <= 0)
				? ("* Ready to harvest!")
				: ("[#58FFB1]* Will fully grow in " + Utils.timeString(time) + "[]");
		return info;
	}

	private static String getDebugInfo() {
		String info = "[#C3C3C3]";
		info += lineDelimiter + Resources.LINEBREAK;
		info += "Debug Info:" + Resources.LINEBREAK;
		info += "Objects on map: " + loc.getObjectCount() + Resources.LINEBREAK;
		info += "NPCs on map: " + loc.getNpcCount() + Resources.LINEBREAK;
		if (!object.equals(Map.nullObject))
			info += "Object HP: " + object.hp + " | Object HarvestHp: " + object.harvestHp + Resources.LINEBREAK;
		if (!npc.equals(Map.nullNpc)) {
			info += "Npc hp: " + npc.stats.hp + Resources.LINEBREAK;
			info += "speed: " + npc.getSpeed() + "(" + npc.stats.get(Stat.Dexterity) + ")" + " | tickCharge: " + npc.tickCharge
					+ Resources.LINEBREAK;
		}
		info += lineDelimiter + Resources.LINEBREAK;
		info += "[]";
		return info;

	}

	private static final int CLICK_ACTION_REST = 0;
	private static final int CLICK_ACTION_DIAGONAL = 1;
	private static boolean rest, diagonal;

	private static void processClick() {
		clickDst = (int) Line.calcDistance(World.player.x, World.player.y, wx, wy);
		diagonal = (clickDst == CLICK_ACTION_DIAGONAL);
		rest = (clickDst == CLICK_ACTION_REST);
	}

	public static void mouseClickAction() {
		processClick();

		if (MadSand.state != GameState.GAME)
			return;

		if ((World.player.isStepping()) || Gui.isGameUnfocused())
			return;

		if (justClicked && ((pointingAtObject && diagonal) || rest)) {
			justClicked = false;

			World.player.lookAtMouse(wx, wy, diagonal);

			if (rest && loot != Map.nullLoot) {
				World.player.pickUpLoot();
				return;
			} else if (rest) {
				World.player.rest();
				return;
			}

			if (pointingAtObject && !npc.equals(Map.nullNpc)) {
				if (!npc.friendly) {
					World.player.attack();
					return;
				}
			}

			if (pointingAtObject && diagonal) { // Interact with friendly npc or object
				World.player.interact();
				return;
			}

		}

		justClicked = false;
	}

	public static void pollMouseMovement() {
		if (Keyboard.inputIgnored())
			return;

		processClick();
		if (Gdx.input.isButtonPressed(Buttons.LEFT) && !(diagonal && pointingAtObject) && !rest) {
			World.player.lookAtMouse(wx, wy);
			World.player.walk(World.player.stats.look);
		}
	}

}
