package hitonoriol.madsand.launcher.util;

import java.util.Date;

import org.kohsuke.github.GHRateLimit.Record;
import org.kohsuke.github.RateLimitChecker;

public class RateLimiter extends RateLimitChecker {
	private int minRemaining;

	public RateLimiter(int minRemaining) {
		this.minRemaining = minRemaining;
	}

	@Override
	protected boolean checkRateLimit(Record rateLimitRecord, long count) throws InterruptedException {
		Date resetDate = rateLimitRecord.getResetDate();
		int remaining = rateLimitRecord.getRemaining();
		System.out.printf("API requests : %d/%d (until %s)\n",
				remaining, rateLimitRecord.getLimit(), resetDate);
		if (rateLimitRecord.getRemaining() <= minRemaining)
			throw new RuntimeException("GitHub API rate limit exceeded! Expires " + resetDate);
		return false;
	}

}
