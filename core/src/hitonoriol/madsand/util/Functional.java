package hitonoriol.madsand.util;

import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Functional {
	public static <T> boolean test(Optional<T> opt, Predicate<T> predicate) {
		return opt.filter(predicate).isPresent();
	}

	public static <T> T with(T obj, Consumer<T> action) {
		action.accept(obj);
		return obj;
	}

	public static void repeat(int times, Consumer<Integer> action) {
		IntStream.range(0, times).forEach(i -> action.accept(i));
	}

	public static void repeat(int times, Runnable action) {
		IntStream.range(0, times).forEach(i -> action.run());
	}

	public static <T> Stream<T> takeWhile(Stream<T> stream, Predicate<T> predicate) {
		return StreamSupport.stream(new PredicateSpliterator<>(stream.spliterator(), predicate), false);
	}

	public static void tryTo(SafeRunnable action, Consumer<Exception> catcher) {
		try {
			action.run();
		} catch (Exception e) {
			catcher.accept(e);
		}
	}

	public static interface SafeRunnable {
		void run() throws Exception;
	}

	private static class PredicateSpliterator<T> extends AbstractSpliterator<T> {
		private Spliterator<T> spliterator;
		private Predicate<T> predicate;
		private boolean isMatched = true;

		public PredicateSpliterator(Spliterator<T> spliterator, Predicate<T> predicate) {
			super(spliterator.estimateSize(), 0);
			this.spliterator = spliterator;
			this.predicate = predicate;
		}

		@Override
		public synchronized boolean tryAdvance(Consumer<? super T> consumer) {
			boolean hadNext = spliterator.tryAdvance(elem -> {
				if (predicate.test(elem) && isMatched)
					consumer.accept(elem);
				else
					isMatched = false;
			});
			return hadNext && isMatched;
		}
	}
}
