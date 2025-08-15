package com.teneocast.media.service;

import com.teneocast.media.dto.AdvertisementDto;
import com.teneocast.media.dto.AdTypeDto;
import com.teneocast.media.dto.UploadAdvertisementRequest;
import com.teneocast.media.entity.Advertisement;
import com.teneocast.media.entity.AdType;
import com.teneocast.media.repository.AdTypeRepository;
import com.teneocast.media.repository.AdvertisementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvertisementService {
    
    private final AdvertisementRepository advertisementRepository;
    private final AdTypeRepository adTypeRepository;
    private final StorageService storageService;
    private final MediaProcessingService mediaProcessingService;
    
    /**
     * Upload advertisement file
     * @param file The audio file
     * @param request Upload request with metadata
     * @param tenantId The tenant ID
     * @return The created advertisement DTO
     * @throws IOException If upload fails
     */
    @Transactional
    public AdvertisementDto uploadAdvertisement(MultipartFile file, UploadAdvertisementRequest request, UUID tenantId) throws IOException {
        log.info("Uploading advertisement file: name={}, tenant={}", file.getOriginalFilename(), tenantId);
        
        // Validate file
        mediaProcessingService.validateFile(file);
        
        // Check if advertisement already exists
        if (advertisementRepository.existsByTenantIdAndName(tenantId, request.getName())) {
            throw new IllegalArgumentException("Advertisement with this name already exists for this tenant");
        }
        
        // Get ad type
        AdType adType = adTypeRepository.findById(request.getAdTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid ad type ID: " + request.getAdTypeId()));
        
        // Upload file to storage
        String filePath = storageService.uploadFile(file, tenantId, "advertisements");
        
        // Extract metadata
        String metadata = mediaProcessingService.extractMetadata(file);
        Integer duration = mediaProcessingService.getFileDuration(file);
        
        // Create advertisement entity
        Advertisement advertisement = Advertisement.builder()
                .tenantId(tenantId)
                .adType(adType)
                .name(request.getName())
                .description(request.getDescription())
                .durationSeconds(duration)
                .filePath(filePath)
                .fileSize(file.getSize())
                .fileFormat(getFileExtension(file.getOriginalFilename()))
                .targetAudience(request.getTargetAudience())
                .metadata(metadata)
                .build();
        
        // Save to database
        Advertisement savedAdvertisement = advertisementRepository.save(advertisement);
        
        log.info("Advertisement uploaded successfully: id={}, name={}", 
                savedAdvertisement.getId(), savedAdvertisement.getName());
        
        return mapToDto(savedAdvertisement);
    }
    
    /**
     * Get advertisement by ID
     * @param id Advertisement ID
     * @param tenantId Tenant ID for validation
     * @return Advertisement DTO
     */
    public AdvertisementDto getAdvertisementById(Long id, UUID tenantId) {
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Advertisement not found with ID: " + id));
        
        if (!advertisement.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Access denied to advertisement from different tenant");
        }
        
        return mapToDto(advertisement);
    }
    
    /**
     * Get advertisements by tenant with pagination
     * @param tenantId Tenant ID
     * @param pageable Pagination parameters
     * @return Page of advertisement DTOs
     */
    public Page<AdvertisementDto> getAdvertisementsByTenant(UUID tenantId, Pageable pageable) {
        Page<Advertisement> advertisementPage = advertisementRepository.findByTenantId(tenantId, pageable);
        return advertisementPage.map(this::mapToDto);
    }
    
    /**
     * Get advertisements by tenant and ad type
     * @param tenantId Tenant ID
     * @param adTypeId Ad type ID
     * @param pageable Pagination parameters
     * @return Page of advertisement DTOs
     */
    public Page<AdvertisementDto> getAdvertisementsByTenantAndAdType(UUID tenantId, Long adTypeId, Pageable pageable) {
        Page<Advertisement> advertisementPage = advertisementRepository.findByTenantIdAndAdTypeId(tenantId, adTypeId, pageable);
        return advertisementPage.map(this::mapToDto);
    }
    
    /**
     * Search advertisements by tenant and search term
     * @param tenantId Tenant ID
     * @param searchTerm Search term
     * @param pageable Pagination parameters
     * @return Page of advertisement DTOs
     */
    public Page<AdvertisementDto> searchAdvertisements(UUID tenantId, String searchTerm, Pageable pageable) {
        Page<Advertisement> advertisementPage = advertisementRepository.findByTenantIdAndSearchTerm(tenantId, searchTerm, pageable);
        return advertisementPage.map(this::mapToDto);
    }
    
    /**
     * Get all advertisements for tenant (for player service)
     * @param tenantId Tenant ID
     * @return List of advertisement DTOs
     */
    public List<AdvertisementDto> getAllAdvertisementsByTenant(UUID tenantId) {
        List<Advertisement> advertisementList = advertisementRepository.findAllByTenantId(tenantId);
        return advertisementList.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Delete advertisement file
     * @param id Advertisement ID
     * @param tenantId Tenant ID for validation
     * @throws IOException If deletion fails
     */
    @Transactional
    public void deleteAdvertisement(Long id, UUID tenantId) throws IOException {
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Advertisement not found with ID: " + id));
        
        if (!advertisement.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Access denied to advertisement from different tenant");
        }
        
        // Delete from storage
        storageService.deleteFile(advertisement.getFilePath(), tenantId);
        
        // Delete from database
        advertisementRepository.delete(advertisement);
        
        log.info("Advertisement deleted successfully: id={}, name={}", id, advertisement.getName());
    }
    
    /**
     * Get ad types for tenant
     * @param tenantId Tenant ID
     * @return List of ad type DTOs
     */
    public List<AdTypeDto> getAdTypesByTenant(UUID tenantId) {
        List<AdType> adTypes = adTypeRepository.findByTenantIdOrGlobal(tenantId);
        return adTypes.stream()
                .map(this::mapAdTypeToDto)
                .collect(Collectors.toList());
    }
    
    private AdvertisementDto mapToDto(Advertisement advertisement) {
        return AdvertisementDto.builder()
                .id(advertisement.getId())
                .tenantId(advertisement.getTenantId())
                .adTypeId(advertisement.getAdType() != null ? advertisement.getAdType().getId() : null)
                .adTypeName(advertisement.getAdType() != null ? advertisement.getAdType().getName() : null)
                .name(advertisement.getName())
                .description(advertisement.getDescription())
                .durationSeconds(advertisement.getDurationSeconds())
                .filePath(advertisement.getFilePath())
                .fileSize(advertisement.getFileSize())
                .fileFormat(advertisement.getFileFormat())
                .targetAudience(advertisement.getTargetAudience())
                .metadata(advertisement.getMetadata())
                .createdAt(advertisement.getCreatedAt())
                .updatedAt(advertisement.getUpdatedAt())
                .build();
    }
    
    private AdTypeDto mapAdTypeToDto(AdType adType) {
        return AdTypeDto.builder()
                .id(adType.getId())
                .tenantId(adType.getTenantId())
                .name(adType.getName())
                .description(adType.getDescription())
                .isSelectable(adType.getIsSelectable())
                .canPlayRemotely(adType.getCanPlayRemotely())
                .canPlayIndividually(adType.getCanPlayIndividually())
                .createdAt(adType.getCreatedAt())
                .updatedAt(adType.getUpdatedAt())
                .build();
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
