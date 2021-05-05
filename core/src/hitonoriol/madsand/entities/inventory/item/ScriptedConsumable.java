package hitonoriol.madsand.entities.inventory.item;

import java.util.HashMap;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.gfx.ConditionalEffects;
import hitonoriol.madsand.gfx.Effects;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.util.Utils;

public abstract class ScriptedConsumable extends Item {
	private static ConditionalEffects<ScriptedConsumable> textureFx = ConditionalEffects.create(fx -> fx
			.addEffect(item -> Effects.colorize(Utils.randomColor(fx.random()))));

	public ScriptedConsumable(ScriptedConsumable protoItem) {
		super(protoItem);

		if (protoItem.useAction == null)
			roll();
	}

	public ScriptedConsumable() {
		super();
	}

	@Override
	public void use(Player player) {
		if (useAction == null)
			useAction = getScript();

		Utils.out("Using scripted consumable [%s] script {%s}", name, useAction);

		MadSand.notice(getUseMsg() + name);
		super.use(player);
		player.delItem(this, 1);
	}

	@Override
	protected void refreshTextureEffects() {
		super.refreshTextureEffects();
		textureFx.apply(this);
	}

	protected abstract HashMap<String, String> getScriptMap();

	protected abstract int getBaseId();

	protected abstract String getBaseName();

	protected String getScript() {
		return getScriptMap().get(name.replace(getBaseName(), ""));
	}

	protected String getUseMsg() {
		return "You use ";
	}

	public ScriptedConsumable roll() {
		if (name != null && !name.equals(getBaseName()))
			return this;

		return load(Utils.randElement(getScriptMap().keySet()));
	}

	public ScriptedConsumable load(String name) {
		if (id == Item.NULL_ITEM)
			loadProperties(ItemProp.getItem(id = getBaseId()));

		this.name = getBaseName() + name;
		useAction = getScript();
		quantity = 1;
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj) && name.equals(((Item) obj).name);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(48481, 29411)
				.append(super.hashCode())
				.append(name)
				.toHashCode();
	}
}
