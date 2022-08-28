package hitonoriol.madsand.entities.movement;

import hitonoriol.madsand.containers.PairFloat;
import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.resources.Resources;

public class MeleeAttackMovement extends Movement {
	public static final float SPEED_FACTOR = 2f;
	private static final float DISTANCE = Resources.TILESIZE;

	private boolean attacked = false;
	private Runnable attackAction = () -> {};
	private Runnable afterAttackAction = () -> {};
	
	public MeleeAttackMovement(Direction direction, float speed) {
		super(direction, speed);
		setDistance(DISTANCE);
		setIgnoreObstacles(true);
		allowDiagonal(true);
		applyChanges(false);
	}
	
	@Override
	public float getDuration() {
		return super.getDuration() * 2;
	}
	
	public MeleeAttackMovement onAttack(Runnable attackAction) {
		this.attackAction = attackAction;
		return this;
	}
	
	public MeleeAttackMovement onAttackFinish(Runnable afterAttack) {
		this.afterAttackAction = afterAttack;
		return this;
	}
	
	@Override
	public boolean update(PairFloat position) {
		boolean updated = super.update(position);
		
		if (updated)
			return true;
		
		if (attacked) {
			afterAttackAction.run();
			return false;
		}
		
		attacked = true;
		attackAction.run();
		setDistance(DISTANCE);
		setDirection(direction().opposite());
		return true;
	}
}
