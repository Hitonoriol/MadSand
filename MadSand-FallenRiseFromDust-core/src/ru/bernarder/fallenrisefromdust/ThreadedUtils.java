package ru.bernarder.fallenrisefromdust;

import ru.bernarder.fallenrisefromdust.enums.GameState;

public class ThreadedUtils {
	public static boolean mapstop = false;

	public Thread worldGen = new Thread(new Runnable() {
		@SuppressWarnings("deprecation")
		public void run() {
			ThreadedUtils.this.worldGen.stop();
		}
	});

	public Thread initialWorldGen = new Thread(new Runnable() {
		@SuppressWarnings("deprecation")
		public void run() {
			MadSand.world.Generate();
			MadSand.state = GameState.GAME;
			ThreadedUtils.this.worldGen.stop();
		}
	});

	public Thread gotoSector = new Thread(new Runnable() {
		@SuppressWarnings("deprecation")
		public void run() {
			ThreadedUtils.this.gotoSector.stop();
		}
	});

	public Thread wontlisten = new Thread(new Runnable() {
		@SuppressWarnings("deprecation")
		public void run() {
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			MadSand.dontlisten = false;
			ThreadedUtils.this.wontlisten.stop();
		}
	});
}
