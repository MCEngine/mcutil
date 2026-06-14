package io.github.mcengine.mcutil.gitlab;

import io.github.mcengine.mcutil.git.GitIssue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Fetches a single issue from a GitLab project by its iid.
 */
final class GetIssue {

    private GetIssue() {
        // operation holder
    }

    static GitIssue execute(HttpClient httpClient, String org, String repo, String token, long issueId) throws IOException {
        try {
            String projectId = ProjectId.encode(org, repo);
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(String.format("%s/projects/%s/issues/%d", MCUtilGitLab.API_BASE, projectId, issueId)))
                    .timeout(Duration.ofSeconds(20))
                    .header("Accept", "application/json");

            if (token != null && !token.isBlank()) {
                builder.header("PRIVATE-TOKEN", token.trim());
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new IOException("GitLab API error: " + response.statusCode() + " body: " + response.body());
            }

            return IssueParser.parse(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }
}
