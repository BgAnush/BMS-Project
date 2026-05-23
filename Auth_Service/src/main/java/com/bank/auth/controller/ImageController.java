package com.bank.auth.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Hidden;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequestMapping("/uploads")
public class ImageController {

    private static final String UPLOAD_DIR = "C:/Capgemini Project/Auth_Service/uploads/";
    @Hidden
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {

        if (filename == null || filename.isBlank()) {
            log.warn("Invalid filename received");
            return ResponseEntity.badRequest().build();
        }

        try {
            Path filePath = Paths.get(UPLOAD_DIR)
                    .resolve(filename)
                    .normalize();

            log.info("Fetching image from path: {}", filePath);

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.warn("File not found or not readable: {}", filename);
                return ResponseEntity.notFound().build();
            }

            // Detect content type safely
            String contentType = Files.probeContentType(filePath);

            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (Exception ex) {
            log.error("Error while retrieving image: {}", filename, ex);
            return ResponseEntity.internalServerError().build();
        }
    }
}