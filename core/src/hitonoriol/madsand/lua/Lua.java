package hitonoriol.madsand.lua;

import java.util.stream.Stream;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import hitonoriol.madsand.GameSaver;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Resources;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.input.Mouse;
import hitonoriol.madsand.properties.Tutorial;
import hitonoriol.madsand.util.Utils;

public class Lua {

	public static Globals globals;
	public static Runnable onAction;

	public static final String initScript = "map_init_newgame.lua";
	public static final String onActionScript = "player_onaction.lua";
	public static final String onCreationScript = "player_oncreation.lua";
	public static final String offlineRewardScript = "offline_reward.lua";

	static {
		reinitGlobals();
	}

	public static void init() {
		register("lua", CoerceJavaToLua.coerce(new Lua()));
		register("utils", CoerceJavaToLua.coerce(new LuaUtils()));
		register("val_utils", CoerceJavaToLua.coerce(new Utils()));
		register("lambda", CoerceJavaToLua.coerce(new LuaLambda()));
		register("pair", CoerceJavaToLua.coerce(Pair.getInstance()));
		register("item", CoerceJavaToLua.coerce(new Item()));
		register("mouse", CoerceJavaToLua.coerce(new Mouse()));
		register("world", CoerceJavaToLua.coerce(MadSand.world()));
		register("player", CoerceJavaToLua.coerce(MadSand.player()));
		register("tutorial", CoerceJavaToLua.coerce(new Tutorial()));

		executeScript("globals.lua", Resources.SCRIPT_DIR);
		onAction = scriptAsRunnable(onActionScript);
	}

	private static void reinitGlobals() {
		globals = JsePlatform.standardGlobals();
	}

	public static void register(String luaName, LuaValue object) {
		globals.set(luaName, object);
	}

	public static LuaValue executeScript(String file) {
		return callChunk(loadScript(file));
	}

	public static LuaValue executeScript(String file, Object arg) {
		return callChunk(loadScript(file), CoerceJavaToLua.coerce(arg));
	}

	public static Varargs executeScript(String file, Object... args) {
		return callChunk(loadScript(file), args);
	}

	public static Runnable scriptAsRunnable(String file) {
		return LuaLambda.runnable(loadScript(file));
	}

	public static LuaValue execute(String str) {
		return callChunk(loadChunk(str));
	}

	public static LuaValue execute(String str, Object arg) {
		return callChunk(loadChunk(str), arg);
	}

	public static LuaValue loadChunk(String chunk) {
		return globals.load(chunk);
	}

	public static LuaValue loadScript(String file) {
		return loadChunk(Resources.readInternal(Resources.SCRIPT_DIR + file));
	}

	public static LuaValue callChunk(LuaValue chunk) {
		return chunk.call();
	}

	public static LuaValue callChunk(LuaValue chunk, Object arg) {
		return chunk.call(CoerceJavaToLua.coerce(arg));
	}

	public static Varargs callChunk(LuaValue chunk, Object... args) {
		LuaValue[] luaArgs = Stream.of(args)
				.map(arg -> CoerceJavaToLua.coerce(arg))
				.toArray(LuaValue[]::new);
		return chunk.invoke(LuaValue.varargsOf(luaArgs));
	}

	public static String getSectorScriptPath(int wx, int wy) {
		return "/location/" + GameSaver.SECTOR_DELIM + wx + GameSaver.SECTOR_DELIM + wy + ".lua";
	}

}
