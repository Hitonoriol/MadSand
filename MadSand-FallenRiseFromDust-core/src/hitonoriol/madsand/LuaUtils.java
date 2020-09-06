package hitonoriol.madsand;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import hitonoriol.madsand.dialog.GameDialog;
import hitonoriol.madsand.properties.Tutorial;

public class LuaUtils {
	public static Globals globals = JsePlatform.standardGlobals();

	public static String onAction;

	public static final String initScript = "map_init_newgame.lua";
	public static final String onActionScript = "player_onaction.lua";
	public static final String onCreationScript = "player_oncreation.lua";

	public static void init() {
		LuaValue luaWorld = CoerceJavaToLua.coerce(MadSand.world);
		LuaValue luaTutorial = CoerceJavaToLua.coerce(new Tutorial());
		LuaValue luaUtils = CoerceJavaToLua.coerce(new LuaUtils());

		globals.set("world", luaWorld);
		globals.set("tutorial", luaTutorial);
		globals.set("utils", luaUtils);

		onAction = GameSaver.getExternal(MadSand.SCRIPTDIR + onActionScript, true);
	}

	public static LuaValue executeScript(String file) {
		return globals.loadfile(MadSand.SCRIPTDIR + file).call();
	}

	public static LuaValue execute(String str) {
		return globals.load(str).call();
	}

	public static void placeTile(int x, int y, int id) { // For some reason calling world:getCurLoc():addTile(...) from lua is not working
		MadSand.world.getCurLoc().addTile(x, y, id, true);
	}

	public static void showDialog(String query) {
		GameDialog.generateDialogChain(query, Gui.overlay).show();
	}

	public static void print(String msg) {
		MadSand.print(msg);
	}

	public static void notice(String msg) {
		MadSand.notice(msg);
	}
}
