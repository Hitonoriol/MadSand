package hitonoriol.madsand.entities.movement;

import static hitonoriol.madsand.screens.WorldRenderer.TARGET_FRAME_DELTA;

import java.util.function.Consumer;

import hitonoriol.madsand.containers.PairFloat;
import hitonoriol.madsand.entities.Entity;
import hitonoriol.madsand.enums.Direction;
import hitonoriol.madsand.resources.Resources;

public class Movement {
	private boolean ignoreObstacles = false;
	private boolean allowDiagonal = false;
	private boolean applyChanges = true;
	private Direction direction;
	private float speed;
	private float distance = Resources.TILESIZE;
	private Consumer<Movement> afterMovementAction = m -> {};

	public Movement(Direction direction, float speed) {
		setDirection(direction);
		this.speed = speed;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public boolean update(PairFloat position) {
		position.add(getHorizontalOffset(), getVerticalOffset());
		distance -= speed;
		if (distance <= 0) {
			finishMoving();
			return false;
		}
		return true;
	}

	public void apply(PairFloat position) {
		while (update(position));
	}
	
	public float getDuration() {
		return (distance / speed) * TARGET_FRAME_DELTA;
	}
	
	public boolean isFinished() {
		return distance <= 0;
	}
	
	public boolean isInvalid(Entity movingEntity) {
		return !ignoringObstacles() && movingEntity.colliding(direction());
	}

	public Movement onMovementFinish(Consumer<Movement> afterMovementAction) {
		this.afterMovementAction = afterMovementAction;
		return this;
	}

	private void finishMoving() {
		afterMovementAction.accept(this);
	}

	public Movement setSpeed(float speed) {
		this.speed = speed;
		return this;
	}

	public Movement setDistance(float distance) {
		this.distance = distance;
		return this;
	}

	public float getDistance() {
		return distance;
	}

	private float getHorizontalOffset() {
		if (!direction.isHorizontal() && !direction.isDiagonal())
			return 0;

		return direction.isLeft() ? -speed : speed;
	}

	private float getVerticalOffset() {
		if (!direction.isVertical() && !direction.isDiagonal())
			return 0;

		return direction.isDown() ? -speed : speed;
	}

	public boolean ignoringObstacles() {
		return ignoreObstacles;
	}

	public boolean applyChanges() {
		return applyChanges;
	}

	public boolean isDiagonalAllowed() {
		return allowDiagonal;
	}

	public Direction direction() {
		return direction;
	}

	public Movement setIgnoreObstacles(boolean value) {
		ignoreObstacles = value;
		return this;
	}

	public Movement allowDiagonal(boolean value) {
		allowDiagonal = value;
		return this;
	}

	public Movement applyChanges(boolean value) {
		applyChanges = value;
		return this;
	}

	public static Movement walk(Entity entity, Direction direction) {
		return new Movement(direction, entity.calcMovementSpeed());
	}

	public static Movement walk(Entity entity) {
		return walk(entity, entity.stats().look);
	}

	public static MeleeAttackMovement meleeAttack(Entity entity, Runnable attackAction) {
		return new MeleeAttackMovement(entity.stats().look, entity.calcMeleeMovementSpeed())
				.onAttack(attackAction);
	}
}
