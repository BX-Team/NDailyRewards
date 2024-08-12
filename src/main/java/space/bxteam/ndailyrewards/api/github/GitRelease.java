package space.bxteam.ndailyrewards.api.github;

/**
 * Represents a GitHub release, containing information about the release tag,
 * URL of the release page, and the release date.
 */
public class GitRelease {
    private final GitTag tag;
    private final String pageUrl;
    private final String publishedAt;

    /**
     * Constructs a GitRelease instance.
     *
     * @param tag         the tag associated with this release
     * @param pageUrl     the URL of the release page
     * @param publishedAt the date the release was published
     */
    public GitRelease(GitTag tag, String pageUrl, String publishedAt) {
        this.tag = tag;
        this.pageUrl = pageUrl;
        this.publishedAt = publishedAt;
    }

    /**
     * Gets the tag associated with this release.
     *
     * @return the GitTag associated with this release
     */
    public GitTag getTag() {
        return tag;
    }

    /**
     * Gets the URL of the release page.
     *
     * @return the URL of the release page
     */
    public String getPageUrl() {
        return pageUrl;
    }

    /**
     * Gets the date when the release was published.
     *
     * @return the date the release was published
     */
    public String getPublishedAt() {
        return publishedAt;
    }
}
