package com.teneocast.media.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;
import org.springframework.core.io.Resource;

public interface StorageService {
    
    /**
     * Upload a file to storage
     * @param file The file to upload
     * @param tenantId The tenant ID for isolation
     * @param folder The folder path within tenant storage
     * @return The file path in storage
     * @throws IOException If upload fails
     */
    String uploadFile(MultipartFile file, UUID tenantId, String folder) throws IOException;
    
    /**
     * Delete a file from storage
     * @param filePath The file path to delete
     * @param tenantId The tenant ID for validation
     * @throws IOException If deletion fails
     */
    void deleteFile(String filePath, UUID tenantId) throws IOException;
    
    /**
     * Get the full URL for a file
     * @param filePath The file path
     * @return The full URL
     */
    String getFileUrl(String filePath);
    
    /**
     * Check if a file exists
     * @param filePath The file path
     * @return True if file exists
     */
    boolean fileExists(String filePath);
    
    /**
     * Get file size
     * @param filePath The file path
     * @return File size in bytes
     */
    long getFileSize(String filePath);
    
    /**
     * Get file as resource for download
     * @param filePath The file path
     * @param tenantId The tenant ID for validation
     * @return File resource
     * @throws IOException If retrieval fails
     */
    Resource getFileAsResource(String filePath, UUID tenantId) throws IOException;
}
