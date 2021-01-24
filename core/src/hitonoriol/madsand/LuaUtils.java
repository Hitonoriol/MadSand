package hitonoriol.madsand;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.enums.TradeCategory;
import hitonoriol.madsand.properties.NpcProp;
import hitonoriol.madsand.properties.Tutorial;

public class LuaUtils {
	public static Globals globals;

	public static String onAction;

	public static final String initScript = "map_init_newgame.lua";
	public static final String onActionScript = "player_onaction.lua";
	public static final String onCreationScript = "player_oncreation.lua";
	public static final String offlineRewardScript = "offline_reward.lua";

	public static void init() {
		globals = JsePlatform.standardGlobals();

		globals.set("world", CoerceJavaToLua.coerce(MadSand.world));
		globals.set("tutorial", CoerceJavaToLua.coerce(new Tutorial()));
		globals.set("utils", CoerceJavaToLua.coerce(new LuaUtils()));
		globals.set("val_utils", CoerceJavaToLua.coerce(new Utils()));
		globals.set("pair", CoerceJavaToLua.coerce(Pair.getInstance()));

		executeScript("globals.lua", Resources.SCRIPT_DIR);

		onAction = Resources.readInternal(Resources.SCRIPT_DIR + onActionScript);
	}

	public static void register(String luaName, LuaValue object) {
		globals.set(luaName, object);
	}

	public static LuaValue loadScript(String file) {
		return globals.load(Resources.readInternal(Resources.SCRIPT_DIR + file));
	}

	public static LuaValue executeScript(String file, Object arg) {
		return loadScript(file).call(LuaValue.valueOf(String.valueOf(arg)));
	}

	public static LuaValue executeScript(String file) {
		return loadScript(file).call();
	}

	public static LuaValue execute(String str) {
		return globals.load(str).call();
	}

	public static String getSectorScriptPath(int wx, int wy) {
		return "/location/" + GameSaver.SECTOR_DELIM + wx + GameSaver.SECTOR_DELIM + wy + ".lua";
	}

	public static Pair locateTile(int id) {
		return MadSand.world.getCurLoc().locateTile(id);
	}

	public static void placeTile(int x, int y, int id) { // For some reason calling world:getCurLoc():addTile(...) from lua is not working
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
}