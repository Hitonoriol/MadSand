package hitonoriol.madsand;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

public class LuaUtils {
	public static Globals globals = JsePlatform.standardGlobals();
	
	public static void init() {
		LuaValue luaWorld = CoerceJavaToLua.coerce(MadSand.world);
		
		globals.set("world", luaWorld);
	}

	public static LuaValue executeScript(String file) {
		return globals.loadfile(MadSand.SCRIPTDIR + file).call();
	}
	
	public static final String initScript = "map_init_newgame.lua";
}
