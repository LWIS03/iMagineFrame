package be.uantwerpen.fti.se.imagineframe_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {
    private String upload = "uploads";
    private final Path location;
    private final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = new HashSet<>(
            Arrays.asList(".jpg",".jpeg",".png")
    );

    private static final Set<String> ALLOWED_CONTENT_TYPES = new HashSet<>(
            Arrays.asList("image/jpeg","image/png")
    );

    public FileStorageService() throws IOException {
        this.location = Paths.get(upload);
        try {
            Files.createDirectories(this.location);
        } catch (IOException e) {
            throw e;
        }
    }

    public String storeFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File cannot be empty");
        }

        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf('.')).toLowerCase();
        }

        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only images are allowed");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only JPG and PNG is supported");
        }

        String newFilename = UUID.randomUUID() + extension;
        try {
            Files.copy(file.getInputStream(), location.resolve(newFilename), StandardCopyOption.REPLACE_EXISTING);
            logger.info("File stored: {}", newFilename);
            return newFilename;
        } catch (IOException e) {
            logger.error("failed to store file: {}", e.getMessage());
            throw e;
        }
    }
}