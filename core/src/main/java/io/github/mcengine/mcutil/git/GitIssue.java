package io.github.mcengine.mcutil.git;

/**
 * Provider-agnostic representation of a git issue.
 *
 * @param number issue number/iid as shown in the UI (GitHub {@code number}, GitLab {@code iid})
 * @param title  issue title
 * @param body   issue body/description (may be empty when the remote value is {@code null})
 * @param url    web URL of the issue
 * @param state  issue state (e.g. {@code open}, {@code closed}, {@code opened})
 */
public record GitIssue(long number, String title, String body, String url, String state) {
}
