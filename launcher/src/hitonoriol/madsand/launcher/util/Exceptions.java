package hitonoriol.madsand.launcher.util;

import java.util.function.Supplier;

public class Exceptions {
	public static Void printStackTrace(Throwable e) {
		e.printStackTrace();
		return null;
	}

	public static <T> Supplier<T> asUnchecked(ThrowingSupplier<T> task) {
		return () -> {
			try {
				return task.get();
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		};
	}

	public static Runnable asUnchecked(ThrowingRunnable task) {
		return () -> {
			try {
				task.run();
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		};
	}

	public static interface ThrowingRunnable {
		void run() throws Throwable;
	}

	public static interface ThrowingSupplier<T> {
		T get() throws Throwable;
	}
}
