package io.github.mcengine.mcutil.github;

import io.github.mcengine.mcutil.git.GitIssue;
import io.github.mcengine.mcutil.git.GitJson;

/**
 * Maps a GitHub issue JSON payload to a {@link GitIssue}.
 */
final class IssueParser {

    private IssueParser() {
        // utility
    }

    static GitIssue parse(String body) {
        long number = GitJson.number(body, "number").orElse(-1L);
        String title = GitJson.string(body, "title").orElse("");
        String issueBody = GitJson.string(body, "body").orElse("");
        String url = GitJson.string(body, "html_url").orElse("");
        String state = GitJson.string(body, "state").orElse("");
        return new GitIssue(number, title, issueBody, url, state);
    }
}
