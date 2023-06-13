package hitonoriol.madsand.entities.inventory.item;

import com.fasterxml.jackson.annotation.JsonProperty;

import hitonoriol.madsand.containers.rolltable.LootTable;
import hitonoriol.madsand.entities.Player;

public class GrabBag extends Item {
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	protected LootTable contents;

	public GrabBag(GrabBag protoItem) {
		super(protoItem);
		contents = protoItem.contents;
	}

	public GrabBag() {
	}

	public LootTable contents() {
		if (contents == null)
			contents = Item.getProto(id)
				.as(getClass())
				.map(bag -> bag.contents)
				.orElse(new LootTable());

		return contents;
	}

	@Override
	public GrabBag copy() {
		return new GrabBag(this);
	}

	@Override
	public void use(Player player) {
		player.useItem(this);
	}
}
