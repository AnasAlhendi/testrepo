package com.yourco.tp.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipSafe {
    private static final long MAX_TOTAL_UNCOMPRESSED = 512L * 1024L * 1024L; // 512MB

    public static void unzip(InputStream in, Path destDir) throws IOException {
        Files.createDirectories(destDir);
        long total = 0L;
        try (ZipInputStream zis = new ZipInputStream(in)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                Path target = destDir.resolve(entry.getName()).normalize();
                if (!target.startsWith(destDir)) {
                    throw new IOException("Zip Slip attack detected: " + entry.getName());
                }
                Files.createDirectories(target.getParent());
                long written = Files.copy(zis, target);
                total += written;
                if (total > MAX_TOTAL_UNCOMPRESSED) {
                    throw new IOException("Zip bomb detected: too large");
                }
            }
        }
    }
}

