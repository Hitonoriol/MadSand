package hitonoriol.madsand.entities.ability;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import hitonoriol.madsand.DynamicallyCastable;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.lua.Lua;
import hitonoriol.madsand.lua.LuaLambda;
import hitonoriol.madsand.lua.LuaLambda.LuaConsumer;
import hitonoriol.madsand.lua.LuaUtils;
import hitonoriol.madsand.properties.Globals;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY)
@JsonSubTypes({ @Type(ActiveAbility.class), @Type(PassiveAbility.class) })
public abstract class Ability implements DynamicallyCastable<Ability> {
	public int id;
	public int lvl = 1;
	public String name;
	private LuaConsumer action;

	public void apply() {
		Utils.out(name + ": Running ability script...");
		action.accept(this);
	}

	public void setName(String name) {
		this.name = name;
		action = LuaLambda.consumer(Lua.loadScript("ability/" + LuaUtils.getScriptName(name)));
	}

	public int levelUp() {
		return ++lvl;
	}

	public static Ability get(int id) {
		return Globals.instance().abilities.get(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (!(obj instanceof Ability))
			return false;

		return id == ((Ability) obj).id;
	}
}
