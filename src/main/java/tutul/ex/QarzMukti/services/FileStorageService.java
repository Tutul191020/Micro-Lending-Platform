package tutul.ex.QarzMukti.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import tutul.ex.QarzMukti.common.ValidationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    @Value("${file.upload.directory:uploads}")
    private String uploadDirectory;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif"
    );

    private static final List<String> ALLOWED_DOCUMENT_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "application/pdf"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * Validate uploaded file
     */
    public void validateFile(MultipartFile file, String documentType) {
        if (file.isEmpty()) {
            throw new ValidationException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ValidationException("File size exceeds maximum limit of 5MB");
        }

        String contentType = file.getContentType();
        
        if ("PROFILE_PHOTO".equals(documentType)) {
            if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
                throw new ValidationException("Profile photo must be JPEG, JPG, PNG, or GIF");
            }
        } else {
            if (!ALLOWED_DOCUMENT_TYPES.contains(contentType)) {
                throw new ValidationException("Document must be JPEG, PNG, or PDF");
            }
        }
    }

    /**
     * Store uploaded file
     */
    public String store(MultipartFile file, String userId, String documentType) {
        try {
            // Create directory structure: uploads/userId/documentType/
            Path userDirectory = Paths.get(uploadDirectory, userId.toString(), documentType.toLowerCase());
            Files.createDirectories(userDirectory);

            // Generate unique filename
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = getFileExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + fileExtension;

            // Store file
            Path targetLocation = userDirectory.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Return relative path
            String relativePath = userId.toString() + "/" + documentType.toLowerCase() + "/" + newFilename;
            log.info("File stored successfully: {}", relativePath);
            
            return relativePath;

        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new RuntimeException("Failed to store file: " + e.getMessage());
        }
    }

    /**
     * Delete file
     */
    public void delete(String filePath) {
        try {
            Path path = Paths.get(uploadDirectory, filePath);
            Files.deleteIfExists(path);
            log.info("File deleted successfully: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", filePath, e);
        }
    }

    /**
     * Get full file path
     */
    public Path getFilePath(String relativePath) {
        return Paths.get(uploadDirectory, relativePath);
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : filename.substring(lastDotIndex);
    }
}