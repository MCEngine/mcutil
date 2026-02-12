package io.github.mcengine.mcutil.github;

import io.github.mcengine.mcutil.git.IGit;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * GitHub implementation of version checking via tags.
 */
public class MCUtilGitHub implements IGit {

    private static final String API_BASE = "https://api.github.com";
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d{4})\\.(\\d{1,2})\\.(\\d{1,2})(?:-(\\d+))?");

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public String getLatestTag(String org, String repo, String token) throws IOException {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(String.format("%s/repos/%s/%s/tags", API_BASE, org, repo)))
                    .timeout(Duration.ofSeconds(20))
                    .header("Accept", "application/vnd.github+json");

            if (token != null && !token.isBlank()) {
                builder.header("Authorization", "Bearer " + token.trim());
            }

            HttpRequest request = builder.build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new IOException("GitHub API error: " + response.statusCode() + " body: " + response.body());
            }

            return parseLatestTag(response.body())
                    .orElseThrow(() -> new IOException("No tags found for repo: " + org + "/" + repo));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }

    @Override
    public boolean compareVersion(String currentVersion, String org, String repo, String token) throws IOException {
        String latestTag = getLatestTag(org, repo, token);
        String normalizedLatest = normalizeTag(latestTag);
        String normalizedCurrent = normalizeTag(currentVersion);
        return isNewer(normalizedCurrent, normalizedLatest);
    }

    private Optional<String> parseLatestTag(String body) {
        // Simple JSON-less extraction: find all tag names and pick the newest by comparator
        // Expected structure: [{"name":"v2026.1.0",...}, ...]
        Pattern namePattern = Pattern.compile("\"name\"\s*:\s*\"([^\"]+)\"");
        Matcher matcher = namePattern.matcher(body);
        return Stream.generate(() -> matcher)
                .takeWhile(Matcher::find)
                .map(m -> matcher.group(1))
                .filter(this::isVersionLike)
                .max(this::compareVersionStrings);
    }

    private boolean isVersionLike(String tag) {
        String normalized = normalizeTag(tag);
        return VERSION_PATTERN.matcher(normalized).matches();
    }

    private String normalizeTag(String tag) {
        if (tag == null) return "";
        String trimmed = tag.trim();
        if (trimmed.startsWith("v") || trimmed.startsWith("V")) {
            return trimmed.substring(1);
        }
        return trimmed;
    }

    private boolean isNewer(String current, String candidate) {
        return compareVersionStrings(candidate, current) > 0;
    }

    private int compareVersionStrings(String a, String b) {
        Matcher ma = VERSION_PATTERN.matcher(normalizeTag(a));
        Matcher mb = VERSION_PATTERN.matcher(normalizeTag(b));

        if (!ma.matches() || !mb.matches()) {
            // Fallback lexicographic if pattern fails
            return a.compareTo(b);
        }

        int yearA = Integer.parseInt(ma.group(1));
        int monthA = Integer.parseInt(ma.group(2));
        int patchA = Integer.parseInt(ma.group(3));
        int iterA = ma.group(4) != null ? Integer.parseInt(ma.group(4)) : Integer.MAX_VALUE;

        int yearB = Integer.parseInt(mb.group(1));
        int monthB = Integer.parseInt(mb.group(2));
        int patchB = Integer.parseInt(mb.group(3));
        int iterB = mb.group(4) != null ? Integer.parseInt(mb.group(4)) : Integer.MAX_VALUE;

        int cmp = Integer.compare(yearA, yearB);
        if (cmp != 0) return cmp;

        cmp = Integer.compare(monthA, monthB);
        if (cmp != 0) return cmp;

        cmp = Integer.compare(patchA, patchB);
        if (cmp != 0) return cmp;

        // For iterations: missing iteration (stable) treated as newest -> use MAX_VALUE above
        return Integer.compare(iterA, iterB);
    }
}
