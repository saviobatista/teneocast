package com.teneocast.media.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class MediaProcessingService {
    
    private final Tika tika;
    
    @Value("${media.upload.allowed-audio-formats}")
    private String allowedAudioFormats;
    
    @Value("${media.upload.max-file-size}")
    private String maxFileSize;
    
    public MediaProcessingService() {
        this.tika = new Tika();
    }
    
    /**
     * Validate uploaded file
     * @param file The file to validate
     * @throws IllegalArgumentException If validation fails
     */
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        // Check file size
        long maxSize = parseFileSize(maxFileSize);
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size: " + maxFileSize);
        }
        
        // Check file format
        String detectedType = detectFileType(file);
        if (!isAudioFile(detectedType)) {
            throw new IllegalArgumentException("Invalid file type. Only audio files are allowed. Detected: " + detectedType);
        }
        
        // Check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && !hasValidExtension(originalFilename)) {
            throw new IllegalArgumentException("Invalid file extension. Allowed: " + allowedAudioFormats);
        }
        
        log.info("File validation passed: name={}, size={}, type={}", 
                originalFilename, file.getSize(), detectedType);
    }
    
    /**
     * Extract metadata from audio file
     * @param file The audio file
     * @return Extracted metadata as JSON string
     */
    public String extractMetadata(MultipartFile file) {
        try {
            Metadata metadata = new Metadata();
            tika.parse(file.getInputStream(), metadata);
            
            // Extract relevant audio metadata
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{");
            
            String[] names = metadata.names();
            boolean first = true;
            
            for (String name : names) {
                if (isRelevantMetadata(name)) {
                    if (!first) {
                        jsonBuilder.append(",");
                    }
                    jsonBuilder.append("\"").append(name).append("\":\"")
                             .append(metadata.get(name)).append("\"");
                    first = false;
                }
            }
            
            jsonBuilder.append("}");
            return jsonBuilder.toString();
            
        } catch (Exception e) {
            log.warn("Failed to extract metadata from file: {}", file.getOriginalFilename(), e);
            return "{}";
        }
    }
    
    /**
     * Get file duration in seconds (placeholder implementation)
     * @param file The audio file
     * @return Duration in seconds, or null if cannot determine
     */
    public Integer getFileDuration(MultipartFile file) {
        // This is a placeholder implementation
        // In a real implementation, you would use a library like FFmpeg or JavaZoom
        // to extract actual audio duration
        try {
            // For now, return null - duration will need to be set manually or via external processing
            return null;
        } catch (Exception e) {
            log.warn("Failed to get file duration: {}", file.getOriginalFilename(), e);
            return null;
        }
    }
    
    private String detectFileType(MultipartFile file) {
        try {
            return tika.detect(file.getInputStream(), file.getOriginalFilename());
        } catch (IOException e) {
            log.warn("Failed to detect file type for: {}", file.getOriginalFilename(), e);
            return "unknown";
        }
    }
    
    private boolean isAudioFile(String mimeType) {
        return mimeType != null && mimeType.startsWith("audio/");
    }
    
    private boolean hasValidExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return false;
        }
        
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        List<String> allowedFormats = Arrays.asList(allowedAudioFormats.split(","));
        return allowedFormats.contains(extension);
    }
    
    private long parseFileSize(String sizeString) {
        try {
            if (sizeString.endsWith("MB")) {
                return Long.parseLong(sizeString.replace("MB", "")) * 1024 * 1024;
            } else if (sizeString.endsWith("KB")) {
                return Long.parseLong(sizeString.replace("KB", "")) * 1024;
            } else if (sizeString.endsWith("B")) {
                return Long.parseLong(sizeString.replace("B", ""));
            } else {
                return Long.parseLong(sizeString);
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid file size format: {}, using default 100MB", sizeString);
            return 100 * 1024 * 1024; // Default to 100MB
        }
    }
    
    private boolean isRelevantMetadata(String metadataName) {
        // Filter for relevant audio metadata
        String lowerName = metadataName.toLowerCase();
        return lowerName.contains("duration") || 
               lowerName.contains("bitrate") || 
               lowerName.contains("sample") || 
               lowerName.contains("channel") ||
               lowerName.contains("format") ||
               lowerName.contains("title") ||
               lowerName.contains("artist") ||
               lowerName.contains("album");
    }
}
