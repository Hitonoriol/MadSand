package hitonoriol.madsand.world.time;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.iterators.PeekingIterator;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.entities.Entity;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.gui.Gui;
import hitonoriol.madsand.util.TimeUtils;
import hitonoriol.madsand.util.Utils;
import hitonoriol.madsand.world.World;

public class EntityTimeProcessor {
	private World world;
	private List<Entity> entities = new ArrayList<>();

	public EntityTimeProcessor(World world) {
		this.world = world;
	}

	public void processEntityActions(float worldTime) {
		final float maxSimDst = world.getMaxSimDistance();
		Player player = MadSand.player();

		PeekingIterator<Entity> entities = new PeekingIterator<>(prepareEntities().iterator());
		while (entities.hasNext()) {
			Entity entity = entities.next();

			/* Don't simulate entities outside of the simulation radius during time skip */
			if (world.timeSkipInProgress() && entity.distanceTo(player) > maxSimDst)
				continue;
			
			entity.prepareToAnimateAction();
			/* Schedule actions for potentially visible entities / entities out of view act immediately */
			if (player.isInsideFov(entity) && entity.hasActDelay())
				TimeUtils.scheduleTask(() -> entity.act(worldTime), entity.getActDelay());
			else
				entity.act(worldTime);
		}

		float timeToEnd = getTimeToEndOfTurn();
		TimeUtils.scheduleTask(() -> afterAllActions(), timeToEnd);
		Utils.dbg("All entities (%d) should finish their actions in ~%f seconds", this.entities.size(), timeToEnd);
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

	private float getTimeToEndOfTurn() {
		return entities.stream()
				.map(entity -> entity.getActDelay() + entity.getActDuration())
				.reduce(Float::max).orElse(0f);
	}
}
