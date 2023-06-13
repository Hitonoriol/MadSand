package hitonoriol.madsand.entities.ability;

public class PassiveAbility extends Ability {

	public Type type;

	public enum Type {
		ApplyOnce, // Apply once when this ability is acquired
		ApplyAlways // Apply on every tick
	}
}
