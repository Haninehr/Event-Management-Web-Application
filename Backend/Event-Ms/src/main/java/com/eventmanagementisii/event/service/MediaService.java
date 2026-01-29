package com.eventmanagementisii.event.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.util.StringUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


@Service
public class MediaService {

    @Value("${app.upload-dir}")
    private String uploadDir;


        public String storeFile(MultipartFile file , String mediaType) throws IOException {
        // Get the filename sent by frontend (e.g., "20251216-1435-a3f9e2.jpg")
        String filename = file.getOriginalFilename();

        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be empty");
        }

        // Security: sanitize to prevent directory traversal (e.g., "../../evil.jpg")
        filename = StringUtils.cleanPath(filename);

        // Optional extra safety: reject if it still contains ".." or starts with "/"
        if (filename.contains("..") || filename.startsWith("/")) {
            throw new IllegalArgumentException("Invalid filename: " + filename);
        }

        Path targetPath = Paths.get(uploadDir).resolve(filename).normalize();

        // Ensure parent directory exists
        Files.createDirectories(targetPath.getParent());

        // Save the file (replace if exists, unlikely due to timestamp + random)
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return filename; // return the name used for storage
    }

    public String getMediaUrl(String fileName) {
       
        return "/uploads/events-media/" + fileName;
    }
}