package be.uantwerpen.fti.se.imagineframe_backend.controller;

import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.EntityNotFoundException;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.security.JWTUtil;
import be.uantwerpen.fti.se.imagineframe_backend.security.UrlSecurity;
import be.uantwerpen.fti.se.imagineframe_backend.service.PdfExportService;
import be.uantwerpen.fti.se.imagineframe_backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/api/files")
@Tag(name = "File Management", description = "API for file operations including image retrieval and PDF report generation")
@SecurityRequirement(name = "Bearer Authentication")
public class FileController {
    @Value("${file.upload-dir}")
    private String upload;

    private final UserService userService;
    private final PdfExportService pdfExportService;
    private final UrlSecurity urlSecurity;


    public FileController(PdfExportService pdfExportService, UserService userService, UrlSecurity urlSecurity) {
        this.pdfExportService = pdfExportService;
        this.userService = userService;
        this.urlSecurity = urlSecurity;
    }

    @Operation(summary = "Retrieve a file by filename", description = "Retrieves a file from storage by filename. Returns a downloadable resource.")
    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) throws IOException {
        Path imagePath = Paths.get(upload).resolve(filename);
        Resource imageFile = new UrlResource(imagePath.toUri());

        if (!imageFile.exists()) {
            throw new EntityNotFoundException("file", filename);
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(imageFile);
    }

    @Operation(summary = "Generate product stock report", description = "Generates a PDF product stock report. Requires user authentication. Returns inline PDF document.")
    @GetMapping("/report/{user_id}")
    public ResponseEntity<Resource> getProductStockReport(@PathVariable int user_id, @RequestParam String token, @RequestParam String time) throws NoSuchAlgorithmException {
        // Find the user who requested
        User user = userService.findUser(String.valueOf(user_id));

        if (!urlSecurity.isUrlTokenValid(token, time, user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        ByteArrayInputStream pdf = pdfExportService.getProductStockPdfReport(user.getFullName());
        InputStreamResource resource = new InputStreamResource(pdf);
        // Set content disposition to inline instead of attachment so that file is not auto downloaded
        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=\"product-stock-report.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
}
