package hitonoriol.madsand.map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hitonoriol.madsand.DynamicallyCastable;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.AnimationContainer;
import hitonoriol.madsand.containers.Pair;

public abstract class MapEntity implements DynamicallyCastable<MapEntity>{
	public abstract void damage(int amt);

	public abstract String getName();

	@JsonIgnore
	public abstract Pair getPosition();

	public void playAnimation(AnimationContainer animation) {
		Pair worldPos = getPosition().toWorld();
		MadSand.gameWorld.queueAnimation(animation, worldPos.x, worldPos.y);
	}

	public abstract void playDamageAnimation();

	@JsonIgnore
	public boolean isEmpty() {
		return this == Map.nullNpc || this == Map.nullObject;
	}
}
