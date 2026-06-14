package io.github.mcengine.mcutil.github;

import io.github.mcengine.mcutil.git.GitIssue;
import io.github.mcengine.mcutil.git.GitJson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Creates a new issue in a GitHub repository.
 */
final class CreateIssue {

    private CreateIssue() {
        // operation holder
    }

    static GitIssue execute(HttpClient httpClient, String org, String repo, String token, String title, String body) throws IOException {
        String payload = "{\"title\":\"" + GitJson.escape(title) + "\",\"body\":\"" + GitJson.escape(body) + "\"}";
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .uri(URI.create(String.format("%s/repos/%s/%s/issues", MCUtilGitHub.API_BASE, org, repo)))
                    .timeout(Duration.ofSeconds(20))
                    .header("Accept", "application/vnd.github+json")
                    .header("Content-Type", "application/json");

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
