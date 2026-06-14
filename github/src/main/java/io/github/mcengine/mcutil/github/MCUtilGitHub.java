package io.github.mcengine.mcutil.github;

import io.github.mcengine.mcutil.git.GitIssue;
import io.github.mcengine.mcutil.git.IGit;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;

/**
 * GitHub central provider.
 *
 * <p>This is the entry point for GitHub operations. Each operation lives in its
 * own class ({@link GetLatestTag}, {@link CreateIssue}, {@link GetIssue}); this
 * class wires them to a shared {@link HttpClient}.
 */
public class MCUtilGitHub implements IGit {

    /** Base URL of the GitHub REST API, shared by every operation. */
    static final String API_BASE = "https://api.github.com";

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
