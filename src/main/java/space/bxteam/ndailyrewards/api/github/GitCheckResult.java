package space.bxteam.ndailyrewards.api.github;

/**
 * Represents the result of a release check, indicating whether the current version
 * is up-to-date and providing information about the latest release if it is not.
 */
public class GitCheckResult {
    private final boolean upToDate;
    private final GitRelease latestRelease;

    /**
     * Constructs a GitCheckResult instance.
     *
     * @param upToDate      whether the current version is up-to-date
     * @param latestRelease the latest release information, or null if up-to-date
     */
    public GitCheckResult(boolean upToDate, GitRelease latestRelease) {
        this.upToDate = upToDate;
        this.latestRelease = latestRelease;
    }

    /**
     * Checks if the current version is up-to-date.
     *
     * @return true if the current version is up-to-date, false otherwise
     */
    public boolean isUpToDate() {
        return upToDate;
    }

    /**
     * Gets the latest release information if the current version is not up-to-date.
     *
     * @return the latest GitRelease, or null if up-to-date
     */
    public GitRelease getLatestRelease() {
        return latestRelease;
    }
}
