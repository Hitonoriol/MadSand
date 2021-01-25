package hitonoriol.madsand.map;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.containers.AnimationContainer;
import hitonoriol.madsand.containers.Pair;

public abstract class MapEntity {
	public abstract void damage(int amt);

	public abstract Pair getPosition();

	public void playAnimation(AnimationContainer animation) {
		Pair worldPos = getPosition().toWorld();
		MadSand.gameWorld.queueAnimation(animation, worldPos.x, worldPos.y);
	}
	
	public abstract void playDamageAnimation();
}
