package hitonoriol.madsand.map;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;

import com.badlogic.gdx.utils.Timer;

import hitonoriol.madsand.MadSand;
import hitonoriol.madsand.TimeDependent;
import hitonoriol.madsand.util.TimeUtils;
import hitonoriol.madsand.util.Utils;

public class TimeScheduler {
	private Timer timer = new Timer();
	private Map<Long, List<TimeDependent>> updateMap = new HashMap<>();
	private Queue<Runnable> postUpdateActions = new ArrayDeque<>();

	public void register(TimeDependent entity) {
		long updateRate = entity.getUpdateRate();
		if (!updateMap.containsKey(updateRate)) {
			List<TimeDependent> newUpdateList = new ArrayList<>();
			updateMap.put(updateRate, newUpdateList);
			TimeUtils.scheduleRepeatingTask(timer, () -> update(newUpdateList), MadSand.world().ticksToTime(updateRate));
		}

		Utils.dbg("Scheduler {%X} registered entity %s", this.hashCode(), entity);
		updateMap.get(updateRate).add(entity);
	}

	public void remove(TimeDependent entity) {
		long updateRate = entity.getUpdateRate();
		List<TimeDependent> updateList = updateMap.get(updateRate);
		postUpdateActions.add(() -> updateList.remove(entity));
	}

	public void stop() {
		updateMap.clear();
		timer.clear();
		timer.stop();
	}

	public void forEach(Consumer<TimeDependent> action) {
		updateMap
				.forEach((updRate, entityList) -> entityList
						.forEach(entity -> action.accept(entity)));
	}

	private void update(List<TimeDependent> entities) {
		entities.forEach(entity -> entity.update());
		postUpdateActions.forEach(action -> action.run());
	}
}
