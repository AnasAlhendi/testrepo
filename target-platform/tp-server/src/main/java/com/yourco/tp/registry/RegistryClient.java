package com.yourco.tp.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourco.tp.util.TpValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

@Service
public class RegistryClient {
    private final String registryUrl;
    private final PublicKeyLoader publicKeyLoader;
    private final ObjectMapper mapper = new ObjectMapper();
    private final TpValidator validator = new TpValidator();

    public RegistryClient(@Value("${tp.registry.url:}") String registryUrl,
                          PublicKeyLoader publicKeyLoader) {
        this.registryUrl = registryUrl != null ? registryUrl.replaceAll("/+$", "") : "";
        this.publicKeyLoader = publicKeyLoader;
    }

    public boolean isConfigured() { return registryUrl != null && !registryUrl.isBlank(); }

    public RegistryCatalog fetchAndVerify() {
        if (!isConfigured()) throw new IllegalStateException("registry.url not configured");
        String json = httpGet(registryUrl + "/catalog.json");
        String sigB64 = httpGet(registryUrl + "/catalog.sig");
        verify(json.getBytes(), sigB64);
        try {
            RegistryCatalog cat = mapper.readValue(json, RegistryCatalog.class);
            if (cat != null) {
                cat.getPrograms().forEach(validator::validate);
                cat.getPlugins().forEach(validator::validate);
            }
            return cat;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse catalog", e);
        }
    }

    private void verify(byte[] data, String sigB64) {
        if (!publicKeyLoader.isConfigured()) throw new IllegalStateException("publicKeyPath not configured");
        try {
            PublicKey key = publicKeyLoader.get();
            byte[] sigBytes = Base64.getDecoder().decode(sigB64.trim());
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(key);
            sig.update(data);
            if (!sig.verify(sigBytes)) throw new IllegalStateException("catalog signature verification failed");
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify catalog signature", e);
        }
    }

    private String httpGet(String url) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) return resp.body();
            throw new RuntimeException("HTTP " + resp.statusCode() + " calling " + url);
        } catch (Exception e) {
            throw new RuntimeException("Failed HTTP GET " + url, e);
        }
    }
}

