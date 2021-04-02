package hitonoriol.madsand.entities.inventory.item;

import java.util.HashMap;

import hitonoriol.madsand.properties.Globals;

public class Scroll extends ScriptedConsumable {

	public Scroll(Scroll protoItem) {
		super(protoItem);
	}

	public Scroll() {
		super();
	}

	@Override
	public Item copy() {
		return new Scroll(this).roll();
	}

	@Override
	protected HashMap<String, String> getScriptMap() {
		return Globals.instance().scrolls;
	}

	@Override
	protected int getBaseId() {
		return Globals.instance().baseScrollId;
	}

	@Override
	protected String getBaseName() {
		return "Scroll of ";
	}
	
	@Override
	protected String getUseMsg() {
		return "You read ";
	}

	@Override
	public Scroll load(String name) {
		super.load(name);
		return this;
	}

	public static Scroll create(String name) {
		return new Scroll().load(name);
	}
}
