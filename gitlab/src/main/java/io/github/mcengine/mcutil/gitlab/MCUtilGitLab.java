package io.github.mcengine.mcutil.gitlab;

import io.github.mcengine.mcutil.git.GitIssue;
import io.github.mcengine.mcutil.git.IGit;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;

/**
 * GitLab central provider.
 *
 * <p>This is the entry point for GitLab operations. Each operation lives in its
 * own class ({@link GetLatestTag}, {@link CreateIssue}, {@link GetIssue}); this
 * class wires them to a shared {@link HttpClient}.
 */
public class MCUtilGitLab implements IGit {

    /** Base URL of the GitLab REST API, shared by every operation. */
    static final String API_BASE = "https://gitlab.com/api/v4";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public String getLatestTag(String org, String repo, String token) throws IOException {
        return GetLatestTag.execute(httpClient, org, repo, token);
    }

    @Override
    public GitIssue createIssue(String org, String repo, String token, String title, String body) throws IOException {
        return CreateIssue.execute(httpClient, org, repo, token, title, body);
    }

    @Override
    public GitIssue getIssue(String org, String repo, String token, long issueId) throws IOException {
        return GetIssue.execute(httpClient, org, repo, token, issueId);
    }
}
