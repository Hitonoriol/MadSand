package hitonoriol.madsand;

import com.badlogic.gdx.Gdx;
import hitonoriol.madsand.entities.Player;
import hitonoriol.madsand.util.Utils;
import org.junit.internal.TextListener;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import java.io.PrintWriter;

import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

public class MadSandTestWrapper extends MadSand {
    @Override
    public void create() {
        super.create();
        Utils.disableTimestampOutput();

        Player player = player();
        player.stats.roll(10);
        player.reinit();
        worldEntered();

        Utils.out();
        runAll();
        Gdx.app.exit();
    }

    private final SummaryGeneratingListener listener = new SummaryGeneratingListener();

    private void runAll() {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectPackage(getClass().getPackageName()))
                .filters(includeClassNamePatterns(".*Test"))
                .build();

        Launcher launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);

        TestExecutionSummary summary = listener.getSummary();
        summary.printTo(new PrintWriter(System.out));
    }
}
