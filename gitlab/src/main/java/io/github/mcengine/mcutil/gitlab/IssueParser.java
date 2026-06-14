package io.github.mcengine.mcutil.gitlab;

import io.github.mcengine.mcutil.git.GitIssue;
import io.github.mcengine.mcutil.git.GitJson;

/**
 * Maps a GitLab issue JSON payload to a {@link GitIssue}.
 */
final class IssueParser {

    private IssueParser() {
        // utility
    }

    static GitIssue parse(String body) {
        long iid = GitJson.number(body, "iid").orElse(-1L);
        String title = GitJson.string(body, "title").orElse("");
        String description = GitJson.string(body, "description").orElse("");
        String url = GitJson.string(body, "web_url").orElse("");
        String state = GitJson.string(body, "state").orElse("");
        return new GitIssue(iid, title, description, url, state);
    }
}
