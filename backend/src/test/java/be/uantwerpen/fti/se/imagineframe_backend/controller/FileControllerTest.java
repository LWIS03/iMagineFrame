package be.uantwerpen.fti.se.imagineframe_backend.controller;

import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.GlobalExceptionHandler;
import be.uantwerpen.fti.se.imagineframe_backend.security.UrlSecurity;
import be.uantwerpen.fti.se.imagineframe_backend.service.PdfExportService;
import be.uantwerpen.fti.se.imagineframe_backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FileControllerTest {

    @TempDir
    Path tempDir;

    private MockMvc mockMvc;
    private FileController fileController;
    private PdfExportService pdfExportService;
    private UserService userService;
    private UrlSecurity urlSecurity;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        fileController = new FileController(pdfExportService, userService, urlSecurity);
        ReflectionTestUtils.setField(fileController, "upload", tempDir.toString());
        mockMvc = MockMvcBuilders.standaloneSetup(fileController).setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    public void testGetImageNotFound() throws Exception {
        mockMvc.perform(get("/api/files/no.jpg")).andExpect(status().isNotFound());
    }

    @Test
    public void testGetImageSuccess() throws Exception {
        byte[] content = "test content".getBytes();
        Files.write(tempDir.resolve("test.jpg"), content);
        mockMvc.perform(get("/api/files/test.jpg")).andExpect(status().isOk()).andExpect(header().string("Content-Type", "application/octet-stream"));
    }
}