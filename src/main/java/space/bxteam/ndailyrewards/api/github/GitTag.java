package space.bxteam.ndailyrewards.api.github;

/**
 * Represents a Git tag, typically used to mark a specific release.
 */
public class GitTag {
    private final String tag;

    /**
     * Private constructor to create a GitTag instance.
     *
     * @param tag the name of the tag
     */
    private GitTag(String tag) {
        this.tag = tag;
    }

    /**
     * Creates a new instance of GitTag.
     *
     * @param tag the name of the tag
     * @return a new GitTag instance
     */
    public static GitTag of(String tag) {
        return new GitTag(tag);
    }

    /**
     * Gets the name of the tag.
     *
     * @return the name of the tag
     */
    public String getTag() {
        return tag;
    }
}
