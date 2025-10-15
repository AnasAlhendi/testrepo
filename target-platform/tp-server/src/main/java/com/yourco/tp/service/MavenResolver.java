package com.yourco.tp.service;

import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class MavenResolver {
    private final DownloadService downloadService;

    public MavenResolver(DownloadService downloadService) {
        this.downloadService = downloadService;
    }

    // maven: "group:artifact:version:type[:classifier]"
    public Path resolve(String coord) {
        // Stub: attempt to map to a local file under downloads if present; otherwise throw
        String safeName = coord.replace(':', '-');
        Path candidate = downloadService.downloadsDir().resolve(safeName + ".jar");
        if (Files.exists(candidate)) return candidate;
        throw new UnsupportedOperationException("Maven resolution not implemented offline: " + coord);
    }
}

