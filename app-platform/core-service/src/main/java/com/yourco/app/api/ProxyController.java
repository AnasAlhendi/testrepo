package com.yourco.app.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Collections;

@RestController
@RequestMapping("/api")
public class ProxyController {

    private final RestTemplate rest = new RestTemplate();

    @Value("${tp.baseUrl}")
    private String tpBase;
    @Value("${registry.baseUrl}")
    private String regBase;

    @GetMapping("/ui-config")
    public ResponseEntity<?> uiConfig() {
        return ResponseEntity.ok(java.util.Map.of(
                "tp", "/api/tp",
                "registry", "/api/registry"
        ));
    }

    @RequestMapping(value = "/tp/**")
    public ResponseEntity<byte[]> proxyTp(HttpMethod method, HttpServletRequest req, @RequestBody(required = false) byte[] body) {
        return forward(method, req, body, tpBase, "/api/tp");
    }

    @RequestMapping(value = "/registry/**")
    public ResponseEntity<byte[]> proxyRegistry(HttpMethod method, HttpServletRequest req, @RequestBody(required = false) byte[] body) {
        return forward(method, req, body, regBase, "/api/registry");
    }

    private ResponseEntity<byte[]> forward(HttpMethod method, HttpServletRequest req, byte[] body, String base, String prefix) {
        try {
            String path = req.getRequestURI().substring(prefix.length());
            String qs = req.getQueryString();
            String url = base + path + (qs != null ? ("?" + qs) : "");
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.ALL));
            if (body != null) headers.setContentType(MediaType.APPLICATION_JSON);
            RequestEntity<byte[]> request = new RequestEntity<>(body, headers, method, URI.create(url));
            ResponseEntity<byte[]> resp = rest.exchange(request, byte[].class);
            return ResponseEntity.status(resp.getStatusCode()).headers(resp.getHeaders()).body(resp.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(502).body(("Proxy error: " + e.getMessage()).getBytes());
        }
    }
}

