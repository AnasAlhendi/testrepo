package com.yourco.tp.service;

import com.yourco.tp.model.ProgramSpec;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
public class ProgramSupervisor {
    private final HomeService homeService;
    private final LogService logService;
    private final MavenResolver mavenResolver;
    private final DownloadService downloadService;

    private final Map<String, Process> processes = new HashMap<>();

    public ProgramSupervisor(HomeService homeService, LogService logService, MavenResolver mavenResolver, DownloadService downloadService) {
        this.homeService = homeService;
        this.logService = logService;
        this.mavenResolver = mavenResolver;
        this.downloadService = downloadService;
    }

    public synchronized void start(ProgramSpec spec) {
        if (processes.containsKey(spec.getId()) && processes.get(spec.getId()).isAlive()) return;
        Path jar = resolveArtifact(spec);
        List<String> cmd = new ArrayList<>();
        cmd.add("java");
        if (spec.getJvmArgs() != null) cmd.addAll(spec.getJvmArgs());
        cmd.add("-jar");
        cmd.add(jar.toString());
        if (spec.getArgs() != null) cmd.addAll(spec.getArgs());
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(homeService.home().toFile());
        pb.redirectErrorStream(true);
        try {
            Files.createDirectories(logService.logsDir());
            Path logFile = logService.programLog(spec.getId());
            OutputStream log = Files.newOutputStream(logFile, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
            Process p = pb.start();
            processes.put(spec.getId(), p);
            // Pipe output asynchronously
            new Thread(() -> {
                try (var in = p.getInputStream(); log) {
                    in.transferTo(log);
                } catch (IOException ignored) {}
            }, "program-log-" + spec.getId()).start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to start program: " + spec.getId(), e);
        }
    }

    public synchronized void stop(String id) {
        Process p = processes.get(id);
        if (p != null && p.isAlive()) {
            p.destroy();
            try { p.waitFor(); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        }
        processes.remove(id);
    }

    private Path resolveArtifact(ProgramSpec spec) {
        if (spec.getSource() == null) throw new IllegalArgumentException("program source required");
        if (spec.getSource().getPath() != null && !spec.getSource().getPath().isBlank()) {
            Path p = Path.of(spec.getSource().getPath());
            if (!Files.exists(p)) throw new IllegalArgumentException("program path not found: " + p);
            return p;
        }
        if (spec.getSource().getUrl() != null && !spec.getSource().getUrl().isBlank()) {
            return downloadService.downloadUrl(spec.getSource().getUrl(), spec.getId() + (spec.getVersion() != null ? ("-" + spec.getVersion()) : "") + ".jar");
        }
        if (spec.getSource().getMaven() != null && !spec.getSource().getMaven().isBlank()) {
            return mavenResolver.resolve(spec.getSource().getMaven());
        }
        throw new IllegalArgumentException("Unsupported source for program: " + spec.getId());
    }
}

