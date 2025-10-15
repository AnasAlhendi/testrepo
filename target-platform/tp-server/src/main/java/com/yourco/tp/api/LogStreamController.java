package com.yourco.tp.api;

import com.yourco.tp.service.LogService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/logs")
public class LogStreamController {
    private final LogService logService;
    private final ExecutorService exec = Executors.newCachedThreadPool();

    public LogStreamController(LogService logService) { this.logService = logService; }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam String target) {
        SseEmitter emitter = new SseEmitter(0L);
        Path file = resolve(target);
        exec.submit(() -> {
            try {
                long pos = Files.exists(file) ? Files.size(file) : 0L;
                emitter.send("starting");
                while (true) {
                    if (Files.exists(file)) {
                        long size = Files.size(file);
                        if (size > pos) {
                            var bytes = Files.readAllBytes(file);
                            String chunk = new String(bytes, (int) pos, (int) (size - pos));
                            pos = size;
                            for (String line : chunk.split("\r?\n")) {
                                if (!line.isEmpty()) emitter.send(line);
                            }
                        }
                    }
                    Thread.sleep(1000);
                }
            } catch (IOException | InterruptedException e) {
                try { emitter.complete(); } catch (Exception ignored) {}
                Thread.currentThread().interrupt();
            }
        });
        return emitter;
    }

    private Path resolve(String target) {
        if (target.equals("server")) return logService.serverLog();
        if (target.startsWith("program:")) return logService.programLog(target.substring("program:".length()));
        if (target.startsWith("plugin:")) return logService.pluginLog(target.substring("plugin:".length()));
        return logService.serverLog();
    }
}

