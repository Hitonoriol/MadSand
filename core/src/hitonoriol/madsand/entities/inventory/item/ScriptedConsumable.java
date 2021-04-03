package hitonoriol.madsand.entities.inventory.item;

import java.util.HashMap;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.properties.ItemProp;

public abstract class ScriptedConsumable extends Item {
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
}
