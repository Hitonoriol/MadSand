package hitonoriol.madsand.entities.inventory.item;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.Utils;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.properties.Globals;
import hitonoriol.madsand.properties.ItemProp;

public class Scroll extends Item {

	public Scroll(Scroll protoItem) {
		super(protoItem);

		if (protoItem.useAction == null)
			roll();
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
		MadSand.notice("You read " + name);
		super.use(player);
		player.delItem(this, 1);
	}

	public Scroll roll() {
		return load(Utils.randElement(Globals.instance().scrolls.keySet()));
	}

	public Scroll load(String name) {
		Globals globals = Globals.instance();

		if (id == Item.NULL_ITEM)
			loadProperties(ItemProp.getItem(id = globals.baseScrollId));

		this.name = "Scroll of " + name;
		useAction = globals.scrolls.get(name);
		quantity = 1;
		return this;
	}

	public static Scroll create(String name) {
		return new Scroll().load(name);
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj) && name.equals(((Item) obj).name);
	}
}
