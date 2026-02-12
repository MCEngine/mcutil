package io.github.mcengine.mcutil;

import io.github.mcengine.mcutil.git.IGit;
import io.github.mcengine.mcutil.github.MCUtilGitHub;

import java.io.IOException;

public class MCUtil {

    /**
     * Compare a local version with the latest remote tag for the given Git provider.
     *
     * @param gitType        git provider (e.g., "github")
     * @param currentVersion local version string (e.g., 2026.1.0-1)
     * @param org            organization or owner
     * @param repo           repository name
     * @param token          personal access token (nullable for public repos)
     * @return {@code true} if a newer version exists remotely
     */
    public static boolean compareVersion(String gitType, String currentVersion, String org, String repo, String token) throws IOException {
        IGit git = resolve(gitType);
        return git.compareVersion(currentVersion, org, repo, token);
    }

    private static IGit resolve(String gitType) {
        if (gitType == null) {
            throw new IllegalArgumentException("gitType cannot be null");
        }

        switch (gitType.toLowerCase()) {
            case "github":
                return new MCUtilGitHub();
            default:
                throw new IllegalArgumentException("Unsupported git provider: " + gitType);
        }
    }
}
