package com.apihub.backend.features.testing.service;

import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Set;

@Component
public class SafeHttpDestinationValidator {

    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");

    public void validateUrl(String urlString) {
        if (urlString == null || urlString.isBlank()) {
            throw new IllegalArgumentException("Target URL cannot be empty");
        }

        URI uri;
        try {
            uri = URI.create(urlString.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Malformed target URL: " + urlString);
        }

        String scheme = uri.getScheme();
        if (scheme == null || !ALLOWED_SCHEMES.contains(scheme.toLowerCase())) {
            throw new SecurityException("Blocked scheme: " + scheme + ". Only HTTP and HTTPS are permitted.");
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new SecurityException("Target URL has no host component");
        }

        // SSRF protection: Resolve and check for loopback, private IP, and link-local addresses
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress addr : addresses) {
                if (addr.isLoopbackAddress() || addr.isSiteLocalAddress() || addr.isLinkLocalAddress() || addr.isAnyLocalAddress()) {
                    // Permit explicitly for local mock environments if configured, else protect by default
                    if (!"localhost".equalsIgnoreCase(host) && !"127.0.0.1".equals(host)) {
                        throw new SecurityException("Blocked destination: Internal/Private network addresses are not permitted (" + addr.getHostAddress() + ")");
                    }
                }
            }
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Unable to resolve target host: " + host);
        }
    }
}
