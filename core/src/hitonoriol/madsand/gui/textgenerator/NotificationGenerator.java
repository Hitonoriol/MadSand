package hitonoriol.madsand.gui.textgenerator;

import java.util.ArrayDeque;
import java.util.Queue;

import com.badlogic.gdx.utils.Timer;

import hitonoriol.madsand.input.Mouse;
import hitonoriol.madsand.util.TimeUtils;
import hitonoriol.madsand.util.Utils;

public class NotificationGenerator extends StaticTextGenerator {
	private static float NOTIFICATION_DELAY = 1.85f;
	private Timer timer = new Timer();
	private Queue<Runnable> pendingNotifications = new ArrayDeque<>();

	public NotificationGenerator() {
		setEnabled(false);
	}

	public void notify(String text) {
		Runnable notifyTask = () -> performNotification(text, pendingNotifications.size() + (isEnabled() ? 1 : 0));
		if (!isEnabled())
			notifyTask.run();
		else
			pendingNotifications.add(notifyTask);
	}

	private float notificationQuantityFactor(int notifsInQueue) {
		++notifsInQueue;
		if (notifsInQueue <= 1)
			return 1;
			
		return 1f / ((float) Math.sqrt(notifsInQueue));
	}

	private void performNotification(String text, int notifsInQueue) {
		setText(text);
		setEnabled(true);
		Mouse.refreshTooltip();
		Utils.dbg("Notif duration factor: %f", notificationQuantityFactor(notifsInQueue));
		TimeUtils.scheduleTask(timer, () -> {
			if (hasPendingNotifications())
				showNextNotification();
			else
				setEnabled(false);
			Mouse.refreshTooltip();
		}, NOTIFICATION_DELAY * notificationQuantityFactor(notifsInQueue));
	}

	private boolean hasPendingNotifications() {
		return !pendingNotifications.isEmpty();
	}

	private void showNextNotification() {
		pendingNotifications.poll().run();
	}
}
