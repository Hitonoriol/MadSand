package hitonoriol.madsand.map.object;

import java.util.ArrayList;
import java.util.HashMap;

import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.entities.Skill;
import hitonoriol.madsand.entities.inventory.item.Tool;
import me.xdrop.jrand.JRand;
import me.xdrop.jrand.generators.basics.FloatGenerator;

public class ResourceObject extends MapObject {
	public static float MIN_HP = 0.55f, MAX_HP = 1.75f;
	private static FloatGenerator hpRangeGen = JRand.flt().range(MIN_HP, MAX_HP);

	public Skill skill = Skill.None;
	public HashMap<Tool.Type, ArrayList<Integer>> altItems;

	public ResourceObject(ResourceObject protoObject) {
		super(protoObject);
		skill = protoObject.skill;
		altItems = protoObject.altItems;
		rollHp();
	}

	@Override
	public ResourceObject copy() {
		return new ResourceObject(this);
	}

	public ResourceObject() {
		super();
	}

	@Override
	public void interact(Player player) {
		super.interact(player);
		super.interactIfPossible(() -> player.interact(this));
	}

	private void rollHp() {
		hp = maxHp = (int) Math.max(maxHp * hpRangeGen.gen(), 1f);
	}

	public int rollDrop(Tool.Type heldItemType) {
		return rollResource(id, heldItemType, altItems);
	}

}
