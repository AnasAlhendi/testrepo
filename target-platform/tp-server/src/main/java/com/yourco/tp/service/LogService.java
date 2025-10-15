package com.yourco.tp.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

@Service
public class LogService {

    private final HomeService home;

    public LogService(HomeService home) { this.home = home; }

    public Path logsDir() { return home.home().resolve("logs"); }

    public Path programLog(String id) { return logsDir().resolve("program-" + id + ".log"); }
    public Path pluginLog(String id) { return logsDir().resolve("plugin-" + id + ".log"); }
    public Path serverLog() { return logsDir().resolve("tp-server.log"); }

    public List<String> tail(Path file, int maxLines) {
        try {
            if (!Files.exists(file)) return List.of();
            var lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            int from = Math.max(0, lines.size() - maxLines);
            return new ArrayList<>(lines.subList(from, lines.size()));
        } catch (IOException e) {
            return List.of("<error reading log: " + e.getMessage() + ">");
        }
    }
}

