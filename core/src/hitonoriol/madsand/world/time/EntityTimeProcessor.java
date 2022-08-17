package hitonoriol.madsand.world.time;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.iterators.PeekingIterator;
import org.apache.commons.lang3.mutable.MutableFloat;

import hitonoriol.madsand.Gui;
import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.entities.Entity;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.util.TimeUtils;
import hitonoriol.madsand.world.World;
import me.xdrop.jrand.JRand;
import me.xdrop.jrand.generators.basics.FloatGenerator;

public class EntityTimeProcessor {
	private World world;
	private List<Entity> entities = new ArrayList<>();

	private static final float ACT_DURATION_DRIFT = 0.01575f;
	private static FloatGenerator randomActionDelay = JRand.flt().range(Float.MIN_NORMAL, ACT_DURATION_DRIFT);

	public EntityTimeProcessor(World world) {
		this.world = world;
	}

	public void processEntityActions(float worldTime) {
		final float maxSimDst = world.getMaxSimDistance();
		Player player = MadSand.player();

		MutableFloat maxActTime = new MutableFloat(0);
		float totalDrift = 0;

		PeekingIterator<Entity> entities = new PeekingIterator<>(prepareEntities().iterator());
		while (entities.hasNext()) {
			Entity entity = entities.next();
			Entity nextEntity = entities.peek();

			/* Don't simulate entities outside of the simulation radius during time skip */
			if (world.timeSkipInProgress() && entity.distanceTo(player) > maxSimDst)
				return;

			if (player.canSee(entity)) {
				float actionDrift = randomActionDelay.gen() * (nextEntity != null ? nextEntity.getSpeed() : 1);
				TimeUtils.scheduleTask(() -> {
					entity.act(worldTime);
					entity.finishActing();
					maxActTime.setValue(Math.max(entity.getActDuration(), maxActTime.getValue()));
				}, actionDrift);
				totalDrift += actionDrift;
			} else
				entity.act(worldTime);
		}

		TimeUtils.scheduleTask(() -> afterAllActions(), maxActTime.getValue() + totalDrift);
	}

	private List<Entity> prepareEntities() {
		entities.clear();
		entities.addAll(world.getCurLoc().getNpcs().values());
		entities.add(world.getPlayer());
		entities.forEach(entity -> entity.prepareToAct());
		Collections.sort(entities, Entity.speedComparator);
		return entities;
	}
	
	private void afterAllActions() {
		Gui.overlay.refreshActionButton();
		Gui.refreshOverlay();

		if (world.timeSkipInProgress())
			world.endTimeSkip();
	}
}
