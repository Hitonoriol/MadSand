package hitonoriol.madsand;

import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

import java.io.PrintWriter;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import com.badlogic.gdx.Gdx;

import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.util.Utils;

public class MadSandTestWrapper extends MadSand {
	@Override
	public void create() {
		super.create();
		Utils.disableTimestampOutput();

		Player player = player();
		player.stats.roll(10);
		player.reinit();
		enterWorld();

		Utils.out();
		runAll();

		Gdx.app.exit();
	}

	private final SummaryGeneratingListener listener = new SummaryGeneratingListener();

	private void runAll() {
		LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
				.selectors(selectPackage(getClass().getPackageName() + ".tests"))
				.filters(includeClassNamePatterns(".*Test"))
				.build();

		Launcher launcher = LauncherFactory.create();
		launcher.registerTestExecutionListeners(listener, new TestListener());
		launcher.execute(request);

		TestExecutionSummary summary = listener.getSummary();
		PrintWriter out = new PrintWriter(System.out);
		summary.printTo(out);
		summary.printFailuresTo(out);

		/*if (summary.getTotalFailureCount() > 0)
			Gdx.app.exit();*/
	}
}
