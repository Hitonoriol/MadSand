package hitonoriol.madsand.entities.ability;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import hitonoriol.madsand.DynamicallyCastable;
import hitonoriol.madsand.lua.Lua;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.world.World;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY)
@JsonSubTypes({ @Type(ActiveAbility.class), @Type(PassiveAbility.class) })
public abstract class Ability implements DynamicallyCastable<Ability> {
	public int id;
	public int lvl = 1;
	public String name;
	public String script;

	public void apply() {
		Lua.executeScript("ability/" + script, this, World.player);
	}

	public static Ability get(int id) {
		return Globals.instance().abilities.get(id);
	}
}
