package hitonoriol.madsand.util;

public class If {
	public static boolean then(boolean condResult, Runnable action) {
		if (condResult)
			action.run();

		return condResult;
	}

	public static void thenOrElse(boolean condResult, Runnable action, Runnable orElse) {
		if (!If.then(condResult, action))
			orElse.run();
	}
}
