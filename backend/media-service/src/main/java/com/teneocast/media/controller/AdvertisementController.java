package com.teneocast.media.controller;

import com.teneocast.media.dto.ApiResponse;
import com.teneocast.media.dto.AdvertisementDto;
import com.teneocast.media.dto.AdTypeDto;
import com.teneocast.media.dto.UploadAdvertisementRequest;
import com.teneocast.media.service.AdvertisementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/media/ad")
@RequiredArgsConstructor
@Slf4j
public class AdvertisementController {
    
    private final AdvertisementService advertisementService;
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AdvertisementDto>> uploadAdvertisement(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("adTypeId") Long adTypeId,
            @RequestParam("targetAudience") String targetAudience,
            @RequestHeader("X-Tenant-ID") UUID tenantId) {
        
        try {
            UploadAdvertisementRequest request = UploadAdvertisementRequest.builder()
                    .name(name)
                    .description(description)
                    .adTypeId(adTypeId)
                    .targetAudience(targetAudience)
                    .build();
            
            AdvertisementDto advertisement = advertisementService.uploadAdvertisement(file, request, tenantId);
            return ResponseEntity.ok(ApiResponse.success(advertisement, "Advertisement uploaded successfully"));
            
        } catch (IllegalArgumentException e) {
            log.error("Validation error during advertisement upload: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (IOException e) {
            log.error("IO error during advertisement upload: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to upload advertisement file"));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdvertisementDto>> getAdvertisementById(
            @PathVariable Long id,
            @RequestHeader("X-Tenant-ID") UUID tenantId) {
        
        try {
            AdvertisementDto advertisement = advertisementService.getAdvertisementById(id, tenantId);
            return ResponseEntity.ok(ApiResponse.success(advertisement));
        } catch (IllegalArgumentException e) {
            log.error("Error getting advertisement by ID: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdvertisementDto>>> getAdvertisementsByTenant(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long adTypeId,
            @RequestParam(required = false) String search) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<AdvertisementDto> advertisementPage;
            
            if (adTypeId != null) {
                advertisementPage = advertisementService.getAdvertisementsByTenantAndAdType(tenantId, adTypeId, pageable);
            } else if (search != null && !search.trim().isEmpty()) {
                advertisementPage = advertisementService.searchAdvertisements(tenantId, search.trim(), pageable);
            } else {
                advertisementPage = advertisementService.getAdvertisementsByTenant(tenantId, pageable);
            }
            
            return ResponseEntity.ok(ApiResponse.success(advertisementPage));
        } catch (Exception e) {
            log.error("Error getting advertisements by tenant: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to retrieve advertisements"));
        }
    }
    
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<AdvertisementDto>>> getAllAdvertisementsByTenant(
            @RequestHeader("X-Tenant-ID") UUID tenantId) {
        
        try {
            List<AdvertisementDto> advertisementList = advertisementService.getAllAdvertisementsByTenant(tenantId);
            return ResponseEntity.ok(ApiResponse.success(advertisementList));
        } catch (Exception e) {
            log.error("Error getting all advertisements by tenant: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to retrieve advertisements"));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAdvertisement(
            @PathVariable Long id,
            @RequestHeader("X-Tenant-ID") UUID tenantId) {
        
        try {
            advertisementService.deleteAdvertisement(id, tenantId);
            return ResponseEntity.ok(ApiResponse.success(null, "Advertisement deleted successfully"));
        } catch (IllegalArgumentException e) {
            log.error("Error deleting advertisement: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (IOException e) {
            log.error("IO error during advertisement deletion: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to delete advertisement file"));
        }
    }
    
    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<AdTypeDto>>> getAdTypesByTenant(
            @RequestHeader("X-Tenant-ID") UUID tenantId) {
        
        try {
            List<AdTypeDto> adTypes = advertisementService.getAdTypesByTenant(tenantId);
            return ResponseEntity.ok(ApiResponse.success(adTypes));
        } catch (Exception e) {
            log.error("Error getting ad types by tenant: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to retrieve ad types"));
        }
    }
}
