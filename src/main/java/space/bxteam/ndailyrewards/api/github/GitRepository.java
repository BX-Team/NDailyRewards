package space.bxteam.ndailyrewards.api.github;

/**
 * Represents a GitHub repository identified by its owner and project name.
 */
public class GitRepository {
    private final String owner;
    private final String project;

    /**
     * Private constructor to create a GitRepository instance.
     *
     * @param owner   the owner of the GitHub repository
     * @param project the name of the GitHub project
     */
    private GitRepository(String owner, String project) {
        this.owner = owner;
        this.project = project;
    }

    /**
     * Creates a new instance of GitRepository.
     *
     * @param owner   the owner of the GitHub repository
     * @param project the name of the GitHub project
     * @return a new GitRepository instance
     */
    public static GitRepository of(String owner, String project) {
        return new GitRepository(owner, project);
    }

    /**
     * Gets the owner of the repository.
     *
     * @return the owner of the repository
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Gets the name of the repository project.
     *
     * @return the name of the repository project
     */
    public String getProject() {
        return project;
    }
}
