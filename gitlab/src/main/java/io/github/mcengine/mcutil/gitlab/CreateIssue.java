package io.github.mcengine.mcutil.gitlab;

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
 * Creates a new issue in a GitLab project.
 */
final class CreateIssue {

    private CreateIssue() {
        // operation holder
    }

    static GitIssue execute(HttpClient httpClient, String org, String repo, String token, String title, String body) throws IOException {
        String payload = "{\"title\":\"" + GitJson.escape(title) + "\",\"description\":\"" + GitJson.escape(body) + "\"}";
        try {
            String projectId = ProjectId.encode(org, repo);
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .uri(URI.create(String.format("%s/projects/%s/issues", MCUtilGitLab.API_BASE, projectId)))
                    .timeout(Duration.ofSeconds(20))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json");

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
