package com.yourco.registry.api;

import com.yourco.registry.service.StorageService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

@RestController
@RequestMapping("/api/files")
public class FilesController {
    private final StorageService storage;
    public FilesController(StorageService storage) { this.storage = storage; }

    @GetMapping("/**")
    public ResponseEntity<Resource> get(org.springframework.web.servlet.HandlerMapping mapping, javax.servlet.http.HttpServletRequest request) {
        String path = (String) request.getAttribute(org.springframework.web.servlet.HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String pattern = (String) request.getAttribute(org.springframework.web.servlet.HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String relative = new org.springframework.util.AntPathMatcher().extractPathWithinPattern(pattern, path);
        Path file = storage.root().resolve(relative).normalize();
        if (!file.startsWith(storage.root()) || !java.nio.file.Files.exists(file)) {
            return ResponseEntity.notFound().build();
        }
        Resource res = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + file.getFileName())
                .body(res);
    }
}

