package hitonoriol.madsand;

import hitonoriol.madsand.desktop.Launcher;

public class TestLauncher {
	static final String dbgArgs[] = { "debug" };

	public static void main(String[] args) throws Exception {
		Launcher.startHidden();
		Launcher.main(dbgArgs, new MadSandTestWrapper(args.length > 0 ? args[0] : null));
	}
}
