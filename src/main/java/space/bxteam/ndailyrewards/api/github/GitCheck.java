package space.bxteam.ndailyrewards.api.github;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.io.IOException;
import java.time.Instant;
import java.time.Duration;

/**
 * A utility class to check for the latest release of a GitHub repository using the GitHub API.
 *
 * @see GitCheckResult
 * @see GitRelease
 * @see GitRepository
 * @see GitTag
 * @since 3.0.0
 * @author NONPLAYT
 */
public class GitCheck {
    private static final String GITHUB_API_URL = "https://api.github.com/repos";
    private static final Duration CACHE_DURATION = Duration.ofHours(1);

    private GitCheckResult cachedResult;
    private Instant cacheTimestamp;

    /**
     * Checks the latest release of the given GitHub repository and compares it with the current tag.
     * If the result is cached and still valid, returns the cached result.
     *
     * @param repository the GitHub repository to check
     * @param currentTag the current version tag to compare against
     * @return a GitCheckResult indicating if the current version is up-to-date and the latest release details
     */
    public GitCheckResult checkRelease(GitRepository repository, GitTag currentTag) {
        if (isCacheValid()) {
            return cachedResult;
        }

        try {
            HttpClient client = HttpClient.newHttpClient();
            String url = GITHUB_API_URL + "/" + repository.getOwner() + "/" + repository.getProject() + "/releases";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonParser parser = new JsonParser();
                JsonArray releases = parser.parse(response.body()).getAsJsonArray();
                if (releases.size() > 0) {
                    JsonObject latestRelease = releases.get(0).getAsJsonObject();
                    String latestTag = latestRelease.get("tag_name").getAsString();
                    String pageUrl = latestRelease.get("html_url").getAsString();
                    String publishedAt = latestRelease.get("published_at").getAsString();

                    boolean isUpToDate = latestTag.equals(currentTag.getTag());
                    GitRelease release = isUpToDate ? null : new GitRelease(GitTag.of(latestTag), pageUrl, publishedAt);

                    cachedResult = new GitCheckResult(isUpToDate, release);
                    cacheTimestamp = Instant.now();

                    return cachedResult;
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        cachedResult = new GitCheckResult(true, null);
        cacheTimestamp = Instant.now();
        return cachedResult;
    }

    /**
     * Checks if the cached result is still valid based on the defined cache duration.
     *
     * @return true if the cached result is still valid, false otherwise
     */
    private boolean isCacheValid() {
        return cachedResult != null && cacheTimestamp != null && Duration.between(cacheTimestamp, Instant.now()).compareTo(CACHE_DURATION) <= 0;
    }
}
