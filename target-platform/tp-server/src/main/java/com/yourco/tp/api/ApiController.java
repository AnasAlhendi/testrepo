package com.yourco.tp.api;

import com.yourco.tp.model.PluginSpec;
import com.yourco.tp.model.ProgramSpec;
import com.yourco.tp.service.PluginService;
import com.yourco.tp.service.ProgramService;
import com.yourco.tp.registry.RegistryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final PluginService pluginService;
    private final ProgramService programService;
    private final RegistryClient registryClient;
    
    public ApiController(PluginService pluginService, ProgramService programService, RegistryClient registryClient, com.yourco.tp.service.LogService logService) {
        this.pluginService = pluginService;
        this.programService = programService;
        this.registryClient = registryClient;
        this.logService = logService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    // Plugins
    @GetMapping("/plugins")
    public ResponseEntity<List<PluginSpec>> listPlugins() {
        return ResponseEntity.ok(pluginService.list());
    }

    @PostMapping("/plugins/install")
    public ResponseEntity<PluginSpec> installPlugin(@RequestBody PluginSpec spec) {
        return ResponseEntity.ok(pluginService.install(spec));
    }

    @PostMapping("/plugins/start")
    public ResponseEntity<PluginSpec> startPlugin(@RequestParam String id) {
        return ResponseEntity.ok(pluginService.start(id));
    }

    @PostMapping("/plugins/stop")
    public ResponseEntity<PluginSpec> stopPlugin(@RequestParam String id) {
        return ResponseEntity.ok(pluginService.stop(id));
    }

    @PostMapping("/plugins/uninstall")
    public ResponseEntity<Map<String, String>> uninstallPlugin(@RequestParam String id) {
        pluginService.uninstall(id);
        return ResponseEntity.ok(Map.of("id", id, "status", "uninstalled"));
    }

    @PostMapping("/plugins/update")
    public ResponseEntity<PluginSpec> updatePlugin(@RequestBody PluginSpec spec) {
        return ResponseEntity.ok(pluginService.update(spec));
    }

    // Programs
    @GetMapping("/programs")
    public ResponseEntity<List<ProgramSpec>> listPrograms() {
        return ResponseEntity.ok(programService.list());
    }

    @PostMapping("/programs/install")
    public ResponseEntity<ProgramSpec> installProgram(@RequestBody ProgramSpec spec) {
        return ResponseEntity.ok(programService.install(spec));
    }

    @PostMapping("/programs/start")
    public ResponseEntity<ProgramSpec> startProgram(@RequestParam String id) {
        return ResponseEntity.ok(programService.start(id));
    }

    @PostMapping("/programs/stop")
    public ResponseEntity<ProgramSpec> stopProgram(@RequestParam String id) {
        return ResponseEntity.ok(programService.stop(id));
    }

    @PostMapping("/programs/restart")
    public ResponseEntity<ProgramSpec> restartProgram(@RequestParam String id) {
        programService.stop(id);
        return ResponseEntity.ok(programService.start(id));
    }

    private final com.yourco.tp.service.LogService logService;

    @GetMapping("/logs")
    public ResponseEntity<List<String>> logs(@RequestParam String target,
                                             @RequestParam(name = "lines", required = false, defaultValue = "200") int lines) {
        if (target.equals("server")) return ResponseEntity.ok(logService.tail(logService.serverLog(), lines));
        if (target.startsWith("program:")) {
            String id = target.substring("program:".length());
            return ResponseEntity.ok(logService.tail(logService.programLog(id), lines));
        }
        if (target.startsWith("plugin:")) {
            String id = target.substring("plugin:".length());
            return ResponseEntity.ok(logService.tail(logService.pluginLog(id), lines));
        }
        return ResponseEntity.badRequest().body(List.of("unknown target"));
    }

    @PostMapping("/registry/sync")
    public ResponseEntity<Map<String, Object>> registrySync() {
        var cat = registryClient.fetchAndVerify();
        cat.getPrograms().forEach(programService::install);
        cat.getPlugins().forEach(pluginService::install);
        return ResponseEntity.ok(Map.of(
                "programs", programService.list(),
                "plugins", pluginService.list()
        ));
    }
}
