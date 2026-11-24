package com.apihub.backend.features.versioning.domain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record SemanticVersion(int major, int minor, int patch) implements Comparable<SemanticVersion> {

    private static final Pattern SEMVER_PATTERN = Pattern.compile("^v?(\\d+)\\.(\\d+)\\.(\\d+)$");

    public static SemanticVersion parse(String versionLabel) {
        if (versionLabel == null) {
            throw new IllegalArgumentException("Version label cannot be null");
        }
        Matcher matcher = SEMVER_PATTERN.matcher(versionLabel.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid semantic version format: " + versionLabel);
        }
        return new SemanticVersion(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3))
        );
    }

    @Override
    public int compareTo(SemanticVersion o) {
        if (this.major != o.major) return Integer.compare(this.major, o.major);
        if (this.minor != o.minor) return Integer.compare(this.minor, o.minor);
        return Integer.compare(this.patch, o.patch);
    }

    public String format() {
        return "v" + major + "." + minor + "." + patch;
    }
}
