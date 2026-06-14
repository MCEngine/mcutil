package io.github.mcengine.mcutil.github;

import io.github.mcengine.mcutil.git.GitIssue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Fetches a single issue from a GitHub repository by its number.
 */
final class GetIssue {

    private GetIssue() {
        // operation holder
    }

    static GitIssue execute(HttpClient httpClient, String org, String repo, String token, long issueId) throws IOException {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(String.format("%s/repos/%s/%s/issues/%d", MCUtilGitHub.API_BASE, org, repo, issueId)))
                    .timeout(Duration.ofSeconds(20))
                    .header("Accept", "application/vnd.github+json");

            if (token != null && !token.isBlank()) {
                builder.header("Authorization", "Bearer " + token.trim());
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new IOException("GitHub API error: " + response.statusCode() + " body: " + response.body());
            }

            return IssueParser.parse(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }
}
