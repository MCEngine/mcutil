package io.github.mcengine.mcutil.gitlab;

import io.github.mcengine.mcutil.git.GitTagUtil;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Fetches the latest version-like tag for a GitLab project.
 */
final class GetLatestTag {

    private GetLatestTag() {
        // operation holder
    }

    static String execute(HttpClient httpClient, String org, String repo, String token) throws IOException {
        try {
            String projectId = ProjectId.encode(org, repo);
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(String.format("%s/projects/%s/repository/tags", MCUtilGitLab.API_BASE, projectId)))
                    .timeout(Duration.ofSeconds(20))
                    .header("Accept", "application/json");

            if (token != null && !token.isBlank()) {
                builder.header("PRIVATE-TOKEN", token.trim());
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());

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
}
