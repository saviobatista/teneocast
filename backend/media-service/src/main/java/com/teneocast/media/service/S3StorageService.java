package com.teneocast.media.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@Slf4j
public class S3StorageService implements StorageService {
    
    private final S3Client s3Client;
    
    @Value("${aws.s3.bucket-prefix}")
    private String bucketPrefix;
    
    @Value("${aws.s3.endpoint}")
    private String endpoint;
    
    public S3StorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }
    
    @Override
    public String uploadFile(MultipartFile file, UUID tenantId, String folder) throws IOException {
        try {
            String bucketName = getBucketName(tenantId);
            String key = generateFileKey(tenantId, folder, file.getOriginalFilename());
            
            // Ensure bucket exists
            ensureBucketExists(bucketName);
            
            // Upload file
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();
            
            try (InputStream inputStream = file.getInputStream()) {
                s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
            }
            
            log.info("File uploaded successfully: bucket={}, key={}, size={}", bucketName, key, file.getSize());
            return key;
            
        } catch (Exception e) {
            log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
            throw new IOException("Failed to upload file: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteFile(String filePath, UUID tenantId) throws IOException {
        try {
            String bucketName = getBucketName(tenantId);
            
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filePath)
                    .build();
            
            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully: bucket={}, key={}", bucketName, filePath);
            
        } catch (Exception e) {
            log.error("Failed to delete file: {}", filePath, e);
            throw new IOException("Failed to delete file: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getFileUrl(String filePath) {
        // For MinIO, construct the URL manually
        return endpoint + "/" + bucketPrefix + "/" + filePath;
    }
    
    @Override
    public boolean fileExists(String filePath) {
        try {
            // We need to determine the bucket from the file path
            // This is a simplified implementation
            String bucketName = bucketPrefix + "-default";
            
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filePath)
                    .build();
            
            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("Error checking file existence: {}", filePath, e);
            return false;
        }
    }
    
    @Override
    public long getFileSize(String filePath) {
        try {
            // We need to determine the bucket from the file path
            // This is a simplified implementation
            String bucketName = bucketPrefix + "-default";
            
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filePath)
                    .build();
            
            HeadObjectResponse response = s3Client.headObject(headObjectRequest);
            return response.contentLength();
        } catch (Exception e) {
            log.error("Error getting file size: {}", filePath, e);
            return -1;
        }
    }

    @Override
    public Resource getFileAsResource(String filePath, UUID tenantId) throws IOException {
        try {
            String bucketName = getBucketName(tenantId);
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filePath)
                    .build();

            ResponseInputStream<GetObjectResponse> responseInputStream = s3Client.getObject(getObjectRequest);
            byte[] content = responseInputStream.readAllBytes();
            return new ByteArrayResource(content);
        } catch (Exception e) {
            log.error("Error getting file as resource: {}", filePath, e);
            throw new IOException("Failed to get file as resource: " + e.getMessage(), e);
        }
    }
    
    private String getBucketName(UUID tenantId) {
        return bucketPrefix + "-" + tenantId.toString();
    }
    
    private String generateFileKey(UUID tenantId, String folder, String originalFilename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String extension = getFileExtension(originalFilename);
        String filename = originalFilename != null ? originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_") : "file";
        
        return String.format("%s/%s/%s_%s%s", 
                tenantId.toString(), 
                folder, 
                filename.substring(0, Math.min(filename.length(), 50)), 
                timestamp, 
                extension);
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return "." + filename.substring(filename.lastIndexOf(".") + 1);
    }
    
    private void ensureBucketExists(String bucketName) {
        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            
            s3Client.headBucket(headBucketRequest);
        } catch (NoSuchBucketException e) {
            // Create bucket if it doesn't exist
            CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            
            s3Client.createBucket(createBucketRequest);
            log.info("Created bucket: {}", bucketName);
        } catch (Exception e) {
            log.error("Error ensuring bucket exists: {}", bucketName, e);
            throw new RuntimeException("Failed to ensure bucket exists: " + bucketName, e);
        }
    }
}
