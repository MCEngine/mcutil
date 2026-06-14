package io.github.mcengine.mcutil.gitlab;

import io.github.mcengine.mcutil.git.GitTagUtil;
import io.github.mcengine.mcutil.git.IGit;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * GitLab implementation of version checking via tags.
 */
public class MCUtilGitLab implements IGit {

    private static final String API_BASE = "https://gitlab.com/api/v4";
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public String getLatestTag(String org, String repo, String token) throws IOException {
        try {
            String projectId = URLEncoder.encode(org + "/" + repo, StandardCharsets.UTF_8);
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(String.format("%s/projects/%s/repository/tags", API_BASE, projectId)))
                    .timeout(Duration.ofSeconds(20))
                    .header("Accept", "application/json");

            if (token != null && !token.isBlank()) {
                builder.header("PRIVATE-TOKEN", token.trim());
            }

            HttpRequest request = builder.build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new IOException("GitLab API error: " + response.statusCode() + " body: " + response.body());
            }

            return GitTagUtil.extractLatestTag(response.body())
                    .orElseThrow(() -> new IOException("No tags found for repo: " + org + "/" + repo));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }

    @Override
    public boolean compareVersion(String currentVersion, String org, String repo, String token) throws IOException {
        String latestTag = getLatestTag(org, repo, token);
        String normalizedLatest = GitTagUtil.normalizeTag(latestTag);
        String normalizedCurrent = GitTagUtil.normalizeTag(currentVersion);
        return GitTagUtil.isNewer(normalizedCurrent, normalizedLatest);
    }
}
