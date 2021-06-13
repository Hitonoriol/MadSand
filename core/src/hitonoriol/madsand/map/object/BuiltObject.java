package hitonoriol.madsand.map.object;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.Pair;
import hitonoriol.madsand.entities.inventory.item.Item;
import hitonoriol.madsand.properties.ObjectProp;

public abstract class BuiltObject extends MapObject {
	public BuiltObject(BuiltObject protoObject) {
		super(protoObject);
	}

	public BuiltObject() {
		super();
	}

	@Override
	public void destroy() {
		Pair coords = getPosition();
		MadSand.world().getCurLoc()
				.putLoot(coords.x, coords.y, Item.parseItemString(ObjectProp.buildRecipes.get(id)));
		super.destroy();
	}
}
