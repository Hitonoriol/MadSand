package hitonoriol.madsand;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

import java.io.PrintWriter;

import org.junit.platform.engine.DiscoverySelector;
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
	private final String TEST_PACKAGE = getClass().getPackageName() + ".tests";
	private final SummaryGeneratingListener listener = new SummaryGeneratingListener();
	private Launcher launcher = LauncherFactory.create();
	private String testName;

	public MadSandTestWrapper(String testName) {
		this.testName = testName;
	}

	public MadSandTestWrapper() {
		this(null);
	}

	@Override
	public void create() {
		super.create();
		Utils.disableTimestampOutput();
		Player player = player();
		player.stats.randomize(10);
		enterWorld();
		player.finishCreation();
		Utils.out();
		launcher.registerTestExecutionListeners(listener, new TestListener());

		if (testName == null) {
			Utils.out("Running all tests");
			runAll();
		} else {
			Utils.out("Running test %s", testName);
			run(testName);
		}
		Gdx.app.exit();
	}

	public void runAll() {
		run(selectPackage(TEST_PACKAGE));
	}

	public void run(String testName) {
		run(selectClass(TEST_PACKAGE + "." + testName + "Test"));
	}

	private void run(DiscoverySelector selector) {
		launcher.execute(createRequest(selector));
		TestExecutionSummary summary = listener.getSummary();
		PrintWriter out = new PrintWriter(System.out);
		summary.printTo(out);
		summary.printFailuresTo(out);
	}

	private LauncherDiscoveryRequest createRequest(DiscoverySelector selector) {
		return LauncherDiscoveryRequestBuilder.request()
				.selectors(selector)
				.build();
	}
}
