package io.github.mcengine.mcutil.git;

import java.io.IOException;

public interface IGit {

    /**
     * Fetch the latest tag for the given repository.
     *
     * @param org   organization or owner name
     * @param repo  repository name
     * @param token personal access token for private repos (nullable for public)
     * @return latest tag name (e.g., v2026.1.0)
     */
    String getLatestTag(String org, String repo, String token) throws IOException;

    /**
     * Compare the current version to the latest remote tag.
     *
     * @param currentVersion current semantic-like version (e.g., 2026.1.0-1)
     * @param org            organization or owner name
     * @param repo           repository name
     * @param token          personal access token for private repos (nullable for public)
     * @return {@code true} if a newer version exists remotely
     */
    boolean compareVersion(String currentVersion, String org, String repo, String token) throws IOException;
}
