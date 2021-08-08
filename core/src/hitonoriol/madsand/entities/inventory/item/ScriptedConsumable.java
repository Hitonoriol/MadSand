package hitonoriol.madsand.entities.inventory.item;

import java.util.HashMap;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.gfx.ConditionalEffects;
import hitonoriol.madsand.gfx.Effects;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.ItemProp;
import hitonoriol.madsand.util.Utils;

public class ScriptedConsumable extends Item {
	private static final String NAME_TOK = "%s";
	private static ConditionalEffects<ScriptedConsumable> textureFx = ConditionalEffects.create(fx -> fx
			.addEffect(item -> Effects.colorize(Utils.randomColor(fx.random()))));

	private String nameTemplate; // String.format() template with exactly one %s token
	private String scriptMapName;

	public ScriptedConsumable(ScriptedConsumable protoItem, String consumableName) {
		super(protoItem);
		nameTemplate = protoItem.nameTemplate;
		scriptMapName = protoItem.scriptMapName;

		ScriptMap map = getScriptMap();
		if (!map.hasBaseItemId())
			map.setBaseItemId(id);

		if (consumableName != null)
			load(consumableName);
		else if (protoItem.isProto())
			roll();
		else
			load(protoItem.getScriptName());
	}

	public ScriptedConsumable(ScriptedConsumable protoItem) {
		this(protoItem, null);
	}

	public ScriptedConsumable() {}

	@Override
	public ScriptedConsumable copy() {
		return new ScriptedConsumable(this);
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

	@JsonIgnore
	protected ScriptMap getScriptMap() {
		return getScriptMap(scriptMapName);
	}

	protected static ScriptMap getScriptMap(String map) {
		return Globals.values().scriptMaps.get(map);
	}

	protected String getBaseName() {
		return nameTemplate.replace(NAME_TOK, "");
	}

	protected String getScript() {
		return getScriptMap().get(getScriptName());
	}

	protected String getScriptName() {
		return name.replace(getBaseName(), "");
	}

	protected String getUseMsg() {
		return "You use ";
	}

	public ScriptedConsumable roll() {
		return load(Utils.randElement(getScriptMap().keySet()));
	}

	protected ScriptedConsumable load(String name) {
		this.name = String.format(nameTemplate, name);
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

	public static ScriptedConsumable create(String mapName, String consumableName) {
		return new ScriptedConsumable(
				(ScriptedConsumable) ItemProp.getItem(getScriptMap(mapName).getBaseItemId()),
				consumableName);
	}

	public static class ScriptMap extends HashMap<String, String> {
		@JsonProperty
		private int baseItemId;

		public int getBaseItemId() {
			return baseItemId;
		}

		public void setBaseItemId(int id) {
			baseItemId = id;
		}

		public boolean hasBaseItemId() {
			return baseItemId != 0;
		}
	};
}
