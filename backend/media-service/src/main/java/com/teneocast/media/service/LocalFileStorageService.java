package com.teneocast.media.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.teneocast.media.config.S3DisabledCondition;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
@Conditional(S3DisabledCondition.class)
public class LocalFileStorageService implements StorageService {
    
    @Value("${media.upload.storage-path:/tmp/teneocast-media}")
    private String storagePath;
    
    @Override
    public String uploadFile(MultipartFile file, UUID tenantId, String folder) throws IOException {
        try {
            String fileName = generateFileName(file.getOriginalFilename());
            Path tenantPath = Paths.get(storagePath, tenantId.toString(), folder);
            Path filePath = tenantPath.resolve(fileName);
            
            // Create directories if they don't exist
            Files.createDirectories(tenantPath);
            
            // Copy file to local storage
            Files.copy(file.getInputStream(), filePath);
            
            log.info("File uploaded successfully to local storage: {}", filePath);
            return filePath.toString();
            
        } catch (Exception e) {
            log.error("Failed to upload file to local storage: {}", file.getOriginalFilename(), e);
            throw new IOException("Failed to upload file: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteFile(String filePath, UUID tenantId) throws IOException {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("File deleted successfully from local storage: {}", filePath);
            }
        } catch (Exception e) {
            log.error("Failed to delete file from local storage: {}", filePath, e);
            throw new IOException("Failed to delete file: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getFileUrl(String filePath) {
        return "file://" + filePath;
    }
    
    @Override
    public boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }
    
    @Override
    public long getFileSize(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                return Files.size(path);
            }
        } catch (IOException e) {
            log.error("Error getting file size: {}", filePath, e);
        }
        return -1;
    }
    
    @Override
    public Resource getFileAsResource(String filePath, UUID tenantId) throws IOException {
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            return new FileSystemResource(path);
        }
        throw new IOException("File not found: " + filePath);
    }
    
    private String generateFileName(String originalFilename) {
        if (originalFilename == null || originalFilename.isEmpty()) {
            return "file_" + System.currentTimeMillis();
        }
        
        String timestamp = String.valueOf(System.currentTimeMillis());
        String extension = getFileExtension(originalFilename);
        String baseName = originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
        
        return baseName.substring(0, Math.min(baseName.length(), 50)) + "_" + timestamp + extension;
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return "." + filename.substring(filename.lastIndexOf(".") + 1);
    }
}
