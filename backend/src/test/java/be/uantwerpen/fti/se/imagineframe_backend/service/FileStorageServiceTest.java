package be.uantwerpen.fti.se.imagineframe_backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    @Mock
    private MultipartFile multipartFile;

    @Autowired
    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(fileStorageService, "location", tempDir);
    }

    @Test
    void storeFile_ShouldStoreFileWithCorrectExtension() throws IOException {
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("test content".getBytes())); //create fake file content
        when(multipartFile.getOriginalFilename()).thenReturn("test.jpg");
        when(multipartFile.getContentType()).thenReturn("image/jpeg");

        String filename = fileStorageService.storeFile(multipartFile);

        assertTrue(filename.endsWith(".jpg"));
        assertTrue(Files.exists(tempDir.resolve(filename)));
    }
}
