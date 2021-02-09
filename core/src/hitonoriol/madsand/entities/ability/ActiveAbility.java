package hitonoriol.madsand.entities.ability;

public class ActiveAbility extends Ability {

	public int staminaCost;
	public Type type;
	
	public static enum Type {
		Oneshot, Toggleable
	}
}
