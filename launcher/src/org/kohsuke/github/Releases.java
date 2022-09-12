package org.kohsuke.github;

/* A hack to avoid sending an extra API request via GitHub#getRepository(String)
 * (because it's impossible to spend less than 2 requests to retrieve the repo's
 * asset list legitimately and the rate limit for anonymous requests is 60 per hour) */
public class Releases {
	public static PagedIterable<GHRelease> get(GitHub github, String repoName) {
		return github.createRequest()
				.withUrlPath("/repos/" + repoName + "/releases")
				.toIterable(GHRelease[].class, item -> {});
	}
}
