package com.yourco.registry.api;

import com.yourco.registry.service.CatalogService;
import com.yourco.registry.service.SigningService;
import com.yourco.registry.service.StorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api")
public class RegistryController {

    private final CatalogService catalogService;
    private final SigningService signingService;
    private final StorageService storageService;

    public RegistryController(CatalogService catalogService, SigningService signingService, StorageService storageService) {
        this.catalogService = catalogService;
        this.signingService = signingService;
        this.storageService = storageService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @GetMapping("/catalog")
    public ResponseEntity<Map<String, Object>> catalog() {
        return ResponseEntity.ok(Map.of(
                "plugins", List.copyOf(catalogService.getPlugins()),
                "programs", List.copyOf(catalogService.getPrograms())
        ));
    }

    @GetMapping(value = "/catalog.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> catalogJson() {
        return ResponseEntity.ok(catalogService.catalogJson());
    }

    @GetMapping("/catalog.sig")
    public ResponseEntity<String> catalogSignature() {
        if (!signingService.isConfigured()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("signing key not configured");
        String sig = signingService.signToBase64(catalogService.catalogBytes());
        return ResponseEntity.ok(sig);
    }

    @PostMapping("/catalog/plugins")
    public ResponseEntity<Map<String, Object>> addPlugin(@RequestBody Map<String, Object> plugin) {
        catalogService.addPlugin(plugin);
        catalogService.persist();
        signingService.writeSignature(catalogService.catalogBytes());
        return ResponseEntity.ok(plugin);
    }

    @PostMapping("/catalog/programs")
    public ResponseEntity<Map<String, Object>> addProgram(@RequestBody Map<String, Object> program) {
        catalogService.addProgram(program);
        catalogService.persist();
        signingService.writeSignature(catalogService.catalogBytes());
        return ResponseEntity.ok(program);
    }

    @PostMapping(value = "/plugins/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadPlugin(
            @RequestParam("id") String id,
            @RequestParam("version") String version,
            @RequestParam("file") MultipartFile file) {
        var path = storageService.storePlugin(id, version, file);
        var rel = storageService.root().relativize(path).toString().replace('\\','/');
        var plugin = Map.of(
                "id", id,
                "version", version,
                "path", path.toString(),
                "url", "/api/files/" + rel
        );
        catalogService.addPlugin(plugin);
        catalogService.persist();
        signingService.writeSignature(catalogService.catalogBytes());
        return ResponseEntity.ok(plugin);
    }

    @PostMapping(value = "/programs/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadProgram(
            @RequestParam("id") String id,
            @RequestParam("version") String version,
            @RequestParam("file") MultipartFile file) {
        var path = storageService.storeProgram(id, version, file);
        var rel = storageService.root().relativize(path).toString().replace('\\','/');
        var program = Map.of(
                "id", id,
                "version", version,
                "path", path.toString(),
                "url", "/api/files/" + rel
        );
        catalogService.addProgram(program);
        catalogService.persist();
        signingService.writeSignature(catalogService.catalogBytes());
        return ResponseEntity.ok(program);
    }
}
