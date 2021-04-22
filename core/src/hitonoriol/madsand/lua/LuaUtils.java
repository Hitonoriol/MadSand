package hitonoriol.madsand.lua;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.enums.TradeCategory;
import hitonoriol.madsand.properties.NpcProp;
import hitonoriol.madsand.util.Utils;

public class LuaUtils {
	public static Pair locateTile(int id) {
		return MadSand.world.getCurLoc().locateTile(id);
	}

	public static void placeTile(int x, int y, int id) {
		MadSand.world.getCurLoc().addTile(x, y, id, true);
	}

	public static void delObject(int x, int y) {
		MadSand.world.getCurLoc().delObject(x, y);
	}

	public static void addObject(int x, int y, int id) {
		MadSand.world.getCurLoc().addObject(x, y, id);
	}

	public static void descend() {
		MadSand.world.descend();
	}

	public static void ascend() {
		MadSand.world.ascend();
	}

	public static void showDialog(String query) {
		GameDialog.generateDialogChain(query, Gui.overlay).show();
	}

	public static void showOkDialog(String text) {
		Gui.drawOkDialog(text);
	}

	public static void print(String msg) {
		MadSand.print(msg);
	}

	public static void notice(String msg) {
		MadSand.notice(msg);
	}
	
	public static void warn(String msg) {
		MadSand.warn(msg);
	}

	public static int oneOf(String stringList) {
		return Utils.oneOf(stringList);
	}

	public static TradeCategory lootCategory(String name) {
		return TradeCategory.valueOf(name);
	}

	public static int rollLoot(TradeCategory category, int tier) {
		return NpcProp.tradeLists.rollId(category, tier);
	}

	public static int rollLoot(TradeCategory category) {
		return rollLoot(category, -1);
	}

	public static String getScriptName(String regularText) {
		return regularText.toLowerCase().replace(' ', '-') + ".lua";
	}
}
