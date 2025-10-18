package com.yourco.app.web;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;

@Controller
public class SpaFallbackController {

    // Serve index.html for non-API routes without file extensions (SPA fallback)
    // PathPattern-compatible catch-all: captures any path segments
    @GetMapping("/{*path}")
    public ResponseEntity<?> spa(@PathVariable("path") String path) throws IOException {
        // Let API namespace and static file requests fall through
        if (path.startsWith("api") || path.contains(".")) {
            return ResponseEntity.notFound().build();
        }
        var index = new ClassPathResource("static/index.html");
        if (index.exists()) {
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(index.getContentAsByteArray());
        }
        return ResponseEntity.notFound().build();
    }
}
