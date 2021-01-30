package hitonoriol.madsand.entities.inventory.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import hitonoriol.madsand.entities.LootTable;

public class GrabBag extends Item {
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	public LootTable contents;
	
	public GrabBag(GrabBag protoItem) {
		super(protoItem);
		contents = protoItem.contents;
	}
	
	@Override
	public GrabBag copy() {
		return new GrabBag(this);
	}
	
	@JsonSetter("contents")
	public void setContents(String contents) {
		if (contents == null)
			return;

		this.contents = LootTable.parse(contents);
	}
}
