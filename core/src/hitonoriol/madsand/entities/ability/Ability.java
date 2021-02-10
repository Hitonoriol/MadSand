package hitonoriol.madsand.entities.ability;

import hitonoriol.madsand.DynamicallyCastable;
import hitonoriol.madsand.LuaUtils;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.world.World;

public abstract class Ability implements DynamicallyCastable<Ability> {
	public int id;
	public int lvl = 1;
	public String name;
	public String script;

	public void apply() {
		LuaUtils.executeScript("ability/" + script, this, World.player);
	}
	
	public static Ability get(int id) {
		return Globals.instance().abilities.get(id);
	}
}
