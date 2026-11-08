package com.nikita.gamergram.service;

import com.nikita.gamergram.dto.MediaResponse;
import com.nikita.gamergram.entity.enums.MediaType;
import com.nikita.gamergram.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MediaServiceTest {

    @TempDir
    Path uploadDir;

    @Test
    void store_savesFile_underGeneratedName() throws IOException {
        MediaService service = new MediaService(uploadDir.toString());
        MockMultipartFile file = new MockMultipartFile("file", "../../evil.html", "image/png", new byte[]{1, 2, 3});

        MediaResponse response = service.store(file);

        assertThat(response.mediaType()).isEqualTo(MediaType.PHOTO);
        assertThat(response.url()).startsWith("/uploads/").endsWith(".png").doesNotContain("evil");
        Path stored = uploadDir.resolve(response.url().substring("/uploads/".length()));
        assertThat(Files.readAllBytes(stored)).containsExactly(1, 2, 3);
    }

    @Test
    void store_detectsVideo() {
        MediaService service = new MediaService(uploadDir.toString());
        MockMultipartFile file = new MockMultipartFile("file", "clip.mp4", "video/mp4", new byte[]{1});

        assertThat(service.store(file).mediaType()).isEqualTo(MediaType.VIDEO);
    }

    @Test
    void store_rejectsSvg() {
        MediaService service = new MediaService(uploadDir.toString());
        MockMultipartFile file = new MockMultipartFile("file", "x.svg", "image/svg+xml", "<svg/>".getBytes());

        assertThatThrownBy(() -> service.store(file)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void store_rejectsEmptyFile() {
        MediaService service = new MediaService(uploadDir.toString());
        MockMultipartFile file = new MockMultipartFile("file", "x.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> service.store(file)).isInstanceOf(BadRequestException.class);
    }
}
