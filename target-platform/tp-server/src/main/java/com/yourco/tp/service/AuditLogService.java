package com.yourco.tp.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class AuditLogService {
    private final LogService logService;

    public AuditLogService(LogService logService) {
        this.logService = logService;
    }

    public Path auditLog() { return logService.logsDir().resolve("audit.log"); }

    public void record(String action, String target, String id, Map<String, Object> details) {
        String ts = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        String d = details != null ? details.toString() : "{}";
        String line = ts + "\t" + action + "\t" + target + "\t" + id + "\t" + d + System.lineSeparator();
        try {
            Files.createDirectories(logService.logsDir());
            Files.writeString(auditLog(), line, StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException ignored) {}
    }

    public void plugin(String action, String id, Map<String, Object> details) {
        record(action, "plugin", id, details);
    }

    public void program(String action, String id, Map<String, Object> details) {
        record(action, "program", id, details);
    }
}

