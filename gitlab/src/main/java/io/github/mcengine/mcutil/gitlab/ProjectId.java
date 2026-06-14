package io.github.mcengine.mcutil.gitlab;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Builds the URL-encoded {@code namespace/project} identifier GitLab expects in
 * its REST API paths.
 */
final class ProjectId {

    private ProjectId() {
        // utility
    }

    static String encode(String org, String repo) {
        return URLEncoder.encode(org + "/" + repo, StandardCharsets.UTF_8);
    }
}
