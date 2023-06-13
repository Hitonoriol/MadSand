package hitonoriol.madsand.map.object;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.gamecontent.Objects;

public abstract class BuiltObject extends MapObject {
	public BuiltObject(BuiltObject protoObject) {
		super(protoObject);
	}

	public BuiltObject() {
	}

	@Override
	public void destroy() {
		var coords = getPosition();
		MadSand.world().getCurLoc()
			.putLoot(coords.x, coords.y, Item.parseItemString(Objects.all().buildRecipes().get(id)));
		super.destroy();
	}
}
