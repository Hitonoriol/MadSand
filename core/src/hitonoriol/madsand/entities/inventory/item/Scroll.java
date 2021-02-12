package hitonoriol.madsand.entities.inventory.item;

import hitonoriol.madsand.Utils;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.properties.Globals;

public class Scroll extends Item {

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
	public void use(Player player) {
		super.use(player);
		player.delItem(this, 1);
	}

	public Scroll roll() {
		return load(Utils.randElement(Globals.instance().scrolls.keySet()));
	}

	public Scroll load(String name) {
		this.name += name;
		useAction = Globals.instance().scrolls.get(name);
		return this;
	}

	public static Scroll create(String name) {
		Scroll scroll = new Scroll();
		scroll.quantity = 1;
		return scroll.load(name);
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj) && name.equals(((Item) obj).name);
	}
}
