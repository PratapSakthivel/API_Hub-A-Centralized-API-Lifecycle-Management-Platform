package com.apihub.backend.features.release.domain;

public enum ReleaseStage {
    DEVELOPMENT,
    TESTING,
    STAGING,
    PRODUCTION;

    public ReleaseStage next() {
        return switch (this) {
            case DEVELOPMENT -> TESTING;
            case TESTING -> STAGING;
            case STAGING -> PRODUCTION;
            case PRODUCTION -> throw new IllegalStateException("API is already in PRODUCTION stage.");
        };
    }
}
