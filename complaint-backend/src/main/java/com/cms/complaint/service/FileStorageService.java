package com.cms.complaint.service;

import com.cms.complaint.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

/** Saves complaint images to the uploads folder (app.upload-dir). */
@Service
public class FileStorageService {

    public record StoredFile(String storedName, String originalName, String contentType) {
    }

    private static final long MAX_BYTES = 2L * 1024 * 1024;
    private static final Map<String, String> EXT = Map.of(
            "image/png", "png", "image/jpeg", "jpg", "image/jpg", "jpg", "image/gif", "gif", "image/webp", "webp");

    private final Path root;

    public FileStorageService(@Value("${app.upload-dir:uploads}") String dir) {
        this.root = Paths.get(dir).toAbsolutePath().normalize();
    }

    /** Accepts a data URL like data:image/png;base64,AAAA... */
    public StoredFile saveDataUrl(String dataUrl) {
        int comma = dataUrl.indexOf(',');
        if (!dataUrl.startsWith("data:") || comma < 0) {
            throw ApiException.badRequest("Invalid image data");
        }
        String header = dataUrl.substring(5, comma);
        if (!header.endsWith(";base64")) {
            throw ApiException.badRequest("Invalid image data");
        }
        String mime = header.substring(0, header.length() - 7).toLowerCase();
        String ext = EXT.get(mime);
        if (ext == null) {
            throw ApiException.badRequest("Only PNG, JPG, GIF or WEBP images are allowed");
        }
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(dataUrl.substring(comma + 1));
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Invalid image data");
        }
        if (bytes.length > MAX_BYTES) {
            throw ApiException.badRequest("Image must be under 2MB");
        }
        String stored = UUID.randomUUID() + "." + ext;
        try {
            Files.createDirectories(root);
            Files.write(root.resolve(stored), bytes);
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save image");
        }
        return new StoredFile(stored, "image." + ext, mime);
    }

    public String readAsDataUrl(String storedName, String contentType) {
        try {
            Path p = root.resolve(storedName).normalize();
            if (!p.startsWith(root) || !Files.exists(p)) {
                return null;
            }
            return "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(Files.readAllBytes(p));
        } catch (IOException e) {
            return null;
        }
    }
}
