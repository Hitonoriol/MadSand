package hitonoriol.madsand.gui.textgenerator;

import java.util.ArrayDeque;
import java.util.Queue;

import com.badlogic.gdx.utils.Timer;

import hitonoriol.madsand.input.Mouse;
import hitonoriol.madsand.util.TimeUtils;

public class NotificationGenerator extends StaticTextGenerator {
	private static float NOTIFICATION_DELAY = 1.85f;
	private Timer timer = new Timer();
	private Queue<Runnable> pendingNotifications = new ArrayDeque<>();

	public NotificationGenerator() {
		setEnabled(false);
	}

	public void notify(String text) {
		Runnable notifyTask = () -> performNotification(text);
		if (!isEnabled())
			notifyTask.run();
		else
			pendingNotifications.add(notifyTask);
	}

	private void performNotification(String text) {
		setText(text);
		setEnabled(true);
		Mouse.refreshTooltip();
		TimeUtils.scheduleTask(timer, () -> {
			if (hasPendingNotifications())
				showNextNotification();
			else
				setEnabled(false);
			Mouse.refreshTooltip();
		}, NOTIFICATION_DELAY);
	}

	private boolean hasPendingNotifications() {
		return !pendingNotifications.isEmpty();
	}

	private void showNextNotification() {
		pendingNotifications.poll().run();
	}
}
