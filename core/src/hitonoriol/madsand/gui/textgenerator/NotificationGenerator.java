package hitonoriol.madsand.gui.textgenerator;

import com.badlogic.gdx.utils.Timer;

import hitonoriol.madsand.input.Mouse;
import hitonoriol.madsand.util.Utils;

public class NotificationGenerator extends StaticTextGenerator {
	private static float NOTIFICATION_DELAY = 1.55f, DRIFT = 0.035f;
	private Timer timer = new Timer();
	private Timer.Task ntfInProgress = emptyTask;
	private int pending = 0;

	public NotificationGenerator() {
		setEnabled(false);
	}

	public void notify(String text) {
		Utils.dbg("Previous ntf [%s] is still scheduled: %b", ntfInProgress, ntfInProgress.isScheduled());
		if (!ntfInProgress.isScheduled())
			performNotification(text);
		else {
			Utils.dbg("Pending: %d", pending);
			float time = Utils.timeToExecution(ntfInProgress) + (float) pending * (NOTIFICATION_DELAY + DRIFT);
			++pending;
			Utils.dbg("Scheduling to show [%s] in %f seconds", text, time);
			Utils.scheduleTask(timer, () -> {
				performNotification(text);
				--pending;
			}, time);
		}
	}

	private void performNotification(String text) {
		Utils.dbg("Start [%s]", text);
		setText(text);
		setEnabled(true);
		Mouse.refreshTooltip();
		ntfInProgress = Utils.scheduleTask(timer, () -> {
			Utils.dbg("End [%s]", text);
			setEnabled(false);
			Mouse.refreshTooltip();
		}, NOTIFICATION_DELAY);
	}

	private static final Timer.Task emptyTask = Utils.createTask(() -> {});
	static {
		emptyTask.cancel();
	}
}
