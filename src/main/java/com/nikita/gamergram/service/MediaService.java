package com.nikita.gamergram.service;

import com.nikita.gamergram.dto.MediaResponse;
import com.nikita.gamergram.entity.enums.MediaType;
import com.nikita.gamergram.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@Service
public class MediaService {

    private record AllowedType(String extension, MediaType mediaType) {
    }

    // No SVG or HTML: served from our domain, they could run scripts.
    // The extension comes from here, never from the client filename.
    private static final Map<String, AllowedType> ALLOWED_TYPES = Map.of(
            "image/jpeg", new AllowedType("jpg", MediaType.PHOTO),
            "image/png", new AllowedType("png", MediaType.PHOTO),
            "image/webp", new AllowedType("webp", MediaType.PHOTO),
            "image/gif", new AllowedType("gif", MediaType.PHOTO),
            "video/mp4", new AllowedType("mp4", MediaType.VIDEO),
            "video/webm", new AllowedType("webm", MediaType.VIDEO)
    );

    private final Path uploadDir;

    public MediaService(@Value("${app.upload-dir}") String uploadDir) {
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public MediaResponse store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        AllowedType type = ALLOWED_TYPES.get(file.getContentType());
        if (type == null) {
            throw new BadRequestException("Unsupported file type: " + file.getContentType());
        }

        String filename = UUID.randomUUID() + "." + type.extension();
        try (InputStream in = file.getInputStream()) {
            Files.createDirectories(uploadDir);
            Files.copy(in, uploadDir.resolve(filename));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to save file", e);
        }

        return new MediaResponse("/uploads/" + filename, type.mediaType());
    }
}
