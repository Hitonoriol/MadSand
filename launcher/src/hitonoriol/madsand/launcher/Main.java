package hitonoriol.madsand.launcher;

import hitonoriol.madsand.launcher.gui.LauncherFrame;

public class Main {
	public static final String VERSION = Main.class.getPackage().getImplementationVersion();
	private static final LauncherFrame launcherFrame = new LauncherFrame();

	public static void main(String[] args) {
		launcherFrame.setVisible(true);
		launcherFrame.checkForUpdates();
	}

	public static LauncherFrame getWindow() {
		return launcherFrame;
	}
}
