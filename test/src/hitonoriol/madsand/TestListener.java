package hitonoriol.madsand;

import java.util.stream.Stream;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

import hitonoriol.madsand.util.Utils;

public class TestListener implements TestExecutionListener {

	static String[] testIdTypes = Stream.of(TestDescriptor.Type.values())
			.map(type -> type.name().toLowerCase())
			.toArray(String[]::new);

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		Utils.out("* Starting %s [%s]...", testIdTypes[testIdentifier.getType().ordinal()],
				testIdentifier.getDisplayName());
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		if (!testIdentifier.isTest())
			return;

		Utils.out("* [%s] execution finished! [%s]\n",
				testIdentifier.getDisplayName(),
				testExecutionResult.getStatus());
	}

}
