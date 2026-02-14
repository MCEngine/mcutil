package io.github.mcengine.mcutil.git;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Shared helpers for parsing and comparing git tag versions.
 */
public final class GitTagUtil {

    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d{4})\\.(\\d{1,2})\\.(\\d{1,2})(?:-(\\d+))?");
    private static final Pattern NAME_PATTERN = Pattern.compile("\\\"name\\\"\\s*:\\s*\\\"([^\\\\\"]+)\\\"");

    private GitTagUtil() {
        // utility
    }

    public static Optional<String> extractLatestTag(String body) {
        Matcher matcher = NAME_PATTERN.matcher(body);
        return Stream.generate(() -> matcher)
                .takeWhile(Matcher::find)
                .map(m -> matcher.group(1))
                .filter(GitTagUtil::isVersionLike)
                .max(GitTagUtil::compareVersionStrings);
    }

    public static boolean isVersionLike(String tag) {
        String normalized = normalizeTag(tag);
        return VERSION_PATTERN.matcher(normalized).matches();
    }

    public static String normalizeTag(String tag) {
        if (tag == null) return "";
        String trimmed = tag.trim();
        if (trimmed.startsWith("v") || trimmed.startsWith("V")) {
            return trimmed.substring(1);
        }
        return trimmed;
    }

    public static boolean isNewer(String current, String candidate) {
        return compareVersionStrings(candidate, current) > 0;
    }

    public static int compareVersionStrings(String a, String b) {
        Matcher ma = VERSION_PATTERN.matcher(normalizeTag(a));
        Matcher mb = VERSION_PATTERN.matcher(normalizeTag(b));

        if (!ma.matches() || !mb.matches()) {
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

        return Integer.compare(iterA, iterB);
    }
}
