package hitonoriol.madsand.entities.inventory.item;

import com.fasterxml.jackson.annotation.JsonProperty;

import hitonoriol.madsand.entities.LootTable;
import hitonoriol.madsand.entities.Player;

public class GrabBag extends Item {
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	public LootTable contents;
	
	public GrabBag(GrabBag protoItem) {
		super(protoItem);
		contents = protoItem.contents;
	}
	
	public GrabBag() {
		super();
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
