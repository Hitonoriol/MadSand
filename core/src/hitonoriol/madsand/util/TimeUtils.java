package hitonoriol.madsand.util;

import com.badlogic.gdx.utils.Timer;

import hitonoriol.madsand.gui.Gui;

public class TimeUtils {
	public static Timer.Task createTask(Runnable task) {
		return new Timer.Task() {
			@Override
			public void run() {
				task.run();
			}
		};
	}

	public static Timer.Task scheduleTask(Timer timer, Runnable task, float delaySec) {
		return timer.scheduleTask(createTask(task), delaySec);
	}

	public static Timer.Task scheduleTask(Runnable task, float delaySec) {
		return scheduleTask(Timer.instance(), task, delaySec);
	}

	public static Timer.Task scheduleTask(Timer timer, Timer.Task task, float delaySec) {
		return timer.scheduleTask(task, delaySec);
	}

	public static Timer.Task scheduleTask(Timer.Task task, float delaySec) {
		return scheduleTask(Timer.instance(), task, delaySec);
	}

	public static Timer.Task scheduleTask(Runnable task) {
		return scheduleTask(task, Gui.DELAY);
	}

	public static Timer.Task scheduleRepeatingTask(Timer timer, Runnable task, float intervalSec) {
		return timer.scheduleTask(createTask(task), intervalSec, intervalSec);
	}

	public static Timer.Task scheduleRepeatingTask(Runnable task, float intervalSec) {
		return scheduleRepeatingTask(Timer.instance(), task, intervalSec);
	}

	public static float timeToExecution(Timer.Task task) {
		return (task.getExecuteTimeMillis() - System.nanoTime() / 1000000) / 1000f;
	}
}
