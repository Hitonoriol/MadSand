package hitonoriol.madsand.entities.ability;

import hitonoriol.madsand.LuaUtils;
import hitonoriol.madsand.world.World;

public abstract class Ability {
	public String script;

	public void apply() {
		LuaUtils.executeScript(script, World.player);
	}
}
