package hitonoriol.madsand.entities.inventory.item;

import java.util.HashMap;

import hitonoriol.madsand.properties.Globals;

public class Pill extends ScriptedConsumable {
	public Pill(Pill protoItem) {
		super(protoItem);
	}

	public Pill() {
		super();
	}

	@Override
	public Item copy() {
		return new Pill(this).roll();
	}

	@Override
	protected HashMap<String, String> getScriptMap() {
		return Globals.values().pills;
	}

	@Override
	protected int getBaseId() {
		return Globals.values().basePillId;
	}

	@Override
	protected String getBaseName() {
		return "Pill of ";
	}

	@Override
	public Pill load(String name) {
		super.load(name);
		return this;
	}
}